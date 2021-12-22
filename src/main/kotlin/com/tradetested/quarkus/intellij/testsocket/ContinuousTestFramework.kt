package com.tradetested.quarkus.intellij.testsocket

import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.junit.JUnit5Framework
import com.intellij.psi.PsiClass
import com.tradetested.quarkus.intellij.testsocket.configuration.ContinuousTestConfigurationType

class ContinuousTestFramework : JUnit5Framework() {
    override fun getName(): String = "Quarkus CT"
    override fun isTestClass(clazz: PsiClass, canBePotential: Boolean): Boolean =
        clazz.annotations.any { it.hasQualifiedName("io.quarkus.test.junit.QuarkusTest") }

    override fun isMyConfigurationType(type: ConfigurationType): Boolean {
        return type.id == ContinuousTestConfigurationType.ID
    }
}