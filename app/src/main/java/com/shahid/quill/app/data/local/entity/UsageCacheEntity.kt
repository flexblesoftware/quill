package com.shahid.quill.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usage_cache")
data class UsageCacheEntity(
    @PrimaryKey val id: Int = 1,
    val weeklyRemainingPercent: Float? = null,
    val weeklyResetCountdown: String? = null,
    val fiveHourRemainingPercent: Float? = null,
    val fiveHourResetCountdown: String? = null,
    val tokensPerTurn: Int? = null,
    val totalTokens: Int? = null,
    val lastRefreshed: Long = System.currentTimeMillis(),
    val rawJson: String? = null
)
