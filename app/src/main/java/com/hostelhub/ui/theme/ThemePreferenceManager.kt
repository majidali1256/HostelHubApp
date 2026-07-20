package com.hostelhub.ui.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "hostel_hub_theme_prefs")

object ThemePreferenceManager {
    private val IS_DARK_MODE_KEY = booleanPreferencesKey("is_dark_mode")

    fun getThemeModeFlow(context: Context): Flow<Boolean?> {
        return context.themeDataStore.data.map { prefs ->
            prefs[IS_DARK_MODE_KEY]
        }
    }

    suspend fun setDarkMode(context: Context, isDark: Boolean?) {
        context.themeDataStore.edit { prefs ->
            if (isDark == null) {
                prefs.remove(IS_DARK_MODE_KEY)
            } else {
                prefs[IS_DARK_MODE_KEY] = isDark
            }
        }
    }

    fun isDarkThemeBlocking(context: Context): Boolean? {
        return try {
            var result: Boolean? = null
            runBlocking {
                context.themeDataStore.data.collect { prefs ->
                    result = prefs[IS_DARK_MODE_KEY]
                    throw InterruptedException() // Break early
                }
            }
            result
        } catch (e: InterruptedException) {
            null
        } catch (e: Exception) {
            null
        }
    }
}
