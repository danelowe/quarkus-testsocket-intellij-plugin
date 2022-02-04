package com.tradetested.quarkus.intellij.testsocket.producer

import com.intellij.execution.CommonJavaRunConfigurationParameters
import com.intellij.execution.JavaExecutionUtil
import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.junit.AbstractAllInPackageConfigurationProducer
import com.intellij.execution.junit.JUnitConfiguration
import com.intellij.openapi.module.Module
import com.intellij.openapi.util.Comparing
import com.intellij.psi.JavaDirectoryService
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.tradetested.quarkus.intellij.testsocket.configuration.ContinuousTestConfigurationFactory
import com.tradetested.quarkus.intellij.testsocket.configuration.ContinuousTestConfigurationType
import org.jetbrains.kotlin.idea.intentions.loopToCallChain.match


class AllInPackageConfigurationProducer : AbstractAllInPackageConfigurationProducer() {
    override fun getConfigurationFactory() =
        ContinuousTestConfigurationFactory(ContinuousTestConfigurationType.instance)

    /**
     * Override to allow recognising _test all_ as a duplicate
     */
    override fun isConfigurationFromContext(configuration: JUnitConfiguration, context: ConfigurationContext): Boolean {
        if (isMultipleElementsSelected(context)) {
            return false
        }
        val predefinedConfiguration = context.getOriginalConfiguration(
            configurationType
        )
        val contextLocation = context.location ?: return false
        val location = JavaExecutionUtil.stepIntoSingleClass(contextLocation) ?: return false
        val element = location.psiElement
        val template = context.runManager.getConfigurationTemplate(configurationFactory)
        val templateConfiguration: JUnitConfiguration = template.configuration as JUnitConfiguration
        val predefinedModule: Module? = templateConfiguration.getConfigurationModule().getModule()
        val vmParameters: String?
        vmParameters = if (predefinedConfiguration != null) {
            if (predefinedConfiguration is CommonJavaRunConfigurationParameters) (predefinedConfiguration as CommonJavaRunConfigurationParameters).vmParameters else null
        } else {
            templateConfiguration.getVMParameters()
        }
        if (!Comparing.strEqual(vmParameters, configuration.getVMParameters())) return false
        if (differentParamSet(configuration, contextLocation)) return false
        if (!isApplicableTestType(configuration.getTestType(), context)) return false
        val psiClass = PsiTreeUtil.getParentOfType(element, PsiClass::class.java)
        if (psiClass != null && getCurrentFramework(psiClass) == null) return false

        // [dane] This line is changed to prevent duplicating _all tests_ configurations.
        if (configuration.isConfiguredByElement(element) || matchesTestAll(configuration, element)) {
            val configurationModule: Module? = configuration.getConfigurationModule().getModule()
            val locationModule = location.module
            if (Comparing.equal(locationModule, configurationModule)) return true
            if ((predefinedModule != null || locationModule == null) && Comparing.equal(
                    predefinedModule,
                    configurationModule
                )
            ) return true
        }
        return false
    }

    private fun matchesTestAll(configuration: JUnitConfiguration, element: PsiElement) =
        (configuration.persistentData.TEST_OBJECT == JUnitConfiguration.TEST_PACKAGE)
                && (configuration.persistentData.packageName == "")
                && (element is PsiDirectory)
                && (JavaDirectoryService.getInstance().getPackage(element) == null)
}