package com.hostelhub.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val LocalIsDarkTheme = compositionLocalOf { false }

@Composable
fun isAppInDarkTheme(): Boolean = LocalIsDarkTheme.current

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    secondary = Secondary,
    tertiary = Accent,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = Color(0xFF374151),
    outline = Color(0xFF4B5563),
    onPrimary = TextOnPrimary,
    onSecondary = TextOnPrimary,
    onTertiary = TextOnPrimary,
    onBackground = TextOnDark,
    onSurface = TextOnDark,
    error = Error,
    onError = TextOnPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    secondary = Secondary,
    tertiary = Accent,
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = Color(0xFFF3F4F6),
    outline = Color(0xFFE5E7EB),
    onPrimary = TextOnPrimary,
    onSecondary = TextOnPrimary,
    onTertiary = TextOnPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = Error,
    onError = TextOnPrimary
)

@Composable
fun HostelHubTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalIsDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

