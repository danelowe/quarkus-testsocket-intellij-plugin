package com.tradetested.quarkus.intellij.testsocket.events

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
@SerialName("testStarted")
data class TestStartedEvent(
    val id: String,
    val name: String,
    val parentId: String? = null,
    val className: String? = null,
    val methodName: String? = null,
) : TestEvent()