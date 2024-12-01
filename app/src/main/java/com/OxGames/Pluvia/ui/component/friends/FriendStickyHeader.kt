package com.OxGames.Pluvia.ui.component.friends

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.OxGames.Pluvia.data.SteamFriend
import com.OxGames.Pluvia.ui.theme.PluviaTheme
import `in`.dragonbra.javasteam.enums.EPersonaState

@Composable
fun StickyHeaderItem(isCollapsed: Boolean, header: String, count: Int, onHeaderAction: () -> Unit) {
    ListItem(
        headlineContent = { Text(text = "$header ($count)") },
        trailingContent = {
            val button = when (isCollapsed) {
                true -> Icons.Outlined.KeyboardArrowDown
                else -> Icons.Outlined.KeyboardArrowUp
            }
            IconButton(onClick = onHeaderAction) {
                Icon(imageVector = button, contentDescription = null)
            }
        }
    )
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview_StickyHeaderItem() {
    PluviaTheme {
        Column {
            StickyHeaderItem(isCollapsed = true, header = "Online", count = 60, onHeaderAction = {})
            FriendItem(
                friend = SteamFriend(
                    id = 0,
                    state = EPersonaState.Online.code(),
                    gameAppID = 440,
                    gameName = "Team Fortess 2",
                    name = "Name The Game",
                ),
                onClick = {},
            )
        }
    }
}