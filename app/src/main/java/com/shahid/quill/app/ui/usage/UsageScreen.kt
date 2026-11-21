package com.shahid.quill.app.ui.usage

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shahid.quill.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsageScreen(
    viewModel: UsageViewModel,
    onOpenDrawer: () -> Unit
) {
    val usage by viewModel.usage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMsg by viewModel.errorMessage.collectAsState()
    val dateFormat = SimpleDateFormat("h:mm:ss a, MMM d", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Usage & Quota", fontWeight = FontWeight.Bold, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshUsage() }, enabled = !isLoading) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = PurpleAccentLight)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Live Account Telemetry", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextPrimary)
                        Text(
                            text = if (usage != null) "Last Refreshed: ${dateFormat.format(Date(usage!!.lastRefreshedMillis))}" else "Awaiting status...",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = PurpleAccentLight)
                    }
                }
            }

            Text("Quota Windows", fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 16.sp)

            QuotaCard(
                title = "Weekly Quota Window",
                remainingPercent = usage?.weeklyQuota?.remainingPercent,
                resetCountdown = usage?.weeklyQuota?.resetCountdown ?: "Not available from agy"
            )

            QuotaCard(
                title = "5-Hour Quota Window",
                remainingPercent = usage?.fiveHourQuota?.remainingPercent,
                resetCountdown = usage?.fiveHourQuota?.resetCountdown ?: "Not available from agy"
            )

            Text("Token Metrics", fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 16.sp)

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricRow(
                        label = "Tokens Per Turn",
                        value = usage?.tokensPerTurn?.toString() ?: "Not available from agy"
                    )
                    HorizontalDivider(color = DarkBorder)
                    MetricRow(
                        label = "Session Total Tokens",
                        value = usage?.totalTokens?.toString() ?: "Not available from agy"
                    )
                }
            }

            if (!usage?.rawJson.isNullOrBlank()) {
                Text("Raw JSON Telemetry", fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 14.sp)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
                ) {
                    Text(
                        text = usage!!.rawJson!!,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun QuotaCard(
    title: String,
    remainingPercent: Float?,
    resetCountdown: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkBorder))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextPrimary)
            Spacer(modifier = Modifier.height(10.dp))

            if (remainingPercent != null) {
                LinearProgressIndicator(
                    progress = { remainingPercent / 100f },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = if (remainingPercent > 20) PurpleAccent else RedError,
                    trackColor = DarkSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Remaining: ${remainingPercent.toInt()}%", fontSize = 13.sp, color = CyanAccent)
                    Text("Resets in: $resetCountdown", fontSize = 12.sp, color = TextMuted)
                }
            } else {
                Text(
                    text = "Remaining: Not available from agy",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Reset Countdown: $resetCountdown",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
fun MetricRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextSecondary, fontSize = 14.sp)
        Text(value, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}
