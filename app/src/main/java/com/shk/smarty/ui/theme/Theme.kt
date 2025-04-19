package com.shk.smarty.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController

private val DarkColorScheme = darkColorScheme(
    primary = NeonBlue,
    onPrimary = White,
    secondary = ElectricPurple,
    onSecondary = White,
    tertiary = CyberGreen,
    background = SpaceBlack,
    surface = DeepSpace,
    onBackground = NeonCyan,
    onSurface = White,
    surfaceVariant = DeepSpace
)

private val LightColorScheme = lightColorScheme(
    primary = FuturisticBlue,
    onPrimary = White,
    secondary = TechPurple,
    onSecondary = White,
    tertiary = DigitalGreen,
    background = SnowWhite,
    surface = PureWhite,
    onBackground = DeepSpace,
    onSurface = SpaceBlack,
    surfaceVariant = LightGray
)

@Composable
fun SmartyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        val systemUiController = rememberSystemUiController()
        val useDarkIcons = !darkTheme
        
        DisposableEffect(systemUiController, useDarkIcons) {
            // Update all of the system bar colors to be transparent, and use
            // dark icons if we're in light theme
            systemUiController.setSystemBarsColor(
                color = if (darkTheme) SpaceBlack else SnowWhite,
                darkIcons = useDarkIcons
            )
            
            onDispose {}
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}