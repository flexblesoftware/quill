package com.shahid.quill.app.ui.code

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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shahid.quill.app.ui.theme.*
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeWorkspaceScreen(
    viewModel: CodeViewModel,
    onOpenDrawer: () -> Unit,
    onRunInTerminal: (String) -> Unit
) {
    val projects by viewModel.projects.collectAsState()
    val selectedProject by viewModel.selectedProject.collectAsState()
    val currentDirectory by viewModel.currentDirectory.collectAsState()
    val directoryItems by viewModel.directoryItems.collectAsState()
    val selectedFile by viewModel.selectedFile.collectAsState()
    val fileContent by viewModel.fileContent.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var editedContent by remember { mutableStateOf("") }

    LaunchedEffect(fileContent) {
        editedContent = fileContent
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (selectedProject != null) selectedProject!!.name else "Code Studio",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (selectedFile != null) selectedFile!!.name else "AI Project Builder & Terminal",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    if (selectedFile != null) {
                        IconButton(onClick = { viewModel.closeFile() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                        }
                    } else if (selectedProject != null) {
                        IconButton(onClick = { viewModel.closeProject() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                        }
                    } else {
                        IconButton(onClick = onOpenDrawer) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = TextPrimary)
                        }
                    }
                },
                actions = {
                    if (selectedProject != null) {
                        IconButton(onClick = {
                            onRunInTerminal("cd ${selectedProject!!.absolutePath}")
                        }) {
                            Icon(Icons.Default.Terminal, contentDescription = "Run in Terminal", tint = PurpleAccentLight)
                        }
                    }
                    if (selectedFile != null) {
                        IconButton(onClick = { viewModel.saveCurrentFile(editedContent) }) {
                            Icon(Icons.Default.Save, contentDescription = "Save", tint = GreenSuccess)
                        }
                    } else {
                        IconButton(onClick = { showCreateDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "New Project", tint = PurpleAccentLight)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                selectedFile != null -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Surface(
                            color = DarkSurface,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedFile!!.name,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    color = CyanAccent
                                )
                                Button(
                                    onClick = { viewModel.saveCurrentFile(editedContent) },
                                    colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Save Changes", fontSize = 12.sp)
                                }
                            }
                        }

                        OutlinedTextField(
                            value = editedContent,
                            onValueChange = { editedContent = it },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                color = TextPrimary
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = DarkSurface,
                                unfocusedContainerColor = DarkSurface,
                                focusedBorderColor = PurpleAccent,
                                unfocusedBorderColor = DarkBorder
                            )
                        )
                    }
                }
                selectedProject != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { viewModel.navigateUp() }) {
                                Icon(Icons.Default.ArrowUpward, contentDescription = "Up", tint = TextPrimary)
                            }
                            Text(
                                text = currentDirectory?.name ?: "Root",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PurpleAccentLight
                            )
                        }

                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(directoryItems) { item ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.selectFile(item) },
                                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            if (item.isDirectory) Icons.Default.Folder else Icons.Default.InsertDriveFile,
                                            contentDescription = null,
                                            tint = if (item.isDirectory) AmberWarning else CyanAccent
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(item.name, color = TextPrimary, fontSize = 14.sp)
                                            if (!item.isDirectory) {
                                                Text("${item.sizeBytes} bytes", color = TextMuted, fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("AI Project Generator", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Describe the application or script you want to build. Antigravity will generate full code inside app storage.", color = TextSecondary, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { showCreateDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = PurpleAccent),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("New AI Project")
                                }
                            }
                        }

                        Text("Existing Projects", fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        if (projects.isEmpty()) {
                            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Text("No projects created yet.", color = TextMuted)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(projects) { project ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { viewModel.selectProject(project) },
                                        colors = CardDefaults.cardColors(containerColor = DarkSurface)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.FolderSpecial, contentDescription = null, tint = PurpleAccentLight)
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(project.name, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                                Text(project.absolutePath, fontSize = 11.sp, color = TextMuted, maxLines = 1)
                                            }
                                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (showCreateDialog) {
                var name by remember { mutableStateOf("") }
                var desc by remember { mutableStateOf("") }

                AlertDialog(
                    onDismissRequest = { if (!isGenerating) showCreateDialog = false },
                    title = { Text("Generate Project with AI", color = TextPrimary) },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Project Name (e.g. MyNodeApp)") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = desc,
                                onValueChange = { desc = it },
                                label = { Text("Requirements / Prompt") },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 4,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )

                            if (isGenerating) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = PurpleAccentLight)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Generating project files...", fontSize = 12.sp, color = PurpleAccentLight)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.newProjectName.value = name
                                viewModel.prompt.value = desc
                                viewModel.createProjectFromPrompt()
                                showCreateDialog = false
                            },
                            enabled = name.isNotBlank() && desc.isNotBlank() && !isGenerating,
                            colors = ButtonDefaults.buttonColors(containerColor = PurpleAccent)
                        ) {
                            Text("Create")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showCreateDialog = false }) {
                            Text("Cancel", color = TextMuted)
                        }
                    },
                    containerColor = DarkSurface
                )
            }
        }
    }
}
