package com.calorietracker.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Ink = Color(0xFF1B2B22)
val InkSoft = Color(0xFF5C6B60)
val Bg = Color(0xFFF3F5F1)
val Surface = Color(0xFFFFFFFF)
val Primary = Color(0xFF2F6F4E)
val PrimaryLight = Color(0xFFDCEBE2)
val Accent = Color(0xFFF2A93B)
val Danger = Color(0xFFD9634B)
val Line = Color(0xFFE1E7DD)

private val AppColors = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    secondary = Accent,
    background = Bg,
    surface = Surface,
    onBackground = Ink,
    onSurface = Ink,
    error = Danger
)

@Composable
fun CalorieTrackerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColors,
        content = content
    )
}
