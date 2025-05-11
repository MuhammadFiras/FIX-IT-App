@file:JvmName("TypeKt")

package com.example.fixit.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.fixit.R

val InterFont = FontFamily(
    Font(R.font.inter_variablefont_opsz_wght, weight = FontWeight.Normal)
)

val Typography = Typography(
    displayLarge = androidx.compose.ui.text.TextStyle(
        fontFamily = InterFont,
        fontWeight = FontWeight.Normal
    ),
    displayMedium = androidx.compose.ui.text.TextStyle(
        fontFamily = InterFont
    ),
    displaySmall = androidx.compose.ui.text.TextStyle(
        fontFamily = InterFont
    ),
    headlineLarge = androidx.compose.ui.text.TextStyle(
        fontFamily = InterFont
    ),
    headlineMedium = androidx.compose.ui.text.TextStyle(
        fontFamily = InterFont
    ),
    headlineSmall = androidx.compose.ui.text.TextStyle(
        fontFamily = InterFont
    ),
    bodyLarge = androidx.compose.ui.text.TextStyle(
        fontFamily = InterFont
    ),
    bodyMedium = androidx.compose.ui.text.TextStyle(
        fontFamily = InterFont
    ),
    bodySmall = androidx.compose.ui.text.TextStyle(
        fontFamily = InterFont
    ),
    labelLarge = androidx.compose.ui.text.TextStyle(
        fontFamily = InterFont
    ),
    labelMedium = androidx.compose.ui.text.TextStyle(
        fontFamily = InterFont
    ),
    labelSmall = androidx.compose.ui.text.TextStyle(
        fontFamily = InterFont
    )
)
