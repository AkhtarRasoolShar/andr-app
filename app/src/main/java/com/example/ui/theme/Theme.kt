package com.example.ui.theme

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
    primary = RustCaramel,
    onPrimary = NightForest,
    secondary = SilverSage,
    onSecondary = NightForest,
    tertiary = SandDune,
    background = NightForest,
    surface = DarkEbers,
    onBackground = OffWhiteLinen,
    onSurface = OffWhiteLinen,
    outline = SilverSage
)

private val LightColorScheme = lightColorScheme(
    primary = TerracottaClay,
    onPrimary = CanvasIvory,
    secondary = SageWillow,
    onSecondary = CanvasIvory,
    tertiary = SandDune,
    background = CreamChiffon,
    surface = CanvasIvory,
    onBackground = DeepCharcoalBrown,
    onSurface = DeepCharcoalBrown,
    outline = SandDune
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Set dynamic color default to false to showcase our custom artisanal branding scheme
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
        content = content
    )
}
