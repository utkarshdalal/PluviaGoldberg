package com.OxGames.Pluvia.ui.screen.chat

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.OxGames.Pluvia.data.Emoticon
import com.OxGames.Pluvia.data.FriendMessage
import com.OxGames.Pluvia.data.SteamFriend
import com.OxGames.Pluvia.db.dao.EmoticonDao
import com.OxGames.Pluvia.db.dao.FriendMessagesDao
import com.OxGames.Pluvia.db.dao.SteamFriendDao
import com.OxGames.Pluvia.service.SteamService
import com.OxGames.Pluvia.ui.component.topbar.BackButton
import com.OxGames.Pluvia.ui.theme.PluviaTheme
import com.OxGames.Pluvia.ui.util.ListItemImage
import com.OxGames.Pluvia.utils.SteamUtils
import com.OxGames.Pluvia.utils.getAvatarURL
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.dragonbra.javasteam.enums.EPersonaState
import `in`.dragonbra.javasteam.enums.EPersonaStateFlag
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

data class ChatState(
    val friend: SteamFriend = SteamFriend(0),
    val messages: List<FriendMessage> = listOf(),
    val emoticons: List<Emoticon> = listOf(),
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val friendDao: SteamFriendDao,
    private val messagesDao: FriendMessagesDao,
    private val emoticonDao: EmoticonDao,
) : ViewModel() {

    private val _chatState = MutableStateFlow(ChatState())
    val chatState: StateFlow<ChatState> = _chatState.asStateFlow()

    private var emoticonJob: Job? = null
    private var friendJob: Job? = null
    private var messagesJob: Job? = null

    override fun onCleared() {
        super.onCleared()

        Timber.d("onCleared")

        emoticonJob?.cancel()
        friendJob?.cancel()
        messagesJob?.cancel()
    }

    fun setFriend(id: Long) {
        viewModelScope.launch {
            // Since were initiating a chat, refresh our list of emoticons and stickers
            SteamService.getEmoticonList()
            SteamService.getRecentMessages(id)
            SteamService.ackMessage(id)
        }

        emoticonJob = viewModelScope.launch {
            emoticonDao.getAll().collect { list ->
                Timber.tag("ChatViewModel").d("Got Emotes: ${list.size}")
                _chatState.update { it.copy(emoticons = list) }
            }
        }

        friendJob = viewModelScope.launch {
            friendDao.findFriend(id).collect { friend ->
                if (friend == null) {
                    throw RuntimeException("Friend is null and cannot proceed")
                }
                Timber.tag("ChatViewModel").d("Friend update $friend")
                _chatState.update { it.copy(friend = friend) }
            }
        }

        messagesJob = viewModelScope.launch {
            messagesDao.getAllMessagesForFriend(id).collect { list ->
                Timber.tag("ChatViewModel").d("New messages ${list.size}")
                _chatState.update { it.copy(messages = list) }
            }
        }

        Timber.d("Chatting with $id")
    }
}

