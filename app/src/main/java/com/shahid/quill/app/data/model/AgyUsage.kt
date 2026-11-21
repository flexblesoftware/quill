package com.shahid.quill.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class QuotaWindow(
    val title: String,
    val remainingPercent: Float? = null,
    val resetCountdown: String = "Not available from agy"
)

@Serializable
data class ModelGroupQuota(
    val groupName: String,
    val limit: String = "Not available from agy",
    val used: String = "Not available from agy",
    val remaining: String = "Not available from agy"
)

@Serializable
data class AgyUsage(
    val weeklyQuota: QuotaWindow? = null,
    val fiveHourQuota: QuotaWindow? = null,
    val modelGroupQuotas: List<ModelGroupQuota> = emptyList(),
    val tokensPerTurn: Int? = null,
    val totalTokens: Int? = null,
    val lastRefreshedMillis: Long = System.currentTimeMillis(),
    val rawJson: String? = null
)
