package com.yokdil.vocab

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.work.*
import com.yokdil.vocab.worker.DailyWordWorker
import java.util.concurrent.TimeUnit

class VocabApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        scheduleDailyWordNotification()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Günlük Kelime",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Her gün yeni bir YÖKDİL kelimesi öğren"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun scheduleDailyWordNotification() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val request = PeriodicWorkRequestBuilder<DailyWordWorker>(24, TimeUnit.HOURS)
            .setConstraints(constraints)
            .setInitialDelay(1, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "daily_word",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    companion object {
        const val CHANNEL_ID = "daily_word_channel"
    }
}
