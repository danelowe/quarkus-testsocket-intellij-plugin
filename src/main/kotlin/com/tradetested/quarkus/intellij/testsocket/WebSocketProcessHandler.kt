package com.tradetested.quarkus.intellij.testsocket

import com.intellij.execution.process.ProcessHandler
import java.io.OutputStream

class WebSocketProcessHandler(private val client: WebSocketClient) : ProcessHandler() {
    override fun destroyProcessImpl() {
        client.close()
        notifyProcessTerminated(0);
    }

    override fun detachProcessImpl() {
        client.close()
        notifyProcessTerminated(0);
    }

    override fun detachIsDefault() = true

    override fun getProcessInput(): OutputStream? = null
}