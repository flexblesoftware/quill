package com.shahid.quill.app.domain.updates

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import androidx.core.content.pm.PackageInfoCompat
import com.shahid.quill.app.data.model.VersionInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

sealed class UpdateCheckResult {
    data class ForceUpdate(val versionInfo: VersionInfo) : UpdateCheckResult()
    data class OptionalUpdate(val versionInfo: VersionInfo) : UpdateCheckResult()
    object UpToDate : UpdateCheckResult()
    data class Error(val message: String, val isOffline: Boolean = false) : UpdateCheckResult()
}

class UpdateManager(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    val currentVersionCode: Long
        get() {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            return PackageInfoCompat.getLongVersionCode(pInfo)
        }

    val currentVersionName: String
        get() {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            return pInfo.versionName ?: "1.0.0"
        }

    suspend fun checkForUpdates(repoOwner: String = "flexblesoftware", repoName: String = "quill"): UpdateCheckResult = withContext(Dispatchers.IO) {
        val url = "https://raw.githubusercontent.com/$repoOwner/$repoName/main/version.json?t=${System.currentTimeMillis()}"
        val request = Request.Builder().url(url).build()

        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext UpdateCheckResult.Error("HTTP Error ${response.code} fetching update data.")
            }

            val body = response.body?.string() ?: return@withContext UpdateCheckResult.Error("Empty update response.")
            val versionInfo = json.decodeFromString<VersionInfo>(body)

            val currentCode = currentVersionCode
            if (currentCode < versionInfo.minRequiredVersionCode) {
                UpdateCheckResult.ForceUpdate(versionInfo)
            } else if (currentCode < versionInfo.latestVersionCode) {
                UpdateCheckResult.OptionalUpdate(versionInfo)
            } else {
                UpdateCheckResult.UpToDate
            }
        } catch (e: Exception) {
            val isOffline = e is java.net.UnknownHostException || e is java.net.SocketTimeoutException
            UpdateCheckResult.Error(e.message ?: "Failed to check for updates.", isOffline)
        }
    }

    fun canInstallUnknownApps(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    fun openInstallPermissionSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    fun verifySha256(file: File, expectedHash: String): Boolean {
        if (!file.exists() || expectedHash.isBlank() || expectedHash == "PENDING_RELEASE") return true
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var read: Int
            while (input.read(buffer).also { read = it } != -1) {
                digest.update(buffer, 0, read)
            }
        }
        val computed = digest.digest().joinToString("") { "%02x".format(it) }
        return computed.equals(expectedHash.trim(), ignoreCase = true)
    }

    fun installApk(apkFile: File): Result<Unit> {
        return try {
            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, apkFile)

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
