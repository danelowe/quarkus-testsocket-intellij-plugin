package com.tradetested.quarkus.intellij.testsocket.events

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
@SerialName("testComplete")
data class TestCompleteEvent(
    val id: String,
    val log: List<String>,
    val duration: Long,
    val status: TestStatus,
    val methodName: String? = null,
    val className: String? = null,
    val details: TestDetails? = null,
    val parentId: String,
) : TestEvent()

@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
enum class TestStatus {
    @SerialName("successful")
    SUCCESSFUL,
    @SerialName("aborted")
    ABORTED,
    @SerialName("failed")
    FAILED,
}

@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
enum class TestDetailsType {
    @SerialName("error")
    ERROR,
    @SerialName("comparison")
    COMPARISON,
}

@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class TestDetails(
    val message: String,
    val type: TestDetailsType,
    val expected: String = "",
    val actual: String = "",
    val stackTrace: String = "",
)