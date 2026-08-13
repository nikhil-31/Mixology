package com.capstone.nik.mixology.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val MixologyTypography = Typography(
    titleLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        color = MixologyText,
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = MixologyText,
    ),
    bodyLarge = TextStyle(
        fontSize = 17.sp,
        color = MixologyText,
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        color = MixologyText,
    ),
)
