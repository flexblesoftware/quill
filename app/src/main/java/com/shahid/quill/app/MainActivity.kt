package com.shahid.quill.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.shahid.quill.app.domain.updates.UpdateCheckResult
import com.shahid.quill.app.ui.components.ForceUpdateScreen
import com.shahid.quill.app.ui.components.OptionalUpdateDialog
import com.shahid.quill.app.ui.components.QuillNavigationDrawerContent
import com.shahid.quill.app.ui.navigation.QuillNavGraph
import com.shahid.quill.app.ui.navigation.Screen
import com.shahid.quill.app.ui.theme.QuillTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as QuillApplication

        setContent {
            QuillTheme {
                val navController = rememberNavController()
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                var startDestination by remember { mutableStateOf<String?>(null) }
                var updateResult by remember { mutableStateOf<UpdateCheckResult?>(null) }
                var showOptionalUpdateDialog by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    val onboardingDone = app.preferences.isOnboardingCompleted.first()
                    val setupDone = app.preferences.isSetupCompleted.first()

                    startDestination = when {
                        !onboardingDone -> Screen.Onboarding.route
                        !setupDone && !app.prootRunner.isEnvironmentReady() -> Screen.Setup.route
                        else -> Screen.Images.route
                    }

                    val check = app.updateManager.checkForUpdates()
                    updateResult = check
                    if (check is UpdateCheckResult.OptionalUpdate) {
                        showOptionalUpdateDialog = true
                    }
                }

                if (startDestination == null) {
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    }
                } else {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route ?: startDestination!!

                    if (updateResult is UpdateCheckResult.ForceUpdate) {
                        ForceUpdateScreen(
                            versionInfo = (updateResult as UpdateCheckResult.ForceUpdate).versionInfo,
                            onRetry = {
                                scope.launch {
                                    updateResult = app.updateManager.checkForUpdates()
                                }
                            }
                        )
                    } else {
                        ModalNavigationDrawer(
                            drawerState = drawerState,
                            drawerContent = {
                                QuillNavigationDrawerContent(
                                    currentRoute = currentRoute,
                                    onNavigate = { route ->
                                        navController.navigate(route) {
                                            popUpTo(Screen.Images.route) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    onClose = { scope.launch { drawerState.close() } }
                                )
                            }
                        ) {
                            QuillNavGraph(
                                navController = navController,
                                startDestination = startDestination!!,
                                drawerState = drawerState,
                                scope = scope
                            )
                        }

                        if (showOptionalUpdateDialog && updateResult is UpdateCheckResult.OptionalUpdate) {
                            OptionalUpdateDialog(
                                versionInfo = (updateResult as UpdateCheckResult.OptionalUpdate).versionInfo,
                                onDismiss = { showOptionalUpdateDialog = false }
                            )
                        }
                    }
                }
            }
        }
    }
}
