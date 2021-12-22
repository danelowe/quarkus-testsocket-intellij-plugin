package com.tradetested.quarkus.intellij.testsocket.producer

import com.intellij.execution.junit.AbstractAllInPackageConfigurationProducer
import com.tradetested.quarkus.intellij.testsocket.configuration.ContinuousTestConfigurationFactory
import com.tradetested.quarkus.intellij.testsocket.configuration.ContinuousTestConfigurationType


class AllInPackageConfigurationProducer : AbstractAllInPackageConfigurationProducer() {
    override fun getConfigurationFactory() = ContinuousTestConfigurationFactory(ContinuousTestConfigurationType())
}