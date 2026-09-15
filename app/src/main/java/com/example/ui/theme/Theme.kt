package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = GhanaGoldAccent,
    onPrimary = GhanaNavyPrimary,
    primaryContainer = GhanaNavyLight,
    onPrimaryContainer = Color.White,
    secondary = GhanaEmeraldGreen,
    onSecondary = Color.White,
    tertiary = GhanaGoldAccent,
    background = DarkNavyCanvas,
    onBackground = Color(0xFFF1F5F9),
    surface = DarkNavySurface,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF1E2D3D),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF334155)
)

private val LightColorScheme = lightColorScheme(
    primary = GhanaNavyPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE2EBF4),
    onPrimaryContainer = GhanaNavyPrimary,
    secondary = GhanaEmeraldGreen,
    onSecondary = Color.White,
    secondaryContainer = GhanaGreenContainer,
    onSecondaryContainer = Color(0xFF043421),
    tertiary = GhanaGoldAccent,
    background = OffWhiteCanvas,
    onBackground = Color(0xFF0F172A),
    surface = CardSurfaceWhite,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = MutedBorder
)

@Composable
fun StTalaforSchoolTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set false to preserve our custom Ghanaian school palette
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
        content = content
    )
}
