package com.yokdil.vocab.worker

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.yokdil.vocab.VocabApplication
import com.yokdil.vocab.data.db.AppDatabase
import com.yokdil.vocab.data.repository.WordRepository
import com.yokdil.vocab.ui.MainActivity

class DailyWordWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.getDatabase(context)
        val repository = WordRepository(db.wordDao())
        val word = repository.getRandomWord() ?: return Result.success()

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, VocabApplication.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentTitle("Günün Kelimesi: ${word.word}")
            .setContentText("${word.turkishMeaning} (${word.partOfSpeech})")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("${word.turkishMeaning}\n\n${word.exampleSentence}")
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)

        return Result.success()
    }

    companion object {
        const val NOTIFICATION_ID = 1001
    }
}
