package com.shk.smarty.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp

// Set up Google Fonts Provider
private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = listOf(listOf(byteArrayOf(0x00)))  // Using a dummy certificate instead of resource reference
)

// Define the fonts we want to use
private val outrunFont = GoogleFont("Orbitron")
private val quicksandFont = GoogleFont("Quicksand")
private val robotoMonoFont = GoogleFont("Roboto Mono")

// Create font families
val OrbitronFamily = FontFamily(
    Font(
        googleFont = outrunFont,
        fontProvider = provider,
        weight = FontWeight.Bold
    ),
    Font(
        googleFont = outrunFont,
        fontProvider = provider,
        weight = FontWeight.Normal
    )
)

val QuicksandFamily = FontFamily(
    Font(
        googleFont = quicksandFont,
        fontProvider = provider,
        weight = FontWeight.Bold
    ),
    Font(
        googleFont = quicksandFont,
        fontProvider = provider,
        weight = FontWeight.Normal
    ),
    Font(
        googleFont = quicksandFont,
        fontProvider = provider,
        weight = FontWeight.Light
    )
)

val RobotoMonoFamily = FontFamily(
    Font(
        googleFont = robotoMonoFont,
        fontProvider = provider,
        weight = FontWeight.Normal
    ),
    Font(
        googleFont = robotoMonoFont,
        fontProvider = provider,
        weight = FontWeight.Medium
    )
)

// Typography styles using our custom fonts
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = OrbitronFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 48.sp,
        letterSpacing = 0.sp
    ),
    displayMedium = TextStyle(
        fontFamily = OrbitronFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = OrbitronFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = QuicksandFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = QuicksandFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = QuicksandFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    labelLarge = TextStyle(
        fontFamily = RobotoMonoFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = RobotoMonoFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)