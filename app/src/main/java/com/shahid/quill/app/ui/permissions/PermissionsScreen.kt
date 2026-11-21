package com.shahid.quill.app.ui.permissions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shahid.quill.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsScreen(
    viewModel: PermissionsViewModel,
    onOpenDrawer: () -> Unit
) {
    val agentMode by viewModel.agentMode.collectAsState(initial = "auto")
    val permFileWrite by viewModel.permFileWrite.collectAsState(initial = "ALLOW")
    val permCommand by viewModel.permCommand.collectAsState(initial = "ALLOW")
    val permNetwork by viewModel.permNetwork.collectAsState(initial = "ALLOW")
    val permImageGen by viewModel.permImageGen.collectAsState(initial = "ALLOW")
    val logs by viewModel.recentLogs.collectAsState()

    val agentModes = listOf("auto", "default", "accept-edits", "plan")
    val dateFormat = SimpleDateFormat("h:mm:ss a", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Permissions & Policy", fontWeight = FontWeight.Bold, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.clearLogs() }) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Clear Log", tint = TextMuted)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Autonomous Execution Mode", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextPrimary)
                        Text("Controls agent autonomy level when running tools and making modifications.", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            agentModes.forEach { mode ->
                                val isSelected = mode.equals(agentMode, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) PurpleAccent else DarkSurfaceVariant)
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) PurpleAccentLight else DarkBorder,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { viewModel.setAgentMode(mode) }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = mode.uppercase(),
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text("Granular Tool Permissions", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = TextPrimary)
            }

            item {
                PermissionItemCard(
                    toolName = "File Write Access",
                    description = "Allow the agent to create and modify files in workspace storage",
                    currentValue = permFileWrite,
                    onSelect = { viewModel.setPermission("file_write", it) }
                )
            }

            item {
                PermissionItemCard(
                    toolName = "Terminal Execution",
                    description = "Allow the agent to execute shell commands inside Linux container",
                    currentValue = permCommand,
                    onSelect = { viewModel.setPermission("command", it) }
                )
            }

            item {
                PermissionItemCard(
                    toolName = "Network Fetch",
                    description = "Allow the agent to download packages and access remote URLs",
                    currentValue = permNetwork,
                    onSelect = { viewModel.setPermission("network", it) }
                )
            }

            item {
                PermissionItemCard(
                    toolName = "Image Generation",
                    description = "Allow the agent to invoke diffusion image generation tools",
                    currentValue = permImageGen,
                    onSelect = { viewModel.setPermission("image_gen", it) }
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Execution & Permission Audit Log", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = TextPrimary)
                    Text("${logs.size} entries", fontSize = 12.sp, color = TextMuted)
                }
            }

            if (logs.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface)
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            Text("No permission events recorded yet.", color = TextMuted, fontSize = 13.sp)
                        }
                    }
                }
            } else {
                items(logs) { log ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        when (log.decision) {
                                            "ALLOW" -> GreenSuccess.copy(alpha = 0.2f)
                                            "ASK" -> AmberWarning.copy(alpha = 0.2f)
                                            else -> RedError.copy(alpha = 0.2f)
                                        }
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = log.decision,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (log.decision) {
                                        "ALLOW" -> GreenSuccess
                                        "ASK" -> AmberWarning
                                        else -> RedError
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(log.toolName, fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 13.sp)
                                Text(log.details ?: log.target, fontSize = 11.sp, color = TextMuted)
                            }
                            Text(dateFormat.format(Date(log.timestamp)), fontSize = 11.sp, color = TextMuted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionItemCard(
    toolName: String,
    description: String,
    currentValue: String,
    onSelect: (String) -> Unit
) {
    val options = listOf("ALLOW", "ASK", "DENY")

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(toolName, fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 14.sp)
            Text(description, color = TextSecondary, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                options.forEach { opt ->
                    val isSelected = opt.equals(currentValue, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isSelected) {
                                    when (opt) {
                                        "ALLOW" -> GreenSuccess
                                        "ASK" -> AmberWarning
                                        else -> RedError
                                    }
                                } else DarkSurfaceVariant
                            )
                            .clickable { onSelect(opt) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = opt,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else TextSecondary
                        )
                    }
                }
            }
        }
    }
}
