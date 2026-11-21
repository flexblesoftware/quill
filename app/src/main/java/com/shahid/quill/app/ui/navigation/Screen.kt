package com.shahid.quill.app.ui.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Setup : Screen("setup")
    object Images : Screen("images")
    object Chat : Screen("chat/{conversationId}") {
        fun createRoute(conversationId: String = "new") = "chat/$conversationId"
    }
    object Conversations : Screen("conversations")
    object Code : Screen("code")
    object Terminal : Screen("terminal?cmd={cmd}") {
        fun createRoute(cmd: String = "") = if (cmd.isNotBlank()) "terminal?cmd=${java.net.URLEncoder.encode(cmd, "UTF-8")}" else "terminal"
    }
    object Models : Screen("models")
    object Usage : Screen("usage")
    object Permissions : Screen("permissions")
    object Settings : Screen("settings")
}
