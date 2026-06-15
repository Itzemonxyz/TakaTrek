package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.example.R

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val fontName = GoogleFont("Outfit")

val outfitFontFamily = FontFamily(
    Font(googleFont = fontName, fontProvider = provider),
    Font(googleFont = fontName, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = fontName, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = fontName, fontProvider = provider, weight = FontWeight.Bold)
)

private val defaultTypography = Typography()

// Uniformly applied across the entire Material 3 Typography scale
val Typography = Typography(
    displayLarge = defaultTypography.displayLarge.copy(fontFamily = outfitFontFamily),
    displayMedium = defaultTypography.displayMedium.copy(fontFamily = outfitFontFamily),
    displaySmall = defaultTypography.displaySmall.copy(fontFamily = outfitFontFamily),
    headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = outfitFontFamily),
    headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = outfitFontFamily),
    headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = outfitFontFamily),
    titleLarge = defaultTypography.titleLarge.copy(fontFamily = outfitFontFamily),
    titleMedium = defaultTypography.titleMedium.copy(fontFamily = outfitFontFamily),
    titleSmall = defaultTypography.titleSmall.copy(fontFamily = outfitFontFamily),
    bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = outfitFontFamily),
    bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = outfitFontFamily),
    bodySmall = defaultTypography.bodySmall.copy(fontFamily = outfitFontFamily),
    labelLarge = defaultTypography.labelLarge.copy(fontFamily = outfitFontFamily),
    labelMedium = defaultTypography.labelMedium.copy(fontFamily = outfitFontFamily),
    labelSmall = defaultTypography.labelSmall.copy(fontFamily = outfitFontFamily)
)
