package com.tradetested.quarkus.intellij.testsocket.events

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
@SerialName("runStarted")
data class RunStartedEvent(
    val toRun: Int,
) : TestEvent()