package com.example.danakagetclaimer

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Path
import android.graphics.Point
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.RequiresApi

class AutoClickerService : AccessibilityService() {

    companion object {
        private const val TAG = "AutoClickerService"
        const val ACTION_PERFORM_CLICK = "com.example.danakaget.PERFORM_CLICK"
    }

    private val clickReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_PERFORM_CLICK) {
                Log.d(TAG, "Received click request")
                val delay = intent.getLongExtra("delay", 5500)

                Handler(Looper.getMainLooper()).postDelayed({
                    Log.d(TAG, "Performing first click attempt after app load")
                    performCenterClick()

                    // One more click just in case lmao
                    Handler(Looper.getMainLooper()).postDelayed({
                        Log.d(TAG, "Performing second click attempt")
                        performCenterClick()
                    }, 500)
                }, delay)
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "Service connected")

        // Click commands
        val filter = IntentFilter(ACTION_PERFORM_CLICK)
        filter.priority = 999
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(clickReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(clickReceiver, filter)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {

    }

    override fun onInterrupt() {
        Log.d(TAG, "Service interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(clickReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering receiver: ${e.message}")
        }
    }

    fun performCenterClick() {
        Log.d(TAG, "Attempting to perform center click")

        // Get screen dimensions
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val screenSize = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = wm.currentWindowMetrics.bounds
            Point(bounds.width(), bounds.height())
        } else {
            @Suppress("DEPRECATION")
            val display = wm.defaultDisplay
            val size = Point()
            @Suppress("DEPRECATION")
            display.getSize(size)
            size
        }

        val centerX = screenSize.x / 2f
        val centerY = screenSize.y / 2f

        Log.d(TAG, "Screen center: $centerX, $centerY")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            performClick(centerX, centerY)
        } else {
            Log.e(TAG, "Click simulation requires Android 7.0 or higher")
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun performClick(x: Float, y: Float) {
        try {
            val clickPath = Path()
            clickPath.moveTo(x, y)

            val gestureBuilder = GestureDescription.Builder()
            val gestureStroke = GestureDescription.StrokeDescription(
                clickPath,
                0,
                50
            )

            gestureBuilder.addStroke(gestureStroke)
            val gesture = gestureBuilder.build()

            val dispatchResult = dispatchGesture(gesture, object : GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription) {
                    super.onCompleted(gestureDescription)
                    Log.d(TAG, "Click gesture completed successfully")
                }

                override fun onCancelled(gestureDescription: GestureDescription) {
                    super.onCancelled(gestureDescription)
                    Log.e(TAG, "Click gesture was cancelled")
                }
            }, null)

            Log.d(TAG, "Dispatch result: $dispatchResult")

            if (!dispatchResult) {
                Log.d(TAG, "First click approach failed, trying alternative")
                Handler(Looper.getMainLooper()).postDelayed({
                    val newPath = Path()
                    newPath.moveTo(x, y)

                    val newBuilder = GestureDescription.Builder()
                    newBuilder.addStroke(GestureDescription.StrokeDescription(
                        newPath, 0, 100
                    ))

                    dispatchGesture(newBuilder.build(), null, null)
                }, 100)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error performing click: ${e.message}")
            e.printStackTrace()
        }
    }

}