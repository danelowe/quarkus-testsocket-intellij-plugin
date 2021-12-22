package com.tradetested.quarkus.intellij.testsocket.producer

import com.intellij.execution.testframework.AbstractInClassConfigurationProducer
import com.tradetested.quarkus.intellij.testsocket.configuration.ContinuousTestConfigurationFactory
import com.tradetested.quarkus.intellij.testsocket.configuration.ContinuousTestConfigurationType
import com.tradetested.quarkus.intellij.testsocket.configuration.ContinuousTestRunConfiguration

class TestInClassConfigurationProducer : AbstractInClassConfigurationProducer<ContinuousTestRunConfiguration>() {
    override fun getConfigurationFactory() = ContinuousTestConfigurationFactory(ContinuousTestConfigurationType())
}