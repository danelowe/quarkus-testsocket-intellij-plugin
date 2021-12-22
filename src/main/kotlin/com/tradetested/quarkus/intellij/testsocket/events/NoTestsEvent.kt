package com.tradetested.quarkus.intellij.testsocket.events

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
@SerialName("noTests")
class NoTestsEvent(
    val duration: Long,
) : TestEvent()