@Composable
fun ChatScreen(
    friendId: Long,
    viewModel: ChatViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val state by viewModel.chatState.collectAsStateWithLifecycle()
    viewModel.setFriend(friendId)

    ChatScreenContent(
        steamFriend = state.friend,
        messages = state.messages,
        onBack = onBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatScreenContent(
    steamFriend: SteamFriend,
    messages: List<FriendMessage>,
    onBack: () -> Unit,
) {
    val snackbarHost = remember { SnackbarHostState() }
    val scrollState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Needed?
    val topBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topBarState)

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        // Exclude ime and navigation bar padding so this can be added by the ChatInput composable
        contentWindowInsets = ScaffoldDefaults
            .contentWindowInsets
            .exclude(WindowInsets.navigationBars)
            .exclude(WindowInsets.ime),
        snackbarHost = { SnackbarHost(snackbarHost) },
        topBar = {
            CenterAlignedTopAppBar(
                scrollBehavior = scrollBehavior,
                title = {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ListItemImage(
                            image = { steamFriend.avatarHash.getAvatarURL() },
                            size = 40.dp,
                        )

                        Spacer(modifier = Modifier.size(12.dp))

                        Column {
                            CompositionLocalProvider(
                                LocalContentColor provides steamFriend.statusColor,
                                LocalTextStyle provides TextStyle(
                                    lineHeight = 1.em,
                                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                                ),
                            ) {
                                Text(
                                    overflow = TextOverflow.Ellipsis,
                                    fontSize = 20.sp,
                                    maxLines = 1,
                                    text = buildAnnotatedString {
                                        append(steamFriend.nameOrNickname)
                                        if (steamFriend.statusIcon != null) {
                                            append(" ")
                                            appendInlineContent("icon", "[icon]")
                                        }
                                    },
                                    inlineContent = mapOf(
                                        "icon" to InlineTextContent(
                                            Placeholder(
                                                width = 16.sp,
                                                height = 16.sp,
                                                placeholderVerticalAlign = PlaceholderVerticalAlign.Center,
                                            ),
                                            children = {
                                                steamFriend.statusIcon?.let {
                                                    Icon(imageVector = it, tint = Color.LightGray, contentDescription = it.name)
                                                }
                                            },
                                        ),
                                    ),
                                )

                                Text(
                                    text = steamFriend.isPlayingGameName,
                                    overflow = TextOverflow.Ellipsis,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    color = LocalContentColor.current.copy(alpha = .75f),
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    BackButton(onClick = onBack)
                },
                actions = {
                    IconButton(
                        onClick = {
                            // TODO
                            val msg = "View profile not implemented!\nTry long pressing a friend in the friends list?"
                            scope.launch { snackbarHost.showSnackbar(msg) }
                        },
                        content = { Icon(imageVector = Icons.Default.Person, contentDescription = null) },
                    )
                },
            )
        },
    ) { paddingValues ->
        // TODO Typing bar + Send + Emoji selector
        // TODO scroll to bottom
        // TODO scroll to bottom if we're ~3 messages slightly scrolled.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),

        ) {
            // Surround with Box in order to "Jump to Bottom"
            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = scrollState,
                    reverseLayout = true,
                ) {
                    stickyHeader {
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                        ) {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                fontSize = 12.sp,
                                text = "Chatting is still an early feature.\n" +
                                    "Please report any issues in the project repo.",
                            )
                        }
                    }
                    items(messages, key = { it.id }) { msg ->
                        ChatBubble(
                            message = msg.message,
                            timestamp = SteamUtils.fromSteamTime(msg.timestamp),
                            fromLocal = msg.fromLocal,
                        )
                    }
                }
            }

            ChatInput(
                // let this element handle the padding so that the elevation is shown behind the
                // navigation bar
                modifier = Modifier
                    .navigationBarsPadding()
                    .imePadding(),
                onMessageSent = { },
                resetScroll = { },
            )
        }
    }
}

@Composable
private fun NoChatHistoryBox() {
    Box(contentAlignment = Alignment.Center) {
        Surface(
            modifier = Modifier.padding(horizontal = 24.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = 8.dp,
        ) {
            Text(
                modifier = Modifier.padding(24.dp),
                text = "No chat history",
            )
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview_ChatScreenContent() {
    PluviaTheme {
        ChatScreenContent(
            steamFriend = SteamFriend(
                id = 76561198003805806,
                state = EPersonaState.Online,
                avatarHash = "cfc54391f2f2ba745b701ad1287f73e50dc26d74",
                name = "Lossy",
                nickname = "Lossy with a nickname which should clip",
                gameAppID = 440,
                stateFlags = EPersonaStateFlag.from(2048),
            ),
            messages = List(20) {
                FriendMessage(
                    id = it.plus(1).toLong(),
                    steamIDFriend = 76561198003805806,
                    fromLocal = it % 3 == 0,
                    message = "Hey!, ".repeat(it.plus(1).times(1)),
                    // lowPriority = false,
                    timestamp = 1737438789,
                )
            },
            onBack = { },
        )
    }
}
