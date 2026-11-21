package com.shahid.quill.app.ui.models

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shahid.quill.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelsScreen(
    viewModel: ModelsViewModel,
    onOpenDrawer: () -> Unit
) {
    val models by viewModel.models.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMsg by viewModel.errorMessage.collectAsState()
    val activeModel by viewModel.activeModel.collectAsState(initial = "default")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Models & Runtimes", fontWeight = FontWeight.Bold, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadModels() }, enabled = !isLoading) {
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
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Antigravity Model Engine", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = PurpleAccentLight)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Active model is applied to Chat, Image Generation prompts, and Code modifications.", fontSize = 12.sp, color = TextSecondary)
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PurpleAccent)
                }
            } else if (errorMsg != null && models.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(errorMsg!!, color = RedError, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(onClick = { viewModel.loadModels() }) {
                            Text("Retry")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(models) { model ->
                        val isSelected = model.id == activeModel
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setActiveModel(model.id) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) DarkSurfaceContainer else DarkSurface
                            ),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(if (isSelected) PurpleAccent else DarkBorder)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { viewModel.setActiveModel(model.id) },
                                    colors = RadioButtonDefaults.colors(selectedColor = PurpleAccent)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = model.name,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "ID: ${model.id}",
                                        fontSize = 12.sp,
                                        color = TextMuted
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Surface(
                                        color = DarkSurfaceVariant,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "Capability: ${model.capability}",
                                            fontSize = 11.sp,
                                            color = CyanAccent,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
