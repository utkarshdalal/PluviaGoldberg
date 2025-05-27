package app.gamenative.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.materialkolor.PaletteStyle

// Custom dark color scheme based on your provided colors
private val CustomDarkColorScheme = darkColorScheme(
    primary = customPrimary,
    onPrimary = customPrimaryForeground,
    primaryContainer = customPrimary.copy(alpha = 0.2f),
    onPrimaryContainer = customPrimaryForeground,

    secondary = customSecondary,
    onSecondary = customSecondaryForeground,
    secondaryContainer = customSecondary.copy(alpha = 0.8f),
    onSecondaryContainer = customSecondaryForeground,

    tertiary = customAccent,
    onTertiary = customAccentForeground,
    tertiaryContainer = customAccent.copy(alpha = 0.2f),
    onTertiaryContainer = customAccentForeground,

    background = customBackground,
    onBackground = customForeground,

    surface = customCard,
    onSurface = customCardForeground,
    surfaceVariant = customSecondary,
    onSurfaceVariant = customMutedForeground,
    surfaceTint = customPrimary,

    inverseSurface = customForeground,
    inverseOnSurface = customBackground,
    inversePrimary = customPrimary,

    error = customDestructive,
    onError = customForeground,
    errorContainer = customDestructive.copy(alpha = 0.2f),
    onErrorContainer = customForeground,

    outline = customMutedForeground,
    outlineVariant = customMuted,

    scrim = Color.Black.copy(alpha = 0.5f),
    surfaceBright = customSecondary,
    surfaceDim = customBackground,
    surfaceContainer = customCard,
    surfaceContainerHigh = customSecondary,
    surfaceContainerHighest = customSecondary.copy(alpha = 0.9f),
    surfaceContainerLow = customBackground,
    surfaceContainerLowest = customBackground,
)

@Composable
fun PluviaTheme(
    seedColor: Color = pluviaSeedColor,
    isDark: Boolean = true, // Force dark theme since your colors are dark
    isAmoled: Boolean = false,
    style: PaletteStyle = PaletteStyle.TonalSpot,
    content: @Composable () -> Unit,
) {
    // Use your custom color scheme instead of dynamic colors for consistency
    val colorScheme = CustomDarkColorScheme

    // Override the system bars color theme.
    val view = LocalView.current
    if (!view.isInEditMode) {
        val window = (view.context as Activity).window
        val insetsController = WindowCompat.getInsetsController(window, view)

        // Always use dark system bars since we're using a dark theme
        insetsController.isAppearanceLightStatusBars = false
        insetsController.isAppearanceLightNavigationBars = false
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PluviaTypography,
        content = content,
    )
}
