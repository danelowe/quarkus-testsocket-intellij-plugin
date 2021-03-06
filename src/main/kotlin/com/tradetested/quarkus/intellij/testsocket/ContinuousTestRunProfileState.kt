package com.tradetested.quarkus.intellij.testsocket

import com.intellij.execution.DefaultExecutionResult
import com.intellij.execution.ExecutionResult
import com.intellij.execution.Executor
import com.intellij.execution.junit.JUnitConfiguration
import com.intellij.execution.junit.TestObject
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ProgramRunner
import com.intellij.execution.testframework.TestConsoleProperties
import com.intellij.execution.testframework.sm.SMTestRunnerConnectionUtil
import com.intellij.execution.testframework.sm.runner.ui.SMTRunnerConsoleView
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.PossiblyDumbAware
import com.intellij.openapi.util.Disposer
import com.intellij.psi.*
import com.intellij.refactoring.listeners.RefactoringElementListener
import com.intellij.util.ui.UIUtil
import com.tradetested.quarkus.intellij.testsocket.configuration.ContinuousTestRunConfiguration
import com.tradetested.quarkus.testsocket.spi.command.Command
import com.tradetested.quarkus.testsocket.spi.command.TestClass
import com.tradetested.quarkus.testsocket.spi.command.TestMethod
import com.tradetested.quarkus.testsocket.spi.command.TestPackage


class ContinuousTestRunProfileState(
    private val configuration: ContinuousTestRunConfiguration,
    environment: ExecutionEnvironment,
) :
    TestObject(configuration, environment), PossiblyDumbAware, DumbAware {
    var started = false
    var brokenOnly = false
    var liveReload = false

    lateinit var actions: Collection<AnAction>

    override fun execute(executor: Executor, runner: ProgramRunner<*>): ExecutionResult {
        val testConsoleProperties = getConfiguration().createTestConsoleProperties(executor)
        testConsoleProperties.setIfUndefined(TestConsoleProperties.HIDE_PASSED_TESTS, false)
        val consoleView = UIUtil.invokeAndWaitIfNeeded<SMTRunnerConsoleView> {
            SMTestRunnerConnectionUtil.createConsole(testConsoleProperties)
        }
        consoleView.resultsViewer.testsRootNode.executionId = environment.executionId
        Disposer.register(configuration.project, consoleView)
        val client = WebSocketClient(configuration.endpoint)
        val handler = WebSocketProcessHandler(client)
        val result = DefaultExecutionResult(consoleView, handler)
        val manager = ContinuousTestWebSocketManager(this, consoleView  as SMTRunnerConsoleView, client, handler)
        consoleView.attachToProcess(handler)
        manager.start(command)
        actions = listOf(
            RerunFailedTestsAction(manager, consoleView, testConsoleProperties),
            StartedAction(this, manager),
            BrokenOnlyAction(this, manager),
            LiveReloadAction(this, manager),
        )

        return result
    }

    override fun suggestActionName(): String {
        throw IllegalStateException("This method is inherited from JUnit by necessity, and should not be used")
    }

    override fun getListener(element: PsiElement?, configuration: JUnitConfiguration?): RefactoringElementListener {
        throw IllegalStateException("This method is inherited from JUnit by necessity, and should not be used")
    }

    override fun isConfiguredByElement(
        configuration: JUnitConfiguration?,
        testClass: PsiClass?,
        testMethod: PsiMethod?,
        testPackage: PsiPackage?,
        testDir: PsiDirectory?
    ): Boolean {
        throw IllegalStateException("This method is inherited from JUnit by necessity, and should not be used")
    }

    private val command : Command by lazy {
        val data = configuration.persistentData
        when (data.TEST_OBJECT) {
            JUnitConfiguration.TEST_METHOD -> TestMethod(data.mainClassName, data.methodName)
            JUnitConfiguration.TEST_CLASS -> TestClass(data.mainClassName)
            JUnitConfiguration.TEST_PACKAGE -> TestPackage(data.packageName)
            JUnitConfiguration.TEST_DIRECTORY -> TestPackage(data.packageName)
            else -> throw IllegalStateException("Cannot run ${data.TEST_OBJECT}")

        }
    }
}