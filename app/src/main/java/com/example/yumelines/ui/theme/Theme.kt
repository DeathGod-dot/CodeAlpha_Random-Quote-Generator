package com.example.yumelines.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = MysticPurple,
    secondary = DeepSakura,
    tertiary = ElectricBlue,
    background = MidnightBlack,
    surface = ShadowGray,
    onPrimary = StarWhite,
    onSecondary = StarWhite,
    onTertiary = StarWhite,
    onBackground = StarWhite,
    onSurface = StarWhite,
    primaryContainer = ShadowGray,
    onPrimaryContainer = StarWhite,
    secondaryContainer = MidnightBlack,
    onSecondaryContainer = DeepSakura,
    error = CrimsonRed,
    onError = StarWhite
)

private val LightColorScheme = lightColorScheme(
    primary = NightPurple,
    secondary = SakuraPink,
    tertiary = AnimeBlue,
    background = StarWhite,
    surface = CloudGray,
    onPrimary = StarWhite,
    onSecondary = MidnightBlack,
    onTertiary = StarWhite,
    onBackground = MidnightBlack,
    onSurface = MidnightBlack,
    primaryContainer = CloudGray,
    onPrimaryContainer = NightPurple,
    secondaryContainer = StarWhite,
    onSecondaryContainer = SakuraPink,
    error = CrimsonRed,
    onError = StarWhite,
    surfaceVariant = CloudGray,
    onSurfaceVariant = ShadowGray
)

@Composable
fun YumeLinesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+ - disabled to show custom theme
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}