package com.hostelhub.ui.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "hostel_hub_theme_prefs")

object ThemePreferenceManager {
    private val THEME_MODE_KEY = stringPreferencesKey("theme_mode_str")
    private val OLD_IS_DARK_KEY = booleanPreferencesKey("is_dark_mode")

    const val MODE_LIGHT = "LIGHT"
    const val MODE_DARK = "DARK"
    const val MODE_SYSTEM = "SYSTEM"

    fun getThemeModeFlow(context: Context): Flow<String> {
        return context.themeDataStore.data.map { prefs ->
            prefs[THEME_MODE_KEY] ?: when (prefs[OLD_IS_DARK_KEY]) {
                true -> MODE_DARK
                false -> MODE_LIGHT
                null -> MODE_LIGHT // Default theme is Light as requested
            }
        }
    }

    suspend fun setThemeMode(context: Context, mode: String) {
        context.themeDataStore.edit { prefs ->
            prefs[THEME_MODE_KEY] = mode
            when (mode) {
                MODE_DARK -> prefs[OLD_IS_DARK_KEY] = true
                MODE_LIGHT -> prefs[OLD_IS_DARK_KEY] = false
                else -> prefs.remove(OLD_IS_DARK_KEY)
            }
        }
    }

    suspend fun setDarkMode(context: Context, isDark: Boolean?) {
        setThemeMode(context, when (isDark) {
            true -> MODE_DARK
            false -> MODE_LIGHT
            null -> MODE_SYSTEM
        })
    }

    fun isDarkThemeBlocking(context: Context): Boolean? {
        return try {
            var result: Boolean? = null
            runBlocking {
                context.themeDataStore.data.collect { prefs ->
                    val mode = prefs[THEME_MODE_KEY] ?: when (prefs[OLD_IS_DARK_KEY]) {
                        true -> MODE_DARK
                        false -> MODE_LIGHT
                        null -> MODE_LIGHT
                    }
                    result = when (mode) {
                        MODE_DARK -> true
                        MODE_LIGHT -> false
                        else -> null
                    }
                    throw InterruptedException()
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
