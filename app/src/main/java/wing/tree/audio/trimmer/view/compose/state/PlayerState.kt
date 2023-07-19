package wing.tree.audio.trimmer.view.compose.state

import wing.tree.audio.trimmer.extension.ZERO

data class PlayerState(
    val currentPosition: Long,
    val duration: Long,
    val state: State,
) {
    enum class State {
        BUFFERING, ENDED, IDLE, PAUSE, PLAYING;
    }

    companion object {
        val initialValue = PlayerState(
            currentPosition = Long.ZERO,
            duration = Long.ZERO,
            state = State.IDLE,
        )
    }
}
