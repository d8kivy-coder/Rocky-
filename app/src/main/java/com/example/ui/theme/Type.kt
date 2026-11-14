package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val BebasNeueFamily = FontFamily(Font(R.font.bebas_neue, FontWeight.Normal))
val OswaldFamily = FontFamily(Font(R.font.oswald, FontWeight.Normal))
val RobotoMonoFamily = FontFamily(Font(R.font.roboto_mono, FontWeight.Normal))

val Typography =
  Typography(
    displayLarge =
      TextStyle(
        fontFamily = BebasNeueFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 44.sp,
        lineHeight = 48.sp,
        letterSpacing = 2.sp,
      ),
    displaySmall =
      TextStyle(
        fontFamily = BebasNeueFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 30.sp,
        lineHeight = 34.sp,
        letterSpacing = 1.5.sp,
      ),
    headlineMedium =
      TextStyle(
        fontFamily = OswaldFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.8.sp,
      ),
    titleMedium =
      TextStyle(
        fontFamily = OswaldFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.6.sp,
      ),
    bodyLarge =
      TextStyle(
        fontFamily = OswaldFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.3.sp,
      ),
    bodyMedium =
      TextStyle(
        fontFamily = OswaldFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.25.sp,
      ),
    labelLarge =
      TextStyle(
        fontFamily = RobotoMonoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.5.sp,
      ),
    labelSmall =
      TextStyle(
        fontFamily = OswaldFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.8.sp,
      ),
  )
