package com.tradetested.quarkus.intellij.testsocket

import io.netty.bootstrap.Bootstrap
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.http.EmptyHttpHeaders
import io.netty.handler.codec.http.FullHttpResponse
import io.netty.handler.codec.http.HttpClientCodec
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.websocketx.*
import io.netty.util.CharsetUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI


class WebSocketClient(uri: String) {
    private val uri: URI
    private var ch: Channel? = null
    private val channel
        get() = ch ?: throw IllegalStateException("Websocket has not been opened yet")
    private lateinit var handler: WebSocketClientHandler
    private val group: EventLoopGroup = NioEventLoopGroup()
    private val textFrameHandlers = mutableListOf<(frame: String) -> Unit>()
    private val closeHandlers = mutableListOf<() -> Unit>()

    init {
        this.uri = URI.create(uri)
    }

    fun open() {
        if (ch != null) {
            return
        }
        require(uri.scheme == "ws") { "Unsupported protocol: ${uri.scheme}" }
        val bootstrap = Bootstrap()
        handler = WebSocketClientHandler(
            WebSocketClientHandshakerFactory.newHandshaker(
                uri,
                WebSocketVersion.V13,
                null,
                false,
                EmptyHttpHeaders.INSTANCE,
                1280000,
            ), this
        )

        bootstrap.group(group)
            .channel(NioSocketChannel::class.java)
            .handler(object : ChannelInitializer<SocketChannel>() {
                override fun initChannel(ch: SocketChannel) {
                    val pipeline: ChannelPipeline = ch.pipeline()
                    pipeline.addLast("http-codec", HttpClientCodec())
                    pipeline.addLast("aggregator", HttpObjectAggregator(65536))
                    pipeline.addLast("ws-handler", handler)
                }
            })
        ch = bootstrap.connect(uri.host, uri.port).sync().channel();
        handler.handshakeFuture().sync();
    }

    fun close() {
        ch?.writeAndFlush(CloseWebSocketFrame())
        ch?.closeFuture()?.sync()
        group.shutdownGracefully()
        this.closeHandlers.forEach { it() }
    }

    fun send(text: String) {
        channel.writeAndFlush(TextWebSocketFrame(text))
    }

    fun onTextFrame(handler: (frame: String) -> Unit) {
        this.textFrameHandlers.add(handler)
    }

    fun onClose(handler: () -> Unit) {
        this.closeHandlers.add(handler)
    }

    private class WebSocketClientHandler(
        private val handshaker: WebSocketClientHandshaker,
        private val client: WebSocketClient
    ) : SimpleChannelInboundHandler<Any>() {

        private var handshakeFuture: ChannelPromise? = null

        fun handshakeFuture(): ChannelFuture {
            return handshakeFuture ?: throw IllegalStateException("Handshake has not been added yet")
        }

        override fun handlerAdded(ctx: ChannelHandlerContext) {
            handshakeFuture = ctx.newPromise()
        }

        override fun channelActive(ctx: ChannelHandlerContext) {
            handshaker.handshake(ctx.channel())
        }

        override fun channelInactive(ctx: ChannelHandlerContext?) {
            //System.out.println("WebSocket Client disconnected!");
        }

        override fun channelRead0(ctx: ChannelHandlerContext?, msg: Any) {
            val ch: Channel = ctx!!.channel()

            if (!handshaker.isHandshakeComplete) {
                handshaker.finishHandshake(ch, msg as FullHttpResponse)
                handshakeFuture!!.setSuccess()
                return
            }

            if (msg is FullHttpResponse) {
                throw Exception(
                    "Unexpected FullHttpResponse (getStatus=" + msg.getStatus().toString() + ", content="
                            + msg.content().toString(CharsetUtil.UTF_8) + ')'
                )
            }
            when (msg) {
                is TextWebSocketFrame -> {
                    client.textFrameHandlers.forEach { it(msg.text()) }
                }
                is PongWebSocketFrame -> {
                }
                is CloseWebSocketFrame -> {
                    ch.close()
                    client.closeHandlers.forEach { it() }
                }
                is BinaryWebSocketFrame -> {
                }
            }
        }

        override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
            cause.printStackTrace()
            if (!handshakeFuture!!.isDone) {
                handshakeFuture!!.setFailure(cause)
            }
            ctx.close()
        }

        companion object {
            private val logger: Logger = LoggerFactory.getLogger(WebSocketClientHandler::class.java)
        }
    }
}

