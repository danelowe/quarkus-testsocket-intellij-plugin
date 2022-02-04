package com.tradetested.quarkus.intellij.testsocket

import com.intellij.execution.Executor
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.RunConfigurationBase
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.testframework.sm.runner.SMTRunnerConsoleProperties
import com.intellij.execution.testframework.sm.runner.ui.SMTRunnerConsoleView
import com.intellij.idea.ActionsBundle
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.options.SettingsEditor
import com.tradetested.quarkus.intellij.testsocket.configuration.ContinuousTestRunConfiguration
import com.tradetested.quarkus.testsocket.spi.command.RerunFailed
import org.jdom.Element

class RerunFailedTestsAction(
    private val manager: ContinuousTestWebSocketManager,
    private val consoleView: SMTRunnerConsoleView,
    private val consoleProperties: SMTRunnerConsoleProperties
    ) : AnAction() {
    private val LOG: Logger = Logger.getInstance(RerunFailedTestsAction::class.java)
    init {
        ActionUtil.copyFrom(this, "RerunFailedTests")
        registerCustomShortcutSet(shortcutSet, consoleView.console.component)
    }
    override fun actionPerformed(e: AnActionEvent) {
        manager.run(RerunFailed())
//        val existingEnvrionment = e.getData(ExecutionDataKeys.EXECUTION_ENVIRONMENT) ?: return
//        val profile = MyRunProfile(consoleProperties.configuration as ContinuousTestRunConfiguration)
//        val environment = ExecutionEnvironmentBuilder(existingEnvrionment).runProfile(profile).build()
//        try {
//            environment.runner.execute(environment)
//        } catch (e: ExecutionException) {
//            LOG.error(e)
//        }
    }

    class MyRunProfile(private val configuration: ContinuousTestRunConfiguration) : RunConfigurationBase<Element>(
        configuration.project,
        configuration.factory,
        ActionsBundle.message("action.RerunFailedTests.text")
    ) {
        override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState =
            ContinuousTestRunProfileState(configuration, environment, true)
        override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> = configuration.configurationEditor
    }
}