package com.shahid.quill.app.domain.terminal

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString

object AnsiParser {

    private val ANSI_PATTERN = Regex("\u001B\\[[;\\d]*[A-Za-z]")

    fun stripAnsi(input: String): String {
        return input.replace(ANSI_PATTERN, "")
    }

    fun parseToAnnotatedString(input: String): AnnotatedString {
        val clean = stripAnsi(input)
        return buildAnnotatedString {
            append(clean)
        }
    }
}
