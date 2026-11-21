package com.shahid.quill.app.ui.settings

import android.widget.Toast
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shahid.quill.app.domain.updates.UpdateCheckResult
import com.shahid.quill.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onOpenDrawer: () -> Unit,
    onLaunchTerminalForLogin: () -> Unit
) {
    val context = LocalContext.current
    val agyVersion by viewModel.agyVersion.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val isCheckingUpdate by viewModel.isCheckingUpdate.collectAsState()
    val updateResult by viewModel.updateCheckResult.collectAsState()
    val statusMsg by viewModel.statusMessage.collectAsState()

    var showClearDataConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(statusMsg) {
        if (statusMsg != null) {
            Toast.makeText(context, statusMsg, Toast.LENGTH_SHORT).show()
            viewModel.clearStatusMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings & Environment", fontWeight = FontWeight.Bold, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = TextPrimary)
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
            // Environment & Antigravity Info Card
            Text("Antigravity Core", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = TextPrimary)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Installed agy Version", color = TextSecondary, fontSize = 14.sp)
                        Text(agyVersion, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    HorizontalDivider(color = DarkBorder)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Google OAuth Status", color = TextSecondary, fontSize = 14.sp)
                        Text(
                            text = if (isLoggedIn) "Authenticated" else "Not Authenticated",
                            color = if (isLoggedIn) GreenSuccess else RedError,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (!isLoggedIn) {
                        Button(
                            onClick = onLaunchTerminalForLogin,
                            colors = ButtonDefaults.buttonColors(containerColor = PurpleAccent),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Log In via Terminal Session")
                        }
                    } else {
                        OutlinedButton(
                            onClick = { viewModel.logout() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RedError),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Log Out from Antigravity")
                        }
                    }
                }
            }

            // Updates Section
            Text("Updates & Releases", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = TextPrimary)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Installed Version", color = TextSecondary, fontSize = 14.sp)
                        Text("v${viewModel.currentVersionName} (${viewModel.currentVersionCode})", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }

                    Button(
                        onClick = { viewModel.checkForUpdates() },
                        enabled = !isCheckingUpdate,
                        colors = ButtonDefaults.buttonColors(containerColor = PurpleAccent),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isCheckingUpdate) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Checking GitHub Releases...")
                        } else {
                            Icon(Icons.Default.SystemUpdateAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Check for Update")
                        }
                    }

                    if (updateResult != null) {
                        when (val r = updateResult!!) {
                            is UpdateCheckResult.UpToDate -> {
                                Text("Quill is up to date!", color = GreenSuccess, fontSize = 13.sp)
                            }
                            is UpdateCheckResult.OptionalUpdate -> {
                                Text("New version available: v${r.versionInfo.latestVersionName}", color = CyanAccent, fontSize = 13.sp)
                            }
                            is UpdateCheckResult.ForceUpdate -> {
                                Text("Required update: v${r.versionInfo.latestVersionName}", color = RedError, fontSize = 13.sp)
                            }
                            is UpdateCheckResult.Error -> {
                                Text("Update check failed: ${r.message}", color = RedError, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // Data & Storage Management
            Text("Data & Storage", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = TextPrimary)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = { viewModel.exportData() },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export All Local Data (JSON)", color = TextPrimary)
                    }

                    OutlinedButton(
                        onClick = { showClearDataConfirm = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RedError),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Clear All Data & Cache")
                    }
                }
            }

            // App Identity & Credits
            Text("About Quill", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = TextPrimary)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Developer: MD Shahid", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                    Text("Package: com.shahid.quill.app", color = TextMuted, fontSize = 12.sp)
                    Text("Engine: Google Antigravity CLI via Linux PRoot", color = TextMuted, fontSize = 12.sp)
                }
            }
        }
    }

    if (showClearDataConfirm) {
        AlertDialog(
            onDismissRequest = { showClearDataConfirm = false },
            title = { Text("Clear All Data?", color = TextPrimary) },
            text = { Text("This will permanently remove all local conversations, image metadata, and cached settings.", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                        showClearDataConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedError)
                ) {
                    Text("Confirm Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataConfirm = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = DarkSurface
        )
    }
}
