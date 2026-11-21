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

class QuillAgentService : Service() {

    companion object {
        const val CHANNEL_ID = "quill_agent_channel"
        const val NOTIFICATION_ID = 2001

        const val ACTION_START_AGENT = "ACTION_START_AGENT"
        const val ACTION_STOP_AGENT = "ACTION_STOP_AGENT"
        const val EXTRA_TASK_NAME = "EXTRA_TASK_NAME"

        fun startAgentTask(context: Context, taskName: String) {
            val intent = Intent(context, QuillAgentService::class.java).apply {
                action = ACTION_START_AGENT
                putExtra(EXTRA_TASK_NAME, taskName)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopAgentTask(context: Context) {
            val intent = Intent(context, QuillAgentService::class.java).apply {
                action = ACTION_STOP_AGENT
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_AGENT -> {
                val task = intent.getStringExtra(EXTRA_TASK_NAME) ?: "Running AI task..."
                startForeground(NOTIFICATION_ID, buildNotification(task))
            }
            ACTION_STOP_AGENT -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Quill Agent Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live status for background Antigravity tasks"
            }
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(taskName: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Quill AI Workspace")
            .setContentText(taskName)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
