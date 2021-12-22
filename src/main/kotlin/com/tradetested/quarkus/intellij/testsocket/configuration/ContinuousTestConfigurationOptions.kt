package com.tradetested.quarkus.intellij.testsocket.configuration

import com.intellij.execution.configurations.ModuleBasedConfigurationOptions

class ContinuousTestConfigurationOptions : ModuleBasedConfigurationOptions() {
    private val myEndpoint = string(DEFAULT_ENDPOINT)
        .provideDelegate(this, "endpoint")
    var endpoint: String
        get() = myEndpoint.getValue(this) ?: DEFAULT_ENDPOINT
        set(value) = myEndpoint.setValue(this, value)

    companion object {
        const val DEFAULT_ENDPOINT = "ws://localhost:8080/q/dev/com.tradetested.testsocket"
    }
}