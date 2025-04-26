package com.utkarshdalal.PluviaGoldberg.ui.model

import androidx.lifecycle.ViewModel
import com.utkarshdalal.PluviaGoldberg.ui.data.HomeState
import com.utkarshdalal.PluviaGoldberg.ui.enums.HomeDestination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class HomeViewModel : ViewModel() {
    private val _homeState = MutableStateFlow(HomeState())
    val homeState: StateFlow<HomeState> = _homeState.asStateFlow()

    fun onDestination(destination: HomeDestination) {
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
