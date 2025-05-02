package com.example.danakagetclaimer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

class DanaForegroundService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForeground()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun startForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "dana_service_channel"
            val channelName = "Dana Link Service"
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                setShowBadge(false)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        // Notification
        val notification = NotificationCompat.Builder(this, "dana_service_channel")
            .setContentTitle("Dana Link Service")
            .setContentText("Running in background to detect Dana links")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your icon
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(1000, notification)
    }
}