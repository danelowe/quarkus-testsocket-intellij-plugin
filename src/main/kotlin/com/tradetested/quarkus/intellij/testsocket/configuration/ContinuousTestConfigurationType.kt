package com.tradetested.quarkus.intellij.testsocket.configuration

import com.intellij.execution.configurations.ConfigurationType
import com.intellij.openapi.project.DumbAware
import com.tradetested.quarkus.intellij.testsocket.Icons
import javax.swing.Icon

class ContinuousTestConfigurationType : ConfigurationType, DumbAware {
    override fun getDisplayName(): String = "Quarkus CT"
    override fun getConfigurationTypeDescription(): String = "Quarkus Continuous Testing"
    override fun getIcon(): Icon = Icons.Quarkus
    override fun getId(): String = ID
    override fun getConfigurationFactories() = arrayOf(ContinuousTestConfigurationFactory(this))
    companion object {
        val instance = ContinuousTestConfigurationType()
        const val ID = "QuarkusCT"
    }
}