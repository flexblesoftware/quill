package com.shahid.quill.app.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shahid.quill.app.QuillApplication
import com.shahid.quill.app.domain.linux.SetupProgress
import com.shahid.quill.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun SetupScreen(
    onSetupComplete: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as QuillApplication
    val scope = rememberCoroutineScope()

    var progressState by remember { mutableStateOf<SetupProgress>(SetupProgress.InProgress("Initializing installer...", 0)) }

    fun startInstallation() {
        scope.launch {
            app.environmentManager.runSetup().collect { prog ->
                progressState = prog
                if (prog is SetupProgress.Completed) {
                    app.preferences.setSetupCompleted(true)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        startInstallation()
    }

    Scaffold(containerColor = DarkBackground) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Linux Environment Setup",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Configuring glibc rootfs and Google Antigravity CLI",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    when (val s = progressState) {
                        is SetupProgress.InProgress -> {
                            LinearProgressIndicator(
                                progress = { s.percent / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp),
                                color = PurpleAccent,
                                trackColor = DarkSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "${s.percent}% • ${s.step}",
                                fontSize = 13.sp,
                                color = CyanAccent,
                                textAlign = TextAlign.Center
                            )

                            if (s.speedText != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(s.speedText, fontSize = 11.sp, color = TextMuted)
                            }
                        }
                        is SetupProgress.Completed -> {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = GreenSuccess,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(s.message, fontSize = 14.sp, color = GreenSuccess, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = onSetupComplete,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PurpleAccent)
                            ) {
                                Text("Enter Workspace", fontWeight = FontWeight.Bold)
                            }
                        }
                        is SetupProgress.Failed -> {
                            Icon(
                                Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = RedError,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(s.error, fontSize = 13.sp, color = RedError, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = { startInstallation() },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PurpleAccent)
                            ) {
                                Text("Retry Installation")
                            }
                        }
                    }
                }
            }
        }
    }
}
