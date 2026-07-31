package com.example.muzik.myapplication.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val KonstaColorScheme = darkColorScheme(
    primary         = AppColors.Purple,
    secondary       = AppColors.PurpleLight,
    background      = AppColors.BgDeep,
    surface         = AppColors.BgSurface,
    onPrimary       = AppColors.TextPrimary,
    onSecondary     = AppColors.TextPrimary,
    onBackground    = AppColors.TextPrimary,
    onSurface       = AppColors.TextPrimary,
)

@Composable
fun KonstaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = KonstaColorScheme,
        content = content
    )
}
