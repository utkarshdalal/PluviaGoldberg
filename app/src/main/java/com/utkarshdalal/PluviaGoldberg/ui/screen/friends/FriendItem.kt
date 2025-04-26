package com.utkarshdalal.PluviaGoldberg.ui.screen.friends

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.utkarshdalal.PluviaGoldberg.data.SteamFriend
import com.utkarshdalal.PluviaGoldberg.ui.theme.PluviaTheme
import com.utkarshdalal.PluviaGoldberg.ui.util.ListItemImage
import com.utkarshdalal.PluviaGoldberg.utils.getAvatarURL
import com.materialkolor.ktx.isLight
import `in`.dragonbra.javasteam.enums.EFriendRelationship
import `in`.dragonbra.javasteam.enums.EPersonaState
import `in`.dragonbra.javasteam.enums.EPersonaStateFlag

// https://m3.material.io/components/lists/specs#d156b3f2-6763-4fde-ba6f-0f088ce5a4e4

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FriendItem(
    modifier: Modifier = Modifier,
    friend: SteamFriend,
    onClick: (SteamFriend) -> Unit,
    onLongClick: (SteamFriend) -> Unit,
) {
    // Can't use CompositionLocal for colors. Instead we can use ListItemDefault.colors()

    val isLight = MaterialTheme.colorScheme.background.isLight()

    ListItem(
        modifier = modifier.combinedClickable(
            onClick = { onClick(friend) },
            onLongClick = { onLongClick(friend) },
        ),
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent,
            headlineColor = if (isLight) MaterialTheme.colorScheme.onSurface else friend.statusColor,
            supportingColor = if (isLight) MaterialTheme.colorScheme.onSurfaceVariant else friend.statusColor,
        ),
        headlineContent = {
            Text(
                text = buildAnnotatedString {
                    append(friend.nameOrNickname)
                    if (friend.nickname.isNotEmpty()) {
                        withStyle(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                            ),
                        ) {
                            append(" * ")
                        }
                    } else {
                        append(" ")
                    }
                    appendInlineContent("icon", "[icon]")
                },
                inlineContent = mapOf(
                    "icon" to InlineTextContent(
                        Placeholder(
                            width = 14.sp,
                            height = 14.sp,
                            placeholderVerticalAlign = PlaceholderVerticalAlign.Center,
                        ),
                        children = {
                            friend.statusIcon?.let {
                                Icon(
                                    imageVector = it,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    contentDescription = it.name,
                                )
                            }
                        },
                    ),
                ),
            )
        },
        supportingContent = {
            if (friend.isPlayingGame) {
                // TODO get game names
                Text(text = "Playing: " + friend.gameName.ifEmpty { "${friend.gameAppID}" })
            } else {
                Text(text = friend.state.name)
            }
        },
        leadingContent = {
            ListItemImage(
                modifier = Modifier.clickable { onLongClick(friend) },
                image = { friend.avatarHash.getAvatarURL() },
            )
        },
    )
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview_FriendItem() {
    val friendData = mapOf(
        "Friend Online" to EPersonaState.Online,
        "Friend Away" to EPersonaState.Away,
        "Friend Offline" to EPersonaState.Offline,
        "Friend In Game" to EPersonaState.Online,
        "Friend Away In Game" to EPersonaState.Away,
    )

    PluviaTheme {
        Surface {
            Column {
                friendData.onEachIndexed { index, entry ->
                    FriendItem(
                        friend = SteamFriend(
                            gameAppID = if (index < 3) 0 else index,
                            gameName = if (index < 3) "" else "Team Fortress 2",
                            id = index.toLong(),
                            name = entry.key,
                            nickname = if (index < 3) "" else entry.key,
                            relation = EFriendRelationship.Friend,
                            state = entry.value,
                            stateFlags = EPersonaStateFlag.from(512.times(index + 1)),
                        ),
                        onClick = { },
                        onLongClick = { },
                    )
                }
            }
        }
    }
}
