package com.shahid.quill.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.shahid.quill.app.domain.updates.UpdateManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

sealed class DownloadStatus {
    object Idle : DownloadStatus()
    data class Downloading(val progressPercent: Int, val bytesDownloaded: Long, val totalBytes: Long) : DownloadStatus()
    data class Success(val apkFile: File) : DownloadStatus()
    data class Error(val error: String) : DownloadStatus()
}

class UpdateDownloadService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private lateinit var notificationManager: NotificationManager
    private lateinit var updateManager: UpdateManager

    companion object {
        const val CHANNEL_ID = "quill_update_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START_DOWNLOAD = "ACTION_START_DOWNLOAD"
        const val EXTRA_APK_URL = "EXTRA_APK_URL"
        const val EXTRA_SHA256 = "EXTRA_SHA256"
        const val EXTRA_VERSION_NAME = "EXTRA_VERSION_NAME"

        private val _downloadStatus = MutableStateFlow<DownloadStatus>(DownloadStatus.Idle)
        val downloadStatus: StateFlow<DownloadStatus> = _downloadStatus.asStateFlow()

        fun startDownload(context: Context, apkUrl: String, sha256: String, versionName: String) {
            val intent = Intent(context, UpdateDownloadService::class.java).apply {
                action = ACTION_START_DOWNLOAD
                putExtra(EXTRA_APK_URL, apkUrl)
                putExtra(EXTRA_SHA256, sha256)
                putExtra(EXTRA_VERSION_NAME, versionName)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        updateManager = UpdateManager(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_START_DOWNLOAD) {
            val apkUrl = intent.getStringExtra(EXTRA_APK_URL) ?: ""
            val sha256 = intent.getStringExtra(EXTRA_SHA256) ?: ""
            val versionName = intent.getStringExtra(EXTRA_VERSION_NAME) ?: "update"

            startForeground(NOTIFICATION_ID, buildNotification("Downloading Quill update...", 0, true))
            performDownload(apkUrl, sha256, versionName)
        }
        return START_NOT_STICKY
    }

    private fun performDownload(apkUrl: String, sha256: String, versionName: String) {
        serviceScope.launch {
            val targetFile = File(cacheDir, "Quill-v$versionName.apk")
            val client = OkHttpClient()
            val request = Request.Builder().url(apkUrl).build()

            try {
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    _downloadStatus.value = DownloadStatus.Error("Failed to download APK: HTTP ${response.code}")
                    stopSelf()
                    return@launch
                }

                val body = response.body ?: throw Exception("Empty response body from download URL.")
                val totalBytes = body.contentLength()
                var downloadedBytes = 0L

                body.byteStream().use { input ->
                    FileOutputStream(targetFile).use { output ->
                        val buffer = ByteArray(8192)
                        var read: Int
                        var lastProgress = 0

                        while (input.read(buffer).also { read = it } != -1) {
                            output.write(buffer, 0, read)
                            downloadedBytes += read

                            val progress = if (totalBytes > 0) ((downloadedBytes * 100) / totalBytes).toInt() else 0
                            if (progress != lastProgress) {
                                lastProgress = progress
                                _downloadStatus.value = DownloadStatus.Downloading(progress, downloadedBytes, totalBytes)
                                notificationManager.notify(NOTIFICATION_ID, buildNotification("Downloading update ($progress%)", progress, false))
                            }
                        }
                    }
                }

                if (!updateManager.verifySha256(targetFile, sha256)) {
                    targetFile.delete()
                    _downloadStatus.value = DownloadStatus.Error("SHA-256 verification mismatch! Corrupted file rejected.")
                    stopSelf()
                    return@launch
                }

                _downloadStatus.value = DownloadStatus.Success(targetFile)
                notificationManager.notify(
                    NOTIFICATION_ID,
                    buildNotification("Download complete. Tap to install.", 100, false)
                )

                updateManager.installApk(targetFile)

            } catch (e: Exception) {
                _downloadStatus.value = DownloadStatus.Error(e.message ?: "Download failed.")
            } finally {
                stopForeground(STOP_FOREGROUND_DETACH)
                stopSelf()
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Quill Updates",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Download progress notifications for Quill updates"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(text: String, progress: Int, indeterminate: Boolean): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Quill App Update")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(100, progress, indeterminate)
            .setOngoing(true)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
