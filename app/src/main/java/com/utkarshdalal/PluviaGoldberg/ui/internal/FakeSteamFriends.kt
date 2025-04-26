package com.utkarshdalal.PluviaGoldberg.ui.internal

import com.utkarshdalal.PluviaGoldberg.data.SteamFriend
import `in`.dragonbra.javasteam.enums.EPersonaState
import kotlin.random.Random

fun fakeSteamFriends(
    id: Long = 0,
    online: Boolean = true,
    inGame: Boolean = true,
): List<SteamFriend> {
    return List(5) { item ->
        SteamFriend(
            id = item + id,
            name = "Friend $item",
            avatarHash = when (item) {
                0 -> "eb59deb3b9282854064421f7c43f4c79bceaf6d8"
                1 -> "59d19880457012c47ea57bc29f599a4d2f663a35"
                2 -> "df3be70187c6d900600796c86963e3e3a1376deb"
                3 -> "d8ebede431682097c76492df1c2209b552c8f61b"
                4 -> "c9180f93ac892fa7d078f5946239d049e987e3b6"
                else -> ""
            },
            state = if (online) EPersonaState.Online else EPersonaState.Offline,
            gameAppID = if (inGame) Random.nextInt(1, 1000) else 0,
        )
    }
}
