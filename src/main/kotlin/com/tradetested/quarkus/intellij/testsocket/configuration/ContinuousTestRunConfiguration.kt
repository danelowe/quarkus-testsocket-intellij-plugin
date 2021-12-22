package com.tradetested.quarkus.intellij.testsocket.configuration

import com.intellij.execution.Executor
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.junit.JUnitConfiguration
import com.intellij.execution.junit2.ui.properties.JUnitConsoleProperties
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.testframework.sm.runner.SMRunnerConsolePropertiesProvider
import com.intellij.execution.testframework.sm.runner.SMTRunnerConsoleProperties
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.tradetested.quarkus.intellij.testsocket.ContinuousTestRunProfileState

/**
 * Run Configuration for Continuous Testing
 *
 * We extend JUnit Configuration despite the fact that it's RunProfileState is command-line based,
 * because there are so many other things we get for free by re-using JUnit classes
 */
class ContinuousTestRunConfiguration(project: Project, factory: ConfigurationFactory, name: String) :
    SMRunnerConsolePropertiesProvider,
    JUnitConfiguration(name, project, factory) {

    var endpoint: String
        get() = options.endpoint
        set(value) {
            options.endpoint = value
        }

    override fun getOptions() = super.getOptions() as ContinuousTestConfigurationOptions

    override fun getState(executor: Executor, env: ExecutionEnvironment) = ContinuousTestRunProfileState(this, env)

    // @todo: Implement as per JUnitConfiguration
    override fun getValidModules(): Collection<Module> = ModuleManager.getInstance(project).modules.toList()

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> = ContinuousTestSettingsEditor(this)

    override fun checkConfiguration() {}

    override fun createTestConsoleProperties(executor: Executor): SMTRunnerConsoleProperties {
        // hardcode isIdBasedTestTree to prevent creating a runner for JUnit?
        val properties = JUnitConsoleProperties(this, executor)
        properties.isIdBasedTestTree = true
        return properties
    }
}