package com.tradetested.quarkus.intellij.testsocket.events

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
@SerialName("state")
data class StateEvent(
    val started: Boolean,
    val brokenOnly: Boolean,
    val liveReload: Boolean,
) : TestEvent()