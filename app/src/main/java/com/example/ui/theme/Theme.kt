package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BurgundyVariantDark = Color(0xFF5A1C28)

private val BurgundyDarkColorScheme = darkColorScheme(
    primary = BurgundyPrimaryDark,
    onPrimary = BurgundyDark,
    primaryContainer = BurgundyContainerDark,
    onPrimaryContainer = BurgundyContainer,
    secondary = BurgundySecondary,
    onSecondary = Color.White,
    secondaryContainer = BurgundyVariantDark,
    onSecondaryContainer = Color(0xFFFFD9DC),
    tertiary = AmberGold,
    onTertiary = Color(0xFF3F2E00),
    tertiaryContainer = Color(0xFF5A4300),
    onTertiaryContainer = AmberGoldContainer,
    background = BgDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = BorderDark,
    outlineVariant = Color(0xFF433034)
)

private val BurgundyLightColorScheme = lightColorScheme(
    primary = BurgundyPrimary,
    onPrimary = Color.White,
    primaryContainer = BurgundyContainer,
    onPrimaryContainer = OnBurgundyContainer,
    secondary = BurgundySecondary,
    onSecondary = Color.White,
    secondaryContainer = BurgundySecondaryContainer,
    onSecondaryContainer = BurgundyDark,
    tertiary = AmberGold,
    onTertiary = Color.White,
    tertiaryContainer = AmberGoldContainer,
    onTertiaryContainer = OnAmberGoldContainer,
    background = BgLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = BorderLight,
    outlineVariant = Color(0xFFEBDCDC)
)

@Composable
fun MonjezTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) BurgundyDarkColorScheme else BurgundyLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MonjezTheme(darkTheme = darkTheme, content = content)
}
