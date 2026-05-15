package com.example.nextstation.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val DEFAULT_PHONE_NUMBER = stringPreferencesKey("default_phone_number")
    }

    val defaultPhoneNumber: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DEFAULT_PHONE_NUMBER] ?: ""
    }

    suspend fun updateDefaultPhoneNumber(phoneNumber: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_PHONE_NUMBER] = phoneNumber
        }
    }
}
