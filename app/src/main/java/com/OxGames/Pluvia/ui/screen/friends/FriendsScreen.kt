package com.OxGames.Pluvia.ui.screen.friends

import android.content.res.Configuration
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.outlined.Games
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldDestinationItem
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowWidthSizeClass
import com.OxGames.Pluvia.R
import com.OxGames.Pluvia.data.SteamFriend
import com.OxGames.Pluvia.ui.component.BBCodeText
import com.OxGames.Pluvia.ui.component.LoadingScreen
import com.OxGames.Pluvia.ui.component.dialog.GamesListDialog
import com.OxGames.Pluvia.ui.component.topbar.AccountButton
import com.OxGames.Pluvia.ui.component.topbar.BackButton
import com.OxGames.Pluvia.ui.data.FriendsState
import com.OxGames.Pluvia.ui.model.FriendsViewModel
import com.OxGames.Pluvia.ui.theme.PluviaTheme
import com.OxGames.Pluvia.utils.getAvatarURL
import com.OxGames.Pluvia.utils.getProfileUrl
import com.materialkolor.ktx.isLight
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil.CoilImage
import `in`.dragonbra.javasteam.enums.EPersonaState
import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.steam.handlers.steamfriends.callback.ProfileInfoCallback
import `in`.dragonbra.javasteam.types.SteamID
import java.util.Date

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun FriendsScreen(
    viewModel: FriendsViewModel = hiltViewModel(),
    onSettings: () -> Unit,
    onLogout: () -> Unit,
) {
    val navigator = rememberListDetailPaneScaffoldNavigator<Unit>()
    val state by viewModel.friendsState.collectAsStateWithLifecycle()

    val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    FriendsScreenContent(
        navigator = navigator,
        state = state,
        onFriendClick = viewModel::observeSelectedFriend,
        onBack = { onBackPressedDispatcher?.onBackPressed() },
        onHeaderAction = viewModel::onHeaderAction,
        onSettings = onSettings,
        onLogout = onLogout,
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun FriendsScreenContent(
    navigator: ThreePaneScaffoldNavigator<Unit>,
    state: FriendsState,
    onFriendClick: (Long) -> Unit,
    onHeaderAction: (String) -> Unit,
    onBack: () -> Unit,
    onSettings: () -> Unit,
    onLogout: () -> Unit,
) {
    val listState = rememberLazyListState() // Hoisted high to preserve state
    val snackbarHost = remember { SnackbarHostState() }

    var showGamesDialog by remember { mutableStateOf(false) }

    GamesListDialog(
        visible = showGamesDialog,
        list = state.profileFriendGames,
        onDismissRequest = {
            showGamesDialog = false
        },
    )

    // Pretty much the same as 'NavigableListDetailPaneScaffold'
    BackHandler(navigator.canNavigateBack(BackNavigationBehavior.PopUntilContentChange)) {
        navigator.navigateBack(BackNavigationBehavior.PopUntilContentChange)
    }

    ListDetailPaneScaffold(
        modifier = Modifier.displayCutoutPadding(),
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        listPane = {
            AnimatedPane {
                FriendsListPane(
                    state = state,
                    listState = listState,
                    snackbarHost = snackbarHost,
                    onBack = onBack,
                    onHeaderAction = onHeaderAction,
                    onFriendClick = {
                        onFriendClick(it.id)
                        navigator.navigateTo(ListDetailPaneScaffoldRole.Detail)
                    },
                    onSettings = onSettings,
                    onLogout = onLogout,
                )
            }
        },
        detailPane = {
            AnimatedPane {
                FriendsDetailPane(
                    state = state,
                    onBack = onBack,
                    onShowGames = {
                        showGamesDialog = true
                    },
                )
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FriendsListPane(
    state: FriendsState,
    snackbarHost: SnackbarHostState,
    listState: LazyListState,
    onBack: () -> Unit,
    onFriendClick: (SteamFriend) -> Unit,
    onHeaderAction: (String) -> Unit,
    onSettings: () -> Unit,
    onLogout: () -> Unit,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Friends") },
                actions = {
                    AccountButton(
                        onSettings = onSettings,
                        onLogout = onLogout,
                    )
                },
                navigationIcon = { BackButton(onClick = onBack) },
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(bottom = 72.dp), // Extra space for fab
        ) {
            state.friendsList.forEach { (key, value) ->
                stickyHeader {
                    StickyHeaderItem(
                        isCollapsed = key in state.collapsedListSections,
                        header = key,
                        count = value.size,
                        onHeaderAction = { onHeaderAction(key) },
                    )
                }

                if (key !in state.collapsedListSections) {
                    itemsIndexed(value, key = { _, item -> item.id }) { idx, friend ->
                        FriendItem(
                            modifier = Modifier.animateItem(),
                            friend = friend,
                            onClick = { /* TODO */ },
                            onLongClick = { onFriendClick(friend) },
                        )

                        if (idx < value.lastIndex) {
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FriendsDetailPane(
    state: FriendsState,
    onBack: () -> Unit,
    onShowGames: () -> Unit,
) {
    Surface {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
            content = {
                if (state.profileFriend == null) {
                    DefaultDetailsScreen()
                } else {
                    ProfileDetailsScreen(
                        state = state,
                        onBack = onBack,
                        onShowGames = onShowGames,
                    )
                }
            },
        )
    }
}

@Composable
private fun DefaultDetailsScreen() {
    Surface(
        modifier = Modifier.padding(horizontal = 24.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 8.dp,
    ) {
        Text(
            modifier = Modifier.padding(24.dp),
            text = "Select a friend to their profile",
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileDetailsScreen(
    state: FriendsState,
    onBack: () -> Unit,
    onShowGames: () -> Unit,
) {
    val scrollState = rememberScrollState()
    val windowWidth = currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass

    Scaffold(
        topBar = {
            // Show Top App Bar when in Compact or Medium screen space.
            if (windowWidth == WindowWidthSizeClass.COMPACT || windowWidth == WindowWidthSizeClass.MEDIUM) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Profile",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    navigationIcon = {
                        BackButton(onClick = onBack)
                    },
                )
            }
        },
    ) { paddingValues ->
        val uriHandler = LocalUriHandler.current
        val context = LocalContext.current
        val isLight = MaterialTheme.colorScheme.background.isLight()

        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            CoilImage(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .clip(CircleShape)
                    .background(Color.DarkGray)
                    .size(92.dp),
                imageModel = { state.profileFriend!!.avatarHash.getAvatarURL() },
                imageOptions = ImageOptions(
                    contentScale = ContentScale.Crop,
                    contentDescription = null,
                ),
                loading = { CircularProgressIndicator() },
                failure = { Icon(Icons.Filled.QuestionMark, null) },
                previewPlaceholder = painterResource(R.drawable.icon_mono_foreground),
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = state.profileFriend!!.nameOrNickname,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.headlineLarge,
            )

            Text(
                text = state.profileFriend.isPlayingGameName,
                color = if (isLight) Color.Unspecified else state.profileFriend.statusColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleLarge,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ProfileButton(
                    icon = Icons.AutoMirrored.Outlined.Chat,
                    text = "Chat",
                    onClick = {
                        // TODO chat
                    },
                )
                Spacer(modifier = Modifier.width(16.dp))
                ProfileButton(
                    icon = Icons.Outlined.Person,
                    text = "Profile",
                    onClick = {
                        uriHandler.openUri(state.profileFriend.id.getProfileUrl())
                    },
                )
                Spacer(modifier = Modifier.width(16.dp))
                ProfileButton(
                    icon = Icons.Outlined.Games,
                    text = "Games",
                    onClick = onShowGames,
                )
                Spacer(modifier = Modifier.width(16.dp))
                ProfileButton(
                    icon = Icons.Outlined.MoreVert,
                    text = "More",
                    onClick = {
                        // TODO more options, such as:
                        //  Friend management: Remove, Block, Unblock
                        //  Notification settings
                        val msg = "'More' not available yet"
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                    },
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isLight) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSecondary,
                ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.Start,
                ) {
                    if (state.profileFriendInfo == null) {
                        LoadingScreen()
                    } else {
                        // 'headline' doesn't seem to be used anymore
                        CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyMedium) {
                            with(state.profileFriendInfo) {
                                // Meh...
                                if (realName.isNotEmpty()) Text(text = "Name: $realName")
                                if (cityName.isNotEmpty()) Text(text = "City: $cityName")
                                if (stateName.isNotEmpty()) Text(text = "State: $stateName")
                                if (stateName.isNotEmpty()) Text(text = "Country: $countryName")
                                Text(text = "Created: $timeCreated")
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(text = "Summary:")
                                BBCodeText(text = summary)
                            }
                        }
                    }
                }
            }

            // Bottom scroll padding
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
internal class FriendsScreenPreview :
    PreviewParameterProvider<ThreePaneScaffoldDestinationItem<Unit>> {
    override val values: Sequence<ThreePaneScaffoldDestinationItem<Unit>>
        get() = sequenceOf(
            ThreePaneScaffoldDestinationItem(ListDetailPaneScaffoldRole.List),
            ThreePaneScaffoldDestinationItem(ListDetailPaneScaffoldRole.Detail),
        )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
    device = "spec:parent=pixel_5,orientation=landscape",
)
@Composable
private fun Preview_FriendsScreenContent(
    @PreviewParameter(FriendsScreenPreview::class) state: ThreePaneScaffoldDestinationItem<Unit>,
) {
    val navigator = rememberListDetailPaneScaffoldNavigator(
        initialDestinationHistory = listOf(state),
    )

    PluviaTheme {
        FriendsScreenContent(
            navigator = navigator,
            state = FriendsState(
                friendsList = mapOf(
                    "TEST A" to List(3) { SteamFriend(id = it.toLong()) },
                    "TEST B" to List(3) { SteamFriend(id = it.toLong() + 5) },
                    "TEST C" to List(3) { SteamFriend(id = it.toLong() + 10) },
                ),
                profileFriend = SteamFriend(
                    id = 123L,
                    nickname = "Pluvia".repeat(3).trimEnd(),
                    state = EPersonaState.Online,
                    gameName = "Left 4 Dead 2",
                ),
                profileFriendInfo = ProfileInfoCallback(
                    result = EResult.OK,
                    steamID = SteamID(123L),
                    timeCreated = Date(9988776655 * 1000L),
                    realName = "Pluvia",
                    cityName = "Pluvia Town",
                    stateName = "Pluviaville",
                    countryName = "United Pluvia",
                    headline = "",
                    summary = "A [emoticon]roar[/emoticon] Fake Summary ːsteamboredː ːsteamthisː",
                ),
            ),
            onFriendClick = { },
            onHeaderAction = { },
            onBack = { },
            onSettings = { },
            onLogout = { },
        )
    }
}
