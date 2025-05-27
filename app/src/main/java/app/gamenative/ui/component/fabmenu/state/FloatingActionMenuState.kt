package app.gamenative.ui.component.fabmenu.state

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.SpringSpec
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CancellationException

/**
 * State of the floating action menu component that can be either opened or closed
 * Written with the help of Claude 3.5 Sonnet
 */
@Stable
class FloatingActionMenuState(
    initialValue: FloatingActionMenuValue,
    /**
     * Callback to confirm state changes.
     */
    private var confirmStateChange: (FloatingActionMenuValue) -> Boolean = { true },
) {
    /**
     * Whether the component is opening or closing.
     */
    var isAnimationRunning by mutableStateOf(false)
        private set

    /**
     * Current value of the state.
     */
    var currentValue by mutableStateOf(initialValue)
        private set

    /**
     * Target value of the state.
     */
    var targetValue by mutableStateOf(initialValue)
        private set

    /**
     * Whether the menu is open.
     */
    val isOpen: Boolean
        get() = currentValue == FloatingActionMenuValue.Open

    /**
     * Whether the menu is closed.
     */
    val isClosed: Boolean
        get() = currentValue == FloatingActionMenuValue.Closed

    /**
     * Open the menu with animation.
     * @throws [CancellationException] if the animation is interrupted
     */
    fun open(animationSpec: AnimationSpec<Float> = SpringSpec()) {
        if (!confirmStateChange(FloatingActionMenuValue.Open)) return
        targetValue = FloatingActionMenuValue.Open
        isAnimationRunning = true
        try {
            // Animation logic would go here
            // You would typically use a Animatable or animate*AsState
            currentValue = FloatingActionMenuValue.Open
        } finally {
            isAnimationRunning = false
        }
    }

    /**
     * Close the menu with animation.
     * @throws [CancellationException] if the animation is interrupted
     */
    fun close(animationSpec: AnimationSpec<Float> = SpringSpec()) {
        if (!confirmStateChange(FloatingActionMenuValue.Closed)) return
        targetValue = FloatingActionMenuValue.Closed
        isAnimationRunning = true
        try {
            // Animation logic would go here
            // You would typically use a Animatable or animate*AsState
            currentValue = FloatingActionMenuValue.Closed
        } finally {
            isAnimationRunning = false
        }
    }

    companion object {
        /**
         * The default [Saver] implementation for [FloatingActionMenuState].
         */
        fun Saver(
            confirmStateChange: (FloatingActionMenuValue) -> Boolean,
        ): Saver<FloatingActionMenuState, String> = Saver(
            save = { state -> state.currentValue.name },
            restore = { savedValue ->
                FloatingActionMenuState(
                    initialValue = FloatingActionMenuValue.valueOf(savedValue),
                    confirmStateChange = confirmStateChange,
                )
            },
        )
    }
}

/**
 * Remember [FloatingActionMenuState] with the given initial value.
 */
@Composable
fun rememberFloatingActionMenuState(
    initialValue: FloatingActionMenuValue = FloatingActionMenuValue.Closed,
    confirmStateChange: (FloatingActionMenuValue) -> Boolean = { true },
): FloatingActionMenuState {
    return rememberSaveable(saver = FloatingActionMenuState.Saver(confirmStateChange)) {
        FloatingActionMenuState(initialValue, confirmStateChange)
    }
}
