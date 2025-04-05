package com.example.mynews.presentation.theme

import androidx.compose.ui.graphics.Color


object BiasColors {
    val Left = Color(0xFF0069CB) // Dark Blue
    val LeanLeft = Color(0xFF67AAEA) // Light Blue
    val Center = Color(0xFFCE9261) // Neutral Beige
    val LeanRight = Color(0xFFD97979) // Light Red
    val Right = Color(0xFFCC0000) // Red
    val Mixed = Color(0xFF1E8A18) // Green
    val Neutral = Color(0xFF424242) // Gray (for unknown sources)

    fun getBiasColour(bias: String): Color {
        return when (bias) {
            "Left" -> Left
            "Lean Left" -> LeanLeft
            "Center" -> Center
            "Lean Right" -> LeanRight
            "Right" -> Right
            "Mixed" -> Mixed
            else -> Neutral
        }
    }

}
