package com.example.myapplication.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.myapplication.R

class ReminderBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderName = intent.getStringExtra("REMINDER_NAME")
        val reminderNote = intent.getStringExtra("REMINDER_NOTE")

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = System.currentTimeMillis().toInt()

        val notificationBuilder = NotificationCompat.Builder(context, "DAILY_REMINDER_CHANNEL")
            .setSmallIcon(R.drawable.ic_notifications_24)
            .setContentTitle(reminderName)
            .setContentText(reminderNote)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "DAILY_REMINDER_CHANNEL",
                "Daily Reminder",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}
