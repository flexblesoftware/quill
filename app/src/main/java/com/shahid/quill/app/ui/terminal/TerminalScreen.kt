package com.shahid.quill.app.ui.terminal

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shahid.quill.app.domain.terminal.TerminalSession
import com.shahid.quill.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalScreen(
    initialCommand: String? = null,
    onBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    val session = remember {
        TerminalSession(context, scope, initialCommand).apply { start() }
    }

    DisposableEffect(Unit) {
        onDispose {
            session.destroy()
        }
    }

    val bufferText by session.buffer.collectAsState()
    val isRunning by session.isRunning.collectAsState()
    var inputCommand by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    LaunchedEffect(bufferText.length) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Linux Shell", fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(if (isRunning) GreenSuccess else RedError)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        clipboardManager.setText(AnnotatedString(bufferText))
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy All", tint = TextPrimary)
                    }
                    IconButton(onClick = { session.clear() }) {
                        Icon(Icons.Default.ClearAll, contentDescription = "Clear", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color.Black)
                    .padding(8.dp)
                    .verticalScroll(scrollState)
            ) {
                Text(
                    text = bufferText.ifEmpty { "Starting Quill Linux environment terminal...\n" },
                    color = Color(0xFF00FF66),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }

            Surface(
                color = DarkSurface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TerminalKeyButton("Ctrl+C") { session.sendCtrl('c') }
                    TerminalKeyButton("Ctrl+D") { session.sendCtrl('d') }
                    TerminalKeyButton("Esc") { session.sendEsc() }
                    TerminalKeyButton("Tab") { session.sendTab() }
                    TerminalKeyButton("↑") { session.sendArrowUp() }
                    TerminalKeyButton("↓") { session.sendArrowDown() }
                    TerminalKeyButton("←") { session.sendArrowLeft() }
                    TerminalKeyButton("→") { session.sendArrowRight() }
                    TerminalKeyButton("Paste") {
                        val clip = clipboardManager.getText()?.text ?: ""
                        if (clip.isNotBlank()) session.write(clip)
                    }
                }
            }

            Surface(
                color = DarkSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("$ ", fontFamily = FontFamily.Monospace, color = PurpleAccentLight, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = inputCommand,
                        onValueChange = { inputCommand = it },
                        placeholder = { Text("type command...", fontSize = 12.sp, color = TextMuted) },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 6.dp),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            color = TextPrimary
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = PurpleAccent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                    IconButton(
                        onClick = {
                            if (inputCommand.isNotBlank()) {
                                session.write(inputCommand + "\n")
                                inputCommand = ""
                            }
                        }
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Run", tint = PurpleAccentLight)
                    }
                }
            }
        }
    }
}

@Composable
fun TerminalKeyButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceContainer),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.height(32.dp)
    ) {
        Text(text, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = TextPrimary)
    }
}
