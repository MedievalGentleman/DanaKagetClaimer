package com.example.danakagetclaimer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log

class LinkBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.application.OPEN_DANA_LINK") {
            val link = intent.getStringExtra("link")
            if (link != null) {
                Log.d("DanaListener", "BroadcastReceiver received link: $link")
                try {
                    val linkIntent = Intent(Intent.ACTION_VIEW, Uri.parse(link)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(linkIntent)
                    Log.d("DanaListener", "BroadcastReceiver opened link successfully")
                } catch (e: Exception) {
                    Log.e("DanaListener", "BroadcastReceiver failed to open link", e)
                }
            }
        }
    }
}