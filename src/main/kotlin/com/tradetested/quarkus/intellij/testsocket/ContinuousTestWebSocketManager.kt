package com.tradetested.quarkus.intellij.testsocket

import com.intellij.execution.testframework.sm.runner.SMTestProxy
import com.intellij.execution.testframework.sm.runner.ui.SMTRunnerConsoleView
import com.tradetested.quarkus.intellij.testsocket.events.*
import com.tradetested.quarkus.testsocket.spi.command.Command
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.net.ConnectException

class ContinuousTestWebSocketManager(
    private val consoleView: SMTRunnerConsoleView,
    private val client: WebSocketClient,
    private val handler: WebSocketProcessHandler
) {
    val format = Json { classDiscriminator = "event" }
    private val viewer = consoleView.resultsViewer
    private val rootNode = viewer.testsRootNode
    private val nodes = mutableMapOf<String, SMTestProxy>()
    fun runTests(command: Command) {
        handler.startNotify();
        try {
            client.open()
            client.send(command.toString())
            client.onTextFrame {
                val event = format.decodeFromString<TestEvent>(it)
                when (event) {
                    is RunStartedEvent -> runStarted(event)
                    is TestStartedEvent -> testStarted(event)
                    is TestCompleteEvent -> testComplete(event)
                    is RunCompleteEvent -> runComplete(event)
                    is NoTestsEvent -> noTests(event)
                }
            }
        } catch (e: ConnectException) {
            rootNode.setTestFailed("Continuous Testing endpoint not available", e.stackTraceToString(), false)
        }
    }

    private fun runStarted(event: RunStartedEvent) {
        nodes.clear()
        rootNode.children.clear()
        rootNode.setSuiteStarted()
        consoleView.clear()
        viewer.onTestingStarted(rootNode)
        viewer.onSuiteStarted(rootNode)
    }

    private fun testStarted(event: TestStartedEvent) {
        if (event.className == null) {
            // Have test classes directly in the root node, JUnit/ArchUnit etc should not appear in tree.
            return;
        }
        val node = SMTestProxy(event.name, event.methodName == null, "")
        registerNode(event.id, node)
        val parent = findNode(event.parentId)
        parent.addChild(node)
        node.setStarted()
        checkComplete(parent)
    }

    private fun testComplete(event: TestCompleteEvent) {
        val node = findNode(event.id)
        node.setDuration(event.duration)
        event.log.forEach(node::addStdOutput)
        when (event.status) {
            TestStatus.SUCCESSFUL -> node.setFinished()
            TestStatus.ABORTED -> {
                node.setTestIgnored(null, null)
                viewer.onTestIgnored(node)
            }
            TestStatus.FAILED -> {
                if (event.details == null) {
                    node.setTestFailed("Unknown error", "", true)
                } else {
                    val details = event.details
                    when (details.type) {
                        TestDetailsType.COMPARISON -> node.setTestComparisonFailed(
                            details.message,
                            details.stackTrace,
                            details.actual,
                            details.expected
                        )
                        TestDetailsType.ERROR -> node.setTestFailed(details.message, details.stackTrace, true)
                    }
                    viewer.onTestFailed(node)
                }

            }
        }
        checkComplete(node.parent)
    }

    private fun runComplete(event: RunCompleteEvent) {
        rootNode.setDuration(event.duration)
        rootNode.setFinished()
        viewer.onBeforeTestingFinished(rootNode)
        viewer.onTestingStarted(rootNode)
    }

    private fun noTests(event: NoTestsEvent) {
        rootNode.setDuration(event.duration)
        rootNode.setTestFailed("No Tests", null, false)
    }

    private fun checkComplete(node: SMTestProxy?) {
        if (node == null) {
            return
        }
        if (node.children?.any { it.isInProgress } != false) {
            node.setStarted()
        } else {
            node.setFinished()
        }
        checkComplete(node.parent)
    }

    private fun findNode(id: String?): SMTestProxy = if (id != null) (nodes[id] ?: rootNode) else rootNode
    private fun registerNode(id: String, node: SMTestProxy) = nodes.put(id, node)

    /*

    result.setRestartActions( //restart failed, //toggle auto test)

    // on test run start
    if (getConfiguration().isSaveOutputToFile()) {
      final File file = OutputFileUtil.getOutputFile(getConfiguration());
      root.setOutputFilePath(file != null ? file.getAbsolutePath() : null);
    }

    //on completion
    Runnable runnable = () -> {
      root.flushOutputFile();
      deleteTempFiles();
      clear();
    };
    UIUtil.invokeLaterIfNeeded(runnable);
    handler.removeProcessListener(this);
     */
}