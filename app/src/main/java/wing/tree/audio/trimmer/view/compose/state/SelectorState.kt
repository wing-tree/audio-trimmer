package wing.tree.audio.trimmer.view.compose.state

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

data class SelectorState(
    val handleWidth: Dp = 16.dp,
    val barWidth: Dp = 3.dp,
    val color: Color = Color.Magenta.copy(alpha = 0.4f),
    val space: Dp = 2.dp,
    val offset: MutableState<IntOffset> = mutableStateOf(IntOffset.Zero),
    val width: MutableState<Dp> = mutableStateOf(98.dp)
) {
    private val _isPlaying: MutableState<Boolean> = mutableStateOf(false)
    val isPlaying: State<Boolean> get() = _isPlaying

    fun play() {
        _isPlaying.value = true
    }

    fun stop() {
        _isPlaying.value = false
    }
}
