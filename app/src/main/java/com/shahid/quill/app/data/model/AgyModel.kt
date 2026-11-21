package com.shahid.quill.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AgyModel(
    val id: String,
    val name: String,
    val description: String? = null,
    val capability: String = "Not provided by agy",
    val contextWindow: String? = null,
    val isDefault: Boolean = false
)
