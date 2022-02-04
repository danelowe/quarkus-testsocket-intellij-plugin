package com.tradetested.quarkus.intellij.testsocket

import com.intellij.execution.testframework.sm.runner.SMTRunnerEventsListener
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
    private val publisher = consoleView.properties.project.messageBus.syncPublisher(SMTRunnerEventsListener.TEST_STATUS)
    fun start(command: Command) {
        handler.startNotify();
        try {
            client.open()
            run(command)
            client.onTextFrame {
                val event = format.decodeFromString<TestEvent>(it)
                when (event) {
                    is RunStartedEvent -> runStarted()
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

    fun run(command: Command) {
        client.send(command.toString())
    }

    private fun runStarted() {
        nodes.clear()
        rootNode.children.clear()
        rootNode.setSuiteStarted()
        consoleView.clear()
        viewer.onTestingStarted(rootNode)
        viewer.onSuiteStarted(rootNode)
        publisher.onTestingStarted(rootNode)
        publisher.onSuiteStarted(rootNode)
    }

    private fun registerTestOrSuite(
        id: String,
        parentId: String?,
        name: String,
        className: String?,
        methodName: String?
    ) : SMTestProxy? {
        if (className == null) {
            // Have test classes directly in the root node, JUnit/ArchUnit etc should not appear in tree.
            return null;
        }
        val node = SMTestProxy(name, methodName == null, "")
        registerNode(id, node)
        val parent = findNode(parentId)
        parent.addChild(node)
        node.setStarted()
        viewer.onTestStarted(node)
        publisher.onTestStarted(node)
        checkComplete(parent)
        return node
    }

    private fun testStarted(event: TestStartedEvent) {
        registerTestOrSuite(event.id, event.parentId, event.name, event.className, event.methodName)
    }

    private fun testComplete(event: TestCompleteEvent) {
        var node = findNode(event.id)
        if ((node == rootNode) && (event.className != null)) {
            // If the test has been skipped, it may not have had a start event in order for it to be registered.
            node = registerTestOrSuite(
                event.id,
                event.parentId,
                event.methodName ?: event.className,
                event.className,
                event.methodName
            ) ?: node
        }
        node.setDuration(event.duration)
        event.log.forEach(node::addStdOutput)
        when (event.status) {
            TestStatus.SUCCESSFUL -> {
                node.setFinished()
                viewer.onTestFinished(node)
                publisher.onTestFinished(node)
            }
            TestStatus.ABORTED -> {
                node.setTestIgnored(null, null)
                viewer.onTestIgnored(node)
                publisher.onTestIgnored(node)
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
                }
                viewer.onTestFailed(node)
                publisher.onTestFailed(node)
            }
        }
        checkComplete(node.parent)
    }

    private fun runComplete(event: RunCompleteEvent) {
        rootNode.setDuration(event.duration)
        rootNode.setFinished()
        viewer.onBeforeTestingFinished(rootNode)
        publisher.onBeforeTestingFinished(rootNode)
        abortChildren(rootNode)
        viewer.onTestFinished(rootNode)
        publisher.onTestFinished(rootNode)
    }

    private fun abortChildren(node: SMTestProxy) {
        if (node.isInProgress) {
            node.setTestIgnored(null, null)
            viewer.onTestIgnored(node)
            publisher.onTestIgnored(node)
            node.children.forEach { abortChildren(it) }
        }
    }

    private fun noTests(event: NoTestsEvent) {
        rootNode.setDuration(event.duration)
        rootNode.setTestFailed("No Tests", null, false)
    }

    private fun checkComplete(node: SMTestProxy?) {
        if (node == null) {
            return
        }
        if (node.children?.all { it.isIgnored } == true) {
            node.setTestIgnored(null, null)
            viewer.onTestIgnored(node)
            publisher.onTestIgnored(node)
        }
        if (node.children?.any { it.isInProgress } != false) {
            node.setStarted()
        } else {
            node.setFinished()
            viewer.onTestFinished(node)
            publisher.onTestFinished(node)
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