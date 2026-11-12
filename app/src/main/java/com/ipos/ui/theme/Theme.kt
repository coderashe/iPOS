package com.ipos.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryGreenDark,
    onPrimary = TextPrimaryDark,
    primaryContainer = PrimaryGreen,
    onPrimaryContainer = TextPrimaryDark,
    
    secondary = SecondaryTeal,
    onSecondary = TextPrimaryDark,
    secondaryContainer = SecondaryTeal,
    onSecondaryContainer = TextPrimaryDark,
    
    tertiary = TertiaryAmber,
    onTertiary = TextPrimaryLight,
    tertiaryContainer = TertiaryAmber,
    onTertiaryContainer = TextPrimaryLight,
    
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = CardDark,
    onSurfaceVariant = TextSecondaryDark,
    
    error = ErrorRed,
    onError = TextPrimaryDark,
    
    outline = DividerDark
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = BackgroundLight,
    primaryContainer = PrimaryGreen,
    onPrimaryContainer = BackgroundLight,
    
    secondary = SecondaryTeal,
    onSecondary = BackgroundLight,
    secondaryContainer = SecondaryTeal,
    onSecondaryContainer = BackgroundLight,
    
    tertiary = TertiaryAmber,
    onTertiary = TextPrimaryLight,
    tertiaryContainer = TertiaryAmber,
    onTertiaryContainer = TextPrimaryLight,
    
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = CardLight,
    onSurfaceVariant = TextSecondaryLight,
    
    error = ErrorRed,
    onError = BackgroundLight,
    
    outline = DividerLight
)

@Composable
fun IPosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disabled for consistent branding
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
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun LearnCompose303Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) = IPosTheme(darkTheme, dynamicColor, content)
