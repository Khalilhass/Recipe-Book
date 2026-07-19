package com.example.recipe_book.data.local

import com.example.recipe_book.util.Constants


import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = Constants.SESSION_DATASTORE_NAME)

@Singleton
class SessionPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val rememberMeKey = booleanPreferencesKey("remember_me")

    suspend fun isRememberMeEnabled(): Boolean {
        return context.dataStore.data.first()[rememberMeKey] ?: false
    }

    suspend fun setRememberMe(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[rememberMeKey] = enabled
        }
    }
}