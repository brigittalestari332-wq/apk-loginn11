package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val LightOceanColorScheme = lightColorScheme(
    primary = OceanPrimary,
    onPrimary = PureWhite,
    primaryContainer = IceBlueContainer,
    onPrimaryContainer = OceanDeep,
    secondary = OceanBright,
    onSecondary = PureWhite,
    secondaryContainer = FoamAqua.copy(alpha = 0.35f),
    onSecondaryContainer = OceanInk,
    tertiary = OceanDeep,
    onTertiary = PureWhite,
    tertiaryContainer = SeaTealContainer,
    onTertiaryContainer = SeaTealSuccess,
    background = SeaMist,
    onBackground = OceanInk,
    surface = PureWhite,
    onSurface = OceanInk,
    surfaceVariant = IceBlueContainer.copy(alpha = 0.55f),
    onSurfaceVariant = OceanMutedText,
    surfaceContainerLowest = PureWhite,
    surfaceContainerLow = SeaMist,
    surfaceContainer = PureWhite,
    surfaceContainerHigh = IceBlueContainer.copy(alpha = 0.45f),
    outline = OceanOutline,
    outlineVariant = OceanOutlineSubtle,
    error = CoralError,
    onError = PureWhite,
    errorContainer = CoralErrorContainer,
    onErrorContainer = CoralError,
)

private val DarkOceanColorScheme = darkColorScheme(
    primary = DarkOceanPrimary,
    onPrimary = DarkOceanOnPrimary,
    primaryContainer = DarkOceanPrimaryContainer,
    onPrimaryContainer = DarkOceanText,
    secondary = FoamAqua,
    onSecondary = DarkOceanOnPrimary,
    secondaryContainer = DarkOceanSurfaceHigh,
    onSecondaryContainer = DarkOceanText,
    tertiary = OceanCyan,
    onTertiary = DarkOceanOnPrimary,
    background = DarkOceanBg,
    onBackground = DarkOceanText,
    surface = DarkOceanSurface,
    onSurface = DarkOceanText,
    surfaceVariant = DarkOceanSurfaceHigh,
    onSurfaceVariant = DarkOceanMuted,
    surfaceContainerLowest = DarkOceanBg,
    surfaceContainerLow = DarkOceanSurface,
    surfaceContainer = DarkOceanSurface,
    surfaceContainerHigh = DarkOceanSurfaceHigh,
    outline = DarkOceanOutline,
    outlineVariant = DarkOceanOutline,
    error = CoralErrorContainer,
    onError = CoralError,
)

val SamudraShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun SamudraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkOceanColorScheme else LightOceanColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SamudraTypography,
        shapes = SamudraShapes,
        content = content,
    )
}
