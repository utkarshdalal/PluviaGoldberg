package com.utkarshdalal.PluviaGoldberg.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.materialkolor.PaletteStyle
import com.materialkolor.rememberDynamicColorScheme

@Composable
fun PluviaTheme(
    seedColor: Color = pluviaSeedColor,
    isDark: Boolean = isSystemInDarkTheme(),
    isAmoled: Boolean = false,
    style: PaletteStyle = PaletteStyle.TonalSpot,
    content: @Composable () -> Unit,
) {
    // https://github.com/jordond/MaterialKolor
    val colorScheme = rememberDynamicColorScheme(primary = seedColor, isDark = isDark, isAmoled = isAmoled, style = style)

    // Override the system bars color theme.
    val view = LocalView.current
    if (!view.isInEditMode) {
        val window = (view.context as Activity).window
        val insetsController = WindowCompat.getInsetsController(window, view)

        insetsController.isAppearanceLightStatusBars = !isDark
        insetsController.isAppearanceLightNavigationBars = !isDark
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
