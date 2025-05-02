package com.example.danakagetclaimer

import android.Manifest
import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        val extras = sbn.notification.extras
        val title = extras.getString("android.title")
        val text = extras.getCharSequence("android.text")?.toString() ?: ""

        if (packageName.contains("whatsapp") || packageName.contains("telegram")) {
            val regex = Regex("https://link\\.dana\\.id/danakaget(?:\\?[^\\s]*)?")
            val match = regex.find(text)

            if (match != null) {
                Log.d("DanaListener", "Found Dana link: ${match.value}")
                openDanaLink(match.value)
            }
        }
    }

    private fun openDanaLink(link: String) {
        Log.d("DanaListener", "Attempting to open Dana link with multiple methods")

        try {
            val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val linkIntent = Intent(Intent.ACTION_VIEW, Uri.parse(link)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            }

            val canResolve = linkIntent.resolveActivity(packageManager) != null
            Log.d("DanaListener", "Can resolve Dana link: $canResolve")

            if (canResolve) {
                startActivity(linkIntent)
                Log.d("DanaListener", "Method 1: Direct startActivity successful")
            } else {
                Log.e("DanaListener", "Method 1: No app can handle this link")
            }
        } catch (e: Exception) {
            Log.e("DanaListener", "Method 1 failed: ${e.message}")
        }

        try {
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                try {
                    val linkIntent = Intent(Intent.ACTION_VIEW, Uri.parse(link)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }

                    val pendingIntent = PendingIntent.getActivity(
                        this,
                        System.currentTimeMillis().toInt(), // Unique request code
                        linkIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    pendingIntent.send()
                    Log.d("DanaListener", "Method 2: PendingIntent.send() successful")
                } catch (e: Exception) {
                    Log.e("DanaListener", "Method 2 failed: ${e.message}")
                }
            }, 1000)
        } catch (e: Exception) {
            Log.e("DanaListener", "Method 2 setup failed: ${e.message}")
        }

        try {
            val broadcastIntent = Intent("com.application.OPEN_DANA_LINK").apply {
                putExtra("link", link)
                addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
            }
            sendBroadcast(broadcastIntent)
            Log.d("DanaListener", "Method 3: Broadcast sent to open link")
        } catch (e: Exception) {
            Log.e("DanaListener", "Method 3 failed: ${e.message}")
        }

        showLinkOpeningNotification(link)
    }

    private fun showLinkOpeningNotification(link: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "dana_link_channel"
            val channelName = "Dana Link Notifications"
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, "dana_link_channel")
            .setContentTitle("Opening Dana Link")
            .setContentText("Opening: $link")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        NotificationManagerCompat.from(this).notify(1001, notification)
    }
}