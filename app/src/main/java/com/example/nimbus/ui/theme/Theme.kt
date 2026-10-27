package com.example.nimbus.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = SkyBlue40,
    onPrimary = CloudWhite,
    primaryContainer = SkyBlue90,
    onPrimaryContainer = SkyBlue10,
    secondary = Slate40,
    onSecondary = CloudWhite,
    secondaryContainer = Slate90,
    onSecondaryContainer = Slate10,
    tertiary = Sun40,
    onTertiary = CloudWhite,
    tertiaryContainer = Sun90,
    onTertiaryContainer = Sun10,
    background = CloudWhite,
    onBackground = Ink,
    surface = CloudWhite,
    onSurface = Ink,
    surfaceVariant = Mist,
    onSurfaceVariant = MistText,
)

private val DarkColorScheme = darkColorScheme(
    primary = SkyBlue80,
    onPrimary = SkyBlue20,
    primaryContainer = SkyBlue30,
    onPrimaryContainer = SkyBlue90,
    secondary = Slate80,
    onSecondary = Slate20,
    secondaryContainer = Slate30,
    onSecondaryContainer = Slate90,
    tertiary = Sun80,
    onTertiary = Sun20,
    tertiaryContainer = Sun30,
    onTertiaryContainer = Sun90,
    background = Night,
    onBackground = NightText,
    surface = Night,
    onSurface = NightText,
    surfaceVariant = NightVariant,
    onSurfaceVariant = NightVariantText,
)

/**
 * Thème de l'app.
 * [darkTheme] vient des réglages (Système / Clair / Sombre), appliqué dans MainActivity.
 * [dynamicColor] (couleurs tirées du fond d'écran, Android 12+) est désactivé par défaut
 * pour garder l'identité bleu ciel de Nimbus.
 */
@Composable
fun NimbusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
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
        content = content,
    )
}
