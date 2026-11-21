package com.shahid.quill.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SecurityUpdateWarning
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shahid.quill.app.QuillApplication
import com.shahid.quill.app.data.model.VersionInfo
import com.shahid.quill.app.service.DownloadStatus
import com.shahid.quill.app.service.UpdateDownloadService
import com.shahid.quill.app.ui.theme.*

@Composable
fun ForceUpdateScreen(
    versionInfo: VersionInfo,
    onRetry: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as QuillApplication
    val downloadStatus by UpdateDownloadService.downloadStatus.collectAsState()

    Scaffold(containerColor = DarkBackground) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.SecurityUpdateWarning,
                contentDescription = null,
                tint = RedError,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Mandatory Update Required",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Quill v${versionInfo.latestVersionName} is required to continue. Critical engine updates are included in this release.",
                fontSize = 14.sp,
                color = TextSecondary,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            when (val s = downloadStatus) {
                is DownloadStatus.Downloading -> {
                    LinearProgressIndicator(
                        progress = { s.progressPercent / 100f },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = PurpleAccent
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Downloading update: ${s.progressPercent}%", fontSize = 12.sp, color = CyanAccent)
                }
                is DownloadStatus.Error -> {
                    Text(s.error, color = RedError, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (!app.updateManager.canInstallUnknownApps()) {
                                app.updateManager.openInstallPermissionSettings()
                            } else {
                                UpdateDownloadService.startDownload(
                                    context,
                                    versionInfo.apkUrl,
                                    versionInfo.sha256,
                                    versionInfo.latestVersionName
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PurpleAccent)
                    ) {
                        Text("Retry Download")
                    }
                }
                else -> {
                    Button(
                        onClick = {
                            if (!app.updateManager.canInstallUnknownApps()) {
                                app.updateManager.openInstallPermissionSettings()
                            } else {
                                UpdateDownloadService.startDownload(
                                    context,
                                    versionInfo.apkUrl,
                                    versionInfo.sha256,
                                    versionInfo.latestVersionName
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PurpleAccent)
                    ) {
                        Icon(Icons.Default.SystemUpdate, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Update Now", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun OptionalUpdateDialog(
    versionInfo: VersionInfo,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as QuillApplication
    val downloadStatus by UpdateDownloadService.downloadStatus.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Quill v${versionInfo.latestVersionName} Available", color = TextPrimary) },
        text = {
            Column {
                Text("A new release is available with new features and improvements:", color = TextSecondary, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(modifier = Modifier.heightIn(max = 140.dp)) {
                    items(versionInfo.changelog) { log ->
                        Text("• $log", fontSize = 12.sp, color = TextPrimary, modifier = Modifier.padding(vertical = 2.dp))
                    }
                }

                if (downloadStatus is DownloadStatus.Downloading) {
                    val p = (downloadStatus as DownloadStatus.Downloading).progressPercent
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { p / 100f },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Downloading... $p%", fontSize = 11.sp, color = CyanAccent)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (!app.updateManager.canInstallUnknownApps()) {
                        app.updateManager.openInstallPermissionSettings()
                    } else {
                        UpdateDownloadService.startDownload(
                            context,
                            versionInfo.apkUrl,
                            versionInfo.sha256,
                            versionInfo.latestVersionName
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PurpleAccent)
            ) {
                Text("Download & Install")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Later", color = TextMuted)
            }
        },
        containerColor = DarkSurface
    )
}
