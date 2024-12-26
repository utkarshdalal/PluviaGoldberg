package com.OxGames.Pluvia.ui.model

import androidx.lifecycle.ViewModel
import com.OxGames.Pluvia.ui.data.HomeState
import com.OxGames.Pluvia.ui.enums.PluviaDestination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class HomeViewModel : ViewModel() {
    private val _homeState = MutableStateFlow(HomeState())
    val homeState: StateFlow<HomeState> = _homeState.asStateFlow()

    fun onDestination(destination: PluviaDestination) {
        _homeState.update { currentState ->
            currentState.copy(currentDestination = destination)
        }
    }

    fun onConfirmExit(value: Boolean) {
        _homeState.update { currentState ->
            currentState.copy(confirmExit = value)
        }
    }
}
