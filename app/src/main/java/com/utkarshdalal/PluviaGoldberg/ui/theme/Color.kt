package com.utkarshdalal.PluviaGoldberg.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.alorma.compose.settings.ui.base.internal.SettingsTileColors
import com.alorma.compose.settings.ui.base.internal.SettingsTileDefaults

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
    titleColor = MaterialTheme.colorScheme.onSurface,
    subtitleColor = MaterialTheme.colorScheme.onSurface.copy(alpha = .75f),
    actionColor = MaterialTheme.colorScheme.onSurface,
)

@Composable
fun settingsTileColorsAlt(): SettingsTileColors = SettingsTileDefaults.colors(
    titleColor = MaterialTheme.colorScheme.onSurface,
    subtitleColor = MaterialTheme.colorScheme.onSurface.copy(alpha = .75f),
)

@Composable
fun settingsTileColorsDebug(): SettingsTileColors = SettingsTileDefaults.colors(
    titleColor = Color(0xFFBC2739),
    subtitleColor = MaterialTheme.colorScheme.onSurface.copy(alpha = .75f),
    actionColor = MaterialTheme.colorScheme.onSurface,
)
