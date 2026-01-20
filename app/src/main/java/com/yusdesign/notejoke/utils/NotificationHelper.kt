package com.yusdesign.notejoke.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.yusdesign.notejoke.MainActivity
import com.yusdesign.notejoke.R

class NotificationHelper(private val context: Context) {

    private val channelId = "notejoke_channel"
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Python Love Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Shows new Python Love Meter readings"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(notificationId: Int, title: String, message: String) {
        // Intent to open the app when notification is tapped
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Replace with your own icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setNumber(1) // Adds a badge count
            .build()

        notificationManager.notify(notificationId, notification)
    }
}
