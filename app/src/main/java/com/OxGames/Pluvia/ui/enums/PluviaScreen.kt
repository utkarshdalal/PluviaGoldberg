package com.OxGames.Pluvia.ui.enums

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.outlined.ViewList
import androidx.compose.ui.graphics.vector.ImageVector
import com.OxGames.Pluvia.R

enum class PluviaScreen(@StringRes val title: Int, val icon: ImageVector) {
    LoginUser(
        title = R.string.login_user,
        icon = Icons.Filled.Password
    ),
    LoginTwoFactor(
        title = R.string.login_2fa,
        icon = Icons.Filled.Password
    ),
    Library(
        title = R.string.app_library,
        icon = Icons.Outlined.ViewList
    ),
    Downloads(
        title = R.string.app_downloads,
        icon = Icons.Filled.Download
    ),
    XServer(
        title = R.string.unknown_app,
        icon = Icons.Filled.QuestionMark
    );

    val menuNavRoutes: Array<PluviaScreen>?
        get() = when(this) {
            Library -> arrayOf(Library, Downloads)
            Downloads -> arrayOf(Library, Downloads)
            else -> null
        }
    val hasMenu: Boolean
        get() = menuNavRoutes != null
}