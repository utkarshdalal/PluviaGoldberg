package com.OxGames.Pluvia.ui.enums

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Groups
import androidx.compose.ui.graphics.vector.ImageVector
import com.OxGames.Pluvia.R

enum class PluviaDestination(@StringRes val title: Int, val icon: ImageVector) {
    Library(R.string.destination_library, Icons.AutoMirrored.Filled.ViewList),
    Downloads(R.string.destination_downloads, Icons.Filled.Download),
    Friends(R.string.destination_friends, Icons.Filled.Groups),
}

enum class PluviaScreen(@StringRes val title: Int) {
    LoginUser(title = R.string.login_user),
    Home(title = R.string.home),
    XServer(title = R.string.unknown_app),
    Settings(title = R.string.settings)
}