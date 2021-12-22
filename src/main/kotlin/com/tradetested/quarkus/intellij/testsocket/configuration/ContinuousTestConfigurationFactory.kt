package com.tradetested.quarkus.intellij.testsocket.configuration

import com.intellij.execution.BeforeRunTask
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key

class ContinuousTestConfigurationFactory(type: ConfigurationType) : ConfigurationFactory(type) {
    override fun getId() = ContinuousTestConfigurationType.ID
    override fun createTemplateConfiguration(project: Project) = ContinuousTestRunConfiguration(project, this, ContinuousTestConfigurationType.ID)
    override fun getOptionsClass() = ContinuousTestConfigurationOptions::class.java

    // Disable hardcoded defaults, e.g. Make
    override fun configureBeforeRunTaskDefaults(
        providerID: Key<out BeforeRunTask<BeforeRunTask<*>>>,
        task: BeforeRunTask<out BeforeRunTask<*>>
    ) {
        task.isEnabled = false
    }
}