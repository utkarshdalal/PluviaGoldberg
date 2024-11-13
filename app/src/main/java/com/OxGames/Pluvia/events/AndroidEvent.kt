package com.OxGames.Pluvia.events

import com.OxGames.Pluvia.ui.enums.Orientation
import java.util.EnumSet

interface AndroidEvent : Event {
    data object BackPressed : AndroidEvent
    data class SetAppBarVisibility(val visible: Boolean) : AndroidEvent
    data class SetSystemUIVisibility(val visible: Boolean) : AndroidEvent
    data class SetAllowedOrientation(val orientations: EnumSet<Orientation>) : AndroidEvent
    data object StartOrientator : AndroidEvent
}