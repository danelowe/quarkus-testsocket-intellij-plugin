package com.tradetested.quarkus.intellij.testsocket.producer

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.junit.AbstractAllInDirectoryConfigurationProducer
import com.intellij.execution.junit.JUnitConfiguration
import com.intellij.openapi.util.Ref
import com.intellij.psi.JavaDirectoryService
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.tradetested.quarkus.intellij.testsocket.configuration.ContinuousTestConfigurationFactory
import com.tradetested.quarkus.intellij.testsocket.configuration.ContinuousTestConfigurationType

class AllInDirectoryConfigurationProducer : AbstractAllInDirectoryConfigurationProducer() {
    override fun getConfigurationFactory() = ContinuousTestConfigurationFactory(ContinuousTestConfigurationType.instance)
    override fun setupConfigurationFromContext(
        configuration: JUnitConfiguration,
        context: ConfigurationContext,
        sourceElement: Ref<PsiElement>
    ): Boolean {
        val result = super.setupConfigurationFromContext(configuration, context, sourceElement)
        if (result && (context.location is PsiDirectory)) {
            configuration.persistentData.PACKAGE_NAME =
                JavaDirectoryService.getInstance().getPackage(context.location as PsiDirectory)?.name ?: ""
        }
        return result
    }
}