package com.shahid.quill.app.ui.usage

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shahid.quill.app.QuillApplication
import com.shahid.quill.app.data.local.entity.UsageCacheEntity
import com.shahid.quill.app.data.model.AgyUsage
import com.shahid.quill.app.data.model.QuotaWindow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class UsageViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as QuillApplication
    private val prootRunner = app.prootRunner
    private val usageCacheDao = app.database.usageCacheDao()

    private val _usage = MutableStateFlow<AgyUsage?>(null)
    val usage: StateFlow<AgyUsage?> = _usage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    init {
        viewModelScope.launch {
            usageCacheDao.getUsageCache().collect { cache ->
                if (cache != null && _usage.value == null) {
                    _usage.value = AgyUsage(
                        weeklyQuota = cache.weeklyRemainingPercent?.let {
                            QuotaWindow("Weekly Window", it, cache.weeklyResetCountdown ?: "Not available from agy")
                        },
                        fiveHourQuota = cache.fiveHourRemainingPercent?.let {
                            QuotaWindow("5-Hour Window", it, cache.fiveHourResetCountdown ?: "Not available from agy")
                        },
                        tokensPerTurn = cache.tokensPerTurn,
                        totalTokens = cache.totalTokens,
                        lastRefreshedMillis = cache.lastRefreshed,
                        rawJson = cache.rawJson
                    )
                }
            }
        }

        refreshUsage()

        viewModelScope.launch {
            while (isActive) {
                delay(15 * 60 * 1000L)
                refreshUsage()
            }
        }
    }

    fun refreshUsage() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = prootRunner.fetchUsage()
            if (result.exitCode == 0 && result.stdout.isNotBlank()) {
                val parsed = parseUsageJson(result.stdout)
                _usage.value = parsed

                usageCacheDao.saveUsageCache(
                    UsageCacheEntity(
                        id = 1,
                        weeklyRemainingPercent = parsed.weeklyQuota?.remainingPercent,
                        weeklyResetCountdown = parsed.weeklyQuota?.resetCountdown,
                        fiveHourRemainingPercent = parsed.fiveHourQuota?.remainingPercent,
                        fiveHourResetCountdown = parsed.fiveHourQuota?.resetCountdown,
                        tokensPerTurn = parsed.tokensPerTurn,
                        totalTokens = parsed.totalTokens,
                        lastRefreshed = parsed.lastRefreshedMillis,
                        rawJson = result.stdout
                    )
                )
            } else {
                _errorMessage.value = if (result.stderr.isNotBlank()) result.stderr else "Not available from agy"
            }
            _isLoading.value = false
        }
    }

    private fun parseUsageJson(raw: String): AgyUsage {
        return try {
            val element = json.parseToJsonElement(raw).jsonObject

            val weekly = element["weekly"]?.jsonObject?.let {
                QuotaWindow(
                    title = "Weekly Window",
                    remainingPercent = it["remaining_percent"]?.jsonPrimitive?.floatOrNull,
                    resetCountdown = it["reset_countdown"]?.jsonPrimitive?.contentOrNull ?: "Not available from agy"
                )
            }

            val fiveHour = element["five_hour"]?.jsonObject?.let {
                QuotaWindow(
                    title = "5-Hour Window",
                    remainingPercent = it["remaining_percent"]?.jsonPrimitive?.floatOrNull,
                    resetCountdown = it["reset_countdown"]?.jsonPrimitive?.contentOrNull ?: "Not available from agy"
                )
            }

            val tokensPerTurn = element["tokens_per_turn"]?.jsonPrimitive?.intOrNull
            val totalTokens = element["total_tokens"]?.jsonPrimitive?.intOrNull

            AgyUsage(
                weeklyQuota = weekly,
                fiveHourQuota = fiveHour,
                tokensPerTurn = tokensPerTurn,
                totalTokens = totalTokens,
                lastRefreshedMillis = System.currentTimeMillis(),
                rawJson = raw
            )
        } catch (e: Exception) {
            AgyUsage(
                lastRefreshedMillis = System.currentTimeMillis(),
                rawJson = raw
            )
        }
    }
}
