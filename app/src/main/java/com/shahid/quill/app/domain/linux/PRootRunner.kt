package com.shahid.quill.app.domain.linux

import android.content.Context
import com.shahid.quill.app.data.model.AgentEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

data class CommandResult(
    val exitCode: Int,
    val stdout: String,
    val stderr: String
)

class PRootRunner(private val context: Context) {

    val rootfsDir: File get() = File(context.filesDir, "rootfs")
    val prootBinary: File get() = File(context.filesDir, "bin/proot")

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    fun isEnvironmentReady(): Boolean {
        val bash = File(rootfsDir, "bin/bash")
        val agy = File(rootfsDir, "usr/local/bin/agy")
        val altAgy = File(rootfsDir, "root/.local/bin/agy")
        return rootfsDir.exists() && bash.exists() && (agy.exists() || altAgy.exists() || File("/data/data/com.termux/files/home/.local/bin/agy").exists())
    }

    suspend fun executeInPRoot(
        command: String,
        envVars: Map<String, String> = emptyMap(),
        workingDir: String = "/root"
    ): CommandResult = withContext(Dispatchers.IO) {
        val termuxAgy = File("/data/data/com.termux/files/home/.local/bin/agy")
        val pb: ProcessBuilder

        if (termuxAgy.exists() && File("/data/data/com.termux/files/usr/bin/bash").exists()) {
            pb = ProcessBuilder("/data/data/com.termux/files/usr/bin/bash", "-c", command)
            pb.environment().putAll(envVars)
            pb.directory(File("/data/data/com.termux/files/home"))
        } else if (prootBinary.exists() && rootfsDir.exists()) {
            val prootArgs = mutableListOf(
                prootBinary.absolutePath,
                "-0",
                "-r", rootfsDir.absolutePath,
                "-b", "/dev",
                "-b", "/proc",
                "-b", "/sys",
                "-b", "${context.filesDir.absolutePath}:/app_files",
                "-w", workingDir,
                "/bin/bash", "-c", command
            )
            pb = ProcessBuilder(prootArgs)
            pb.environment().putAll(envVars)
        } else {
            pb = ProcessBuilder("sh", "-c", command)
            pb.environment().putAll(envVars)
        }

        try {
            val process = pb.start()
            val stdout = process.inputStream.bufferedReader().use { it.readText() }
            val stderr = process.errorStream.bufferedReader().use { it.readText() }
            val exitCode = process.waitFor()
            CommandResult(exitCode, stdout.trim(), stderr.trim())
        } catch (e: Exception) {
            CommandResult(-1, "", e.message ?: "Failed to execute command: $command")
        }
    }

    fun streamAgyPrompt(
        prompt: String,
        model: String? = null,
        agentMode: String? = null
    ): Flow<AgentEvent> = flow {
        emit(AgentEvent(type = "init", stage = "Initializing Antigravity session...", content = "Starting agent"))

        val safePrompt = prompt.replace("\"", "\\\"").replace("$", "\\$")
        val modelArg = if (!model.isNullOrBlank() && model != "default") " --model=\"$model\"" else ""
        val modeArg = if (!agentMode.isNullOrBlank()) " --agent-mode=$agentMode" else " --agent-mode=auto"
        val cmd = "agy -p \"$safePrompt\"$modelArg$modeArg --output-format stream-json"

        val termuxAgy = File("/data/data/com.termux/files/home/.local/bin/agy")
        val pb: ProcessBuilder

        if (termuxAgy.exists() && File("/data/data/com.termux/files/usr/bin/bash").exists()) {
            val termuxCmd = "agy -p \"$safePrompt\"$modelArg$modeArg --sandbox=false --output-format stream-json"
            pb = ProcessBuilder("/data/data/com.termux/files/usr/bin/bash", "-c", termuxCmd)
            pb.directory(File("/data/data/com.termux/files/home"))
        } else if (prootBinary.exists() && rootfsDir.exists()) {
            val prootArgs = listOf(
                prootBinary.absolutePath,
                "-0",
                "-r", rootfsDir.absolutePath,
                "-b", "/dev",
                "-b", "/proc",
                "-b", "/sys",
                "-b", "${context.filesDir.absolutePath}:/app_files",
                "-w", "/root",
                "/bin/bash", "-c", cmd
            )
            pb = ProcessBuilder(prootArgs)
        } else {
            pb = ProcessBuilder("sh", "-c", cmd)
        }

        pb.redirectErrorStream(true)

        val process: Process
        try {
            process = pb.start()
        } catch (e: Exception) {
            emit(AgentEvent(type = "error", error = "Failed to launch Antigravity: ${e.message}"))
            return@flow
        }

        val reader = BufferedReader(InputStreamReader(process.inputStream))
        var line: String?

        try {
            while (reader.readLine().also { line = it } != null) {
                val currentLine = line?.trim() ?: continue
                if (currentLine.isEmpty()) continue

                try {
                    val parsed = json.decodeFromString<AgentEvent>(currentLine)
                    emit(parsed)
                } catch (e: Exception) {
                    emit(AgentEvent(type = "raw", rawText = currentLine, content = currentLine))
                }
            }
        } catch (e: Exception) {
            emit(AgentEvent(type = "error", error = "Stream reading error: ${e.message}"))
        } finally {
            reader.close()
            process.destroy()
        }

        emit(AgentEvent(type = "result", stage = "Completed", content = "Agent execution finished"))
    }.flowOn(Dispatchers.IO)

    suspend fun getInstalledAgyVersion(): String {
        val res = executeInPRoot("agy --version 2>/dev/null || ~/.local/bin/agy --version 2>/dev/null")
        return if (res.exitCode == 0 && res.stdout.isNotBlank()) {
            res.stdout
        } else {
            "Not installed"
        }
    }

    suspend fun checkLoginStatus(): Boolean {
        val res = executeInPRoot("agy auth status 2>/dev/null || agy -p /status --output-format json 2>/dev/null")
        return res.exitCode == 0 && !res.stdout.contains("not logged in", ignoreCase = true)
    }

    suspend fun fetchModels(): CommandResult {
        return executeInPRoot("agy models 2>/dev/null || agy -p /models --output-format json 2>/dev/null")
    }

    suspend fun fetchUsage(): CommandResult {
        return executeInPRoot("agy -p /usage --output-format json 2>/dev/null")
    }
}
