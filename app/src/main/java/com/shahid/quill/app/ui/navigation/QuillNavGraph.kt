package com.shahid.quill.app.ui.navigation

import androidx.compose.material3.DrawerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.shahid.quill.app.ui.chat.ChatScreen
import com.shahid.quill.app.ui.chat.ChatViewModel
import com.shahid.quill.app.ui.chat.ConversationListScreen
import com.shahid.quill.app.ui.code.CodeViewModel
import com.shahid.quill.app.ui.code.CodeWorkspaceScreen
import com.shahid.quill.app.ui.images.ImageViewModel
import com.shahid.quill.app.ui.images.ImageWorkspaceScreen
import com.shahid.quill.app.ui.models.ModelsScreen
import com.shahid.quill.app.ui.models.ModelsViewModel
import com.shahid.quill.app.ui.onboarding.OnboardingScreen
import com.shahid.quill.app.ui.onboarding.SetupScreen
import com.shahid.quill.app.ui.permissions.PermissionsScreen
import com.shahid.quill.app.ui.permissions.PermissionsViewModel
import com.shahid.quill.app.ui.settings.SettingsScreen
import com.shahid.quill.app.ui.settings.SettingsViewModel
import com.shahid.quill.app.ui.terminal.TerminalScreen
import com.shahid.quill.app.ui.usage.UsageScreen
import com.shahid.quill.app.ui.usage.UsageViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun QuillNavGraph(
    navController: NavHostController,
    startDestination: String,
    drawerState: DrawerState,
    scope: CoroutineScope,
    modifier: Modifier = Modifier
) {
    val openDrawer = { scope.launch { drawerState.open() } }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinished = {
                    navController.navigate(Screen.Setup.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Setup.route) {
            SetupScreen(
                onSetupComplete = {
                    navController.navigate(Screen.Images.route) {
                        popUpTo(Screen.Setup.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Images.route) {
            val vm: ImageViewModel = viewModel()
            ImageWorkspaceScreen(
                viewModel = vm,
                onOpenDrawer = { openDrawer() }
            )
        }

        composable(Screen.Chat.route) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString("conversationId") ?: "new"
            val vm: ChatViewModel = viewModel()
            if (conversationId != "new") {
                vm.selectConversation(conversationId)
            }
            ChatScreen(
                viewModel = vm,
                onOpenDrawer = { openDrawer() },
                onOpenConversationsList = { navController.navigate(Screen.Conversations.route) }
            )
        }

        composable(Screen.Conversations.route) {
            val vm: ChatViewModel = viewModel()
            ConversationListScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onConversationSelected = { id ->
                    navController.navigate(Screen.Chat.createRoute(id)) {
                        popUpTo(Screen.Conversations.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Code.route) {
            val vm: CodeViewModel = viewModel()
            CodeWorkspaceScreen(
                viewModel = vm,
                onOpenDrawer = { openDrawer() },
                onRunInTerminal = { cmd ->
                    navController.navigate(Screen.Terminal.createRoute(cmd))
                }
            )
        }

        composable(Screen.Terminal.route) { backStackEntry ->
            val cmd = backStackEntry.arguments?.getString("cmd") ?: ""
            TerminalScreen(
                initialCommand = cmd.ifBlank { null },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Models.route) {
            val vm: ModelsViewModel = viewModel()
            ModelsScreen(
                viewModel = vm,
                onOpenDrawer = { openDrawer() }
            )
        }

        composable(Screen.Usage.route) {
            val vm: UsageViewModel = viewModel()
            UsageScreen(
                viewModel = vm,
                onOpenDrawer = { openDrawer() }
            )
        }

        composable(Screen.Permissions.route) {
            val vm: PermissionsViewModel = viewModel()
            PermissionsScreen(
                viewModel = vm,
                onOpenDrawer = { openDrawer() }
            )
        }

        composable(Screen.Settings.route) {
            val vm: SettingsViewModel = viewModel()
            SettingsScreen(
                viewModel = vm,
                onOpenDrawer = { openDrawer() },
                onLaunchTerminalForLogin = {
                    navController.navigate(Screen.Terminal.createRoute("agy auth login || agy login"))
                }
            )
        }
    }
}
