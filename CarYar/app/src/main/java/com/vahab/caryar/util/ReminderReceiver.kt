package com.vahab.caryar.util

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.vahab.caryar.MainActivity
import com.vahab.caryar.R

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "یادآور ماشین‌یار"
        val message = intent.getStringExtra("message") ?: "یک یادآور فعال دارید"

        val openIntent = Intent(context, MainActivity::class.java)
        val pending = PendingIntent.getActivity(
            context, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "caryar_reminders")
            .setSmallIcon(android.R.drawable.ic_dialog_info) // replace with real icon later
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Re-schedule active reminders after reboot if needed
        // (to be expanded in later versions)
    }
}
