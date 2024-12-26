package com.OxGames.Pluvia.ui.enums

import androidx.annotation.StringRes
import com.OxGames.Pluvia.R

/**
 * Destinations for top level screens, excluding home screen destinations.
 */
enum class PluviaScreen(@StringRes val title: Int) {
    LoginUser(title = R.string.login_user),
    Home(title = R.string.home),
    XServer(title = R.string.unknown_app),
    Settings(title = R.string.settings),
}
