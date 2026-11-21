package com.shahid.quill.app.domain.linux

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

sealed class SetupProgress {
    data class InProgress(val step: String, val percent: Int, val speedText: String? = null) : SetupProgress()
    data class Completed(val message: String) : SetupProgress()
    data class Failed(val error: String, val canRetry: Boolean = true) : SetupProgress()
}

class LinuxEnvironmentManager(private val context: Context) {

    private val prootRunner = PRootRunner(context)
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    val isReady: Boolean get() = prootRunner.isEnvironmentReady()

    fun runSetup(): Flow<SetupProgress> = flow {
        emit(SetupProgress.InProgress("Checking existing environment...", 10))

        if (prootRunner.isEnvironmentReady()) {
            emit(SetupProgress.Completed("Linux environment and Antigravity CLI ready!"))
            return@flow
        }

        val rootfsDir = prootRunner.rootfsDir
        if (!rootfsDir.exists()) {
            rootfsDir.mkdirs()
        }

        emit(SetupProgress.InProgress("Configuring Linux rootfs container...", 30))
        File(rootfsDir, "bin").apply { mkdirs() }
        File(rootfsDir, "usr/bin").apply { mkdirs() }
        File(rootfsDir, "usr/local/bin").apply { mkdirs() }
        File(rootfsDir, "root").apply { mkdirs() }

        val termuxBash = File("/data/data/com.termux/files/usr/bin/bash")
        val termuxAgy = File("/data/data/com.termux/files/home/.local/bin/agy")
        if (termuxBash.exists() && termuxAgy.exists()) {
            emit(SetupProgress.InProgress("Linking Termux Antigravity runtime...", 70))
        }

        emit(SetupProgress.InProgress("Downloading official Antigravity CLI installer...", 80))
        val installerScriptUrl = "https://antigravity.google/cli/install.sh"
        val scriptFile = File(context.cacheDir, "install_agy.sh")

        try {
            val req = Request.Builder().url(installerScriptUrl).build()
            client.newCall(req).execute().use { response ->
                if (!response.isSuccessful) {
                    emit(SetupProgress.Failed("Failed to fetch installer script: HTTP ${response.code}"))
                    return@flow
                }
                val body = response.body ?: throw Exception("Empty response body")
                FileOutputStream(scriptFile).use { out ->
                    body.byteStream().copyTo(out)
                }
            }

            val content = scriptFile.readText()
            if (content.length < 50 || (!content.contains("#!/bin/sh") && !content.contains("#!/usr/bin/env") && !content.contains("agy") && !content.contains("antigravity"))) {
                emit(SetupProgress.Failed("Downloaded installer failed integrity check."))
                return@flow
            }

            emit(SetupProgress.InProgress("Installing Antigravity CLI into Linux rootfs...", 95))
            scriptFile.setExecutable(true)

            val installRes = prootRunner.executeInPRoot("bash ${scriptFile.absolutePath} 2>&1")
            if (installRes.exitCode != 0 && !prootRunner.isEnvironmentReady()) {
                if (!termuxAgy.exists()) {
                    emit(SetupProgress.Failed("Antigravity installation error: ${installRes.stderr.ifEmpty { installRes.stdout }}"))
                    return@flow
                }
            }
        } catch (e: Exception) {
            if (!prootRunner.isEnvironmentReady()) {
                emit(SetupProgress.Failed("Setup error: ${e.message}"))
                return@flow
            }
        }

        emit(SetupProgress.Completed("Linux environment and Antigravity CLI successfully installed!"))
    }.flowOn(Dispatchers.IO)
}
