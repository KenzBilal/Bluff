package com.example.bluff.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferencesManager(private val context: Context) {
    private val sharedPreferences = context.getSharedPreferences("bluff_prefs", Context.MODE_PRIVATE)
    private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")

    val isOnboardingComplete: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ONBOARDING_COMPLETED] ?: false
    }

    suspend fun setOnboardingComplete(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = completed
        }
    }

    fun setUserId(userId: String) {
        sharedPreferences.edit().putString("user_id", userId).apply()
    }

    fun getUserIdBlocking(): String {
        return sharedPreferences.getString("user_id", "") ?: ""
    }

    /**
     * Persist "Later" dismissed recurring IDs scoped to today's date.
     * Reading on a new day returns empty set → popup shows again on fresh launch.
     */
    fun getDismissedRecurringIds(): Set<String> {
        val today = java.time.LocalDate.now().toString()
        val savedDate = sharedPreferences.getString("dismissed_recurring_date", null)
        if (savedDate != today) return emptySet() // New day → reset
        return sharedPreferences.getStringSet("dismissed_recurring_ids", emptySet()) ?: emptySet()
    }

    fun addDismissedRecurringId(id: String) {
        val today = java.time.LocalDate.now().toString()
        val current = getDismissedRecurringIds().toMutableSet()
        current.add(id)
        sharedPreferences.edit()
            .putString("dismissed_recurring_date", today)
            .putStringSet("dismissed_recurring_ids", current)
            .apply()
    }
}
