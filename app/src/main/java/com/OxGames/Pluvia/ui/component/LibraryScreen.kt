package com.OxGames.Pluvia.ui.component

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.LifecycleOwner
import com.OxGames.Pluvia.PluviaApp
import com.OxGames.Pluvia.SteamService
import com.OxGames.Pluvia.data.AppInfo
import com.OxGames.Pluvia.enums.AppType
import com.OxGames.Pluvia.events.SteamEvent
import java.util.EnumSet

@Composable
fun LibraryScreen(
    onAppClick: (appId: Int) -> Unit,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {
    var appsList by remember { mutableStateOf<List<AppInfo>>(SteamService.getAppList(EnumSet.of(AppType.game))) }

    DisposableEffect(lifecycleOwner) {
        val onAppInfoReceived: (SteamEvent.AppInfoReceived) -> Unit = {
            appsList = SteamService.getAppList(EnumSet.of(AppType.game))
            Log.d("LibraryScreen", "Updating games list with ${appsList.count()} item(s)")
        }

        PluviaApp.events.on<SteamEvent.AppInfoReceived>(onAppInfoReceived)

        onDispose {
            PluviaApp.events.off<SteamEvent.AppInfoReceived>(onAppInfoReceived)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        items(appsList) {
            AppItem(
                appInfo = it,
                onClick = {
                    onAppClick(it.appId)
                }
            )
        }
    }
}