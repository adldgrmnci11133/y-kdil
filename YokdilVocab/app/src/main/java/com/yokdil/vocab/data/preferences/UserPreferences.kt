package com.yokdil.vocab.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        val NOTIFICATION_HOUR = intPreferencesKey("notification_hour")
        val NOTIFICATION_MINUTE = intPreferencesKey("notification_minute")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val FLASHCARD_COUNT = intPreferencesKey("flashcard_count")
    }

    val notificationHour: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[NOTIFICATION_HOUR] ?: 9
    }

    val notificationMinute: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[NOTIFICATION_MINUTE] ?: 0
    }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[NOTIFICATIONS_ENABLED] ?: true
    }

    val flashcardCount: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[FLASHCARD_COUNT] ?: 20
    }

    suspend fun setNotificationTime(hour: Int, minute: Int) {
        context.dataStore.edit { prefs ->
            prefs[NOTIFICATION_HOUR] = hour
            prefs[NOTIFICATION_MINUTE] = minute
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setFlashcardCount(count: Int) {
        context.dataStore.edit { prefs ->
            prefs[FLASHCARD_COUNT] = count
        }
    }
}
