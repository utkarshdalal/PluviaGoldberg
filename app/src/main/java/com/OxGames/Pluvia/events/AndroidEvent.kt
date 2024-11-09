package com.OxGames.Pluvia.events

interface AndroidEvent : Event {
    data object BackPressed : AndroidEvent
    data class GotoAppScreen(val appId: Int) : AndroidEvent
}