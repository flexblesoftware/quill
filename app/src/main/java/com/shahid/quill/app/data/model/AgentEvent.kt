package com.shahid.quill.app.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class AgentEvent(
    val type: String = "unknown",
    val stage: String? = null,
    val content: String? = null,
    val tool: String? = null,
    val input: JsonElement? = null,
    val output: JsonElement? = null,
    val error: String? = null,
    val rawText: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
