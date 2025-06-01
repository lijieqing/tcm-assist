package com.tcm.traditionalchinesemedician.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Material 3 palette
val md_theme_light_primary = Color(0xFF366B21)
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer = Color(0xFFB6F49A)
val md_theme_light_onPrimaryContainer = Color(0xFF062100)
val md_theme_light_secondary = Color(0xFF55624C)
val md_theme_light_onSecondary = Color(0xFFFFFFFF)
val md_theme_light_secondaryContainer = Color(0xFFD8E7CB)
val md_theme_light_onSecondaryContainer = Color(0xFF131F0D)
val md_theme_light_background = Color(0xFFFDFDF5)
val md_theme_light_onBackground = Color(0xFF1A1C18)
val md_theme_light_surface = Color(0xFFFDFDF5)
val md_theme_light_onSurface = Color(0xFF1A1C18)

val md_theme_dark_primary = Color(0xFF9BD780)
val md_theme_dark_onPrimary = Color(0xFF0F3900)
val md_theme_dark_primaryContainer = Color(0xFF1F520A)
val md_theme_dark_onPrimaryContainer = Color(0xFFB6F49A)
val md_theme_dark_secondary = Color(0xFFBCCBAF)
val md_theme_dark_onSecondary = Color(0xFF283420)
val md_theme_dark_secondaryContainer = Color(0xFF3E4A35)
val md_theme_dark_onSecondaryContainer = Color(0xFFD8E7CB)
val md_theme_dark_background = Color(0xFF1A1C18)
val md_theme_dark_onBackground = Color(0xFFE3E3DC)
val md_theme_dark_surface = Color(0xFF1A1C18)
val md_theme_dark_onSurface = Color(0xFFE3E3DC)

private val DarkColorScheme = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface
)

private val LightColorScheme = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface
)

@Composable
fun TraditionalChineseMedicianTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
} 