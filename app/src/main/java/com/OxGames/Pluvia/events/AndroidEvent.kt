package com.OxGames.Pluvia.events

interface AndroidEvent : Event {
    data object BackPressed : AndroidEvent
    data object HideAppBar : AndroidEvent
    data class SetSystemUI(val visible: Boolean) : AndroidEvent
}