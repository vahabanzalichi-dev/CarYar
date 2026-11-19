package com.vahab.caryar

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.vahab.caryar.data.AppDatabase

class CarYarApp : Application() {
    val database by lazy { AppDatabase.getInstance(this) }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "caryar_reminders",
                "یادآورهای ماشین‌یار",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "اعلان‌های یادآور سرویس و هزینه‌های خودرو"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
