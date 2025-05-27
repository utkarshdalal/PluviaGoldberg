package app.gamenative.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.alorma.compose.settings.ui.base.internal.SettingsTileColors
import com.alorma.compose.settings.ui.base.internal.SettingsTileDefaults
// Your custom color scheme
val customBackground = Color(0xFF09090B)
val customForeground = Color(0xFFFAFAFA)
val customCard = Color(0xFF09090B)
val customCardForeground = Color(0xFFFAFAFA)
val customPrimary = Color(0xFFA21CAF)
val customPrimaryForeground = Color(0xFFFAFAFA)
val customSecondary = Color(0xFF27272A)
val customSecondaryForeground = Color(0xFFFAFAFA)
val customMuted = Color(0xFF27272A)
val customMutedForeground = Color(0xFF94969C)
val customAccent = Color(0xFF06B6D4)
val customAccentForeground = Color(0xFFFAFAFA)
val customDestructive = Color(0xFF7F1D1D)

val pluviaSeedColor = Color(0x284561FF)

/* Friend Status Colors */
val friendAwayOrSnooze = Color(0x806DCFF6)
val friendInGame = Color(0xFF90BA3C)
val friendInGameAwayOrSnooze = Color(0x8090BA3C)
val friendOffline = Color(0xFF7A7A7A)
val friendOnline = Color(0xFF6DCFF6)
val friendBlocked = Color(0xFF983D3D)

/**
 * Alorma compose settings tile colors
 */
@Composable
fun settingsTileColors(): SettingsTileColors = SettingsTileDefaults.colors(
    titleColor = customForeground,
    subtitleColor = customMutedForeground,
    actionColor = customAccent,
)

@Composable
fun settingsTileColorsAlt(): SettingsTileColors = SettingsTileDefaults.colors(
    titleColor = customForeground,
    subtitleColor = customMutedForeground,
)

@Composable
fun settingsTileColorsDebug(): SettingsTileColors = SettingsTileDefaults.colors(
    titleColor = customDestructive,
    subtitleColor = customMutedForeground,
    actionColor = customAccent,
)
