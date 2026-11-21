package com.shahid.quill.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shahid.quill.app.ui.theme.*

data class NavigationDrawerItemData(
    val route: String,
    val label: String,
    val icon: ImageVector
)

@Composable
fun QuillNavigationDrawerContent(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onClose: () -> Unit
) {
    val items = listOf(
        NavigationDrawerItemData("images", "Image Studio", Icons.Default.Image),
        NavigationDrawerItemData("chat/new", "Chat Assistant", Icons.Default.Chat),
        NavigationDrawerItemData("code", "Code Studio", Icons.Default.Code),
        NavigationDrawerItemData("terminal", "Linux Terminal", Icons.Default.Terminal),
        NavigationDrawerItemData("models", "Models & Engines", Icons.Default.AutoAwesome),
        NavigationDrawerItemData("usage", "Usage & Quota", Icons.Default.QueryStats),
        NavigationDrawerItemData("permissions", "Permissions & Policy", Icons.Default.Security),
        NavigationDrawerItemData("settings", "Settings", Icons.Default.Settings)
    )

    ModalDrawerSheet(
        drawerContainerColor = DarkSurface,
        modifier = Modifier.width(280.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(PurpleAccent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.BorderColor, contentDescription = null, tint = TextPrimary)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Quill", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                    Text("AI Workspace for Android", fontSize = 11.sp, color = TextMuted)
                }
            }

            Divider(color = DarkBorder, modifier = Modifier.padding(bottom = 12.dp))

            items.forEach { item ->
                val isSelected = currentRoute.startsWith(item.route.substringBefore("/"))
                NavigationDrawerItem(
                    label = { Text(item.label, fontSize = 14.sp) },
                    selected = isSelected,
                    onClick = {
                        onNavigate(item.route)
                        onClose()
                    },
                    icon = { Icon(item.icon, contentDescription = null) },
                    shape = RoundedCornerShape(10.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = PurpleAccentDark,
                        unselectedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                        selectedIconColor = TextPrimary,
                        unselectedIconColor = TextSecondary,
                        selectedTextColor = TextPrimary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Built for MD Shahid",
                fontSize = 11.sp,
                color = TextMuted,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}
