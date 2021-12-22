package com.tradetested.quarkus.intellij.testsocket.events

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
@SerialName("runComplete")
data class RunCompleteEvent(
    val duration: Long,
    val counts: RunCounts,
) : TestEvent()

@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class RunCounts(
    val passed: Int,
    val failed: Int,
    val skipped: Int,
    val total: Int,
)