package wing.tree.audio.trimmer.ui.state

import kotlinx.collections.immutable.ImmutableList
import wing.tree.audio.trimmer.model.AudioFile
import wing.tree.audio.trimmer.view.compose.state.PlayerState

data class MainUiState(
    val controlsState: ControlsState,
    val sourceState: AudioFiles.SourceState,
    val trimmedState: AudioFiles.TrimmedState,
    val expanded: AudioFile? = null
) {
    sealed interface AudioFiles {
        sealed interface SourceState {
            object Loading : SourceState
            data class Content(val audioFiles: ImmutableList<AudioFile>) : SourceState
            data class Error(val cause: Throwable) : SourceState
        }

        sealed interface TrimmedState {
            object Loading : TrimmedState
            data class Content(val audioFiles: ImmutableList<AudioFile>) : TrimmedState
            data class Error(val cause: Throwable) : TrimmedState
        }
    }

    data class ControlsState(
        val audioFile: AudioFile?,
        val playerState: PlayerState,
    ) {
        val currentPosition: Long get() = playerState.currentPosition
        val duration: Long get() = playerState.duration

        enum class Action {
            PAUSE, PLAY, REPLAY, STOP
        }

        companion object {
            val initialValue = ControlsState(
                audioFile = null,
                playerState = PlayerState.initialValue,
            )
        }
    }

    companion object {
        val initialValue = MainUiState(
            sourceState = AudioFiles.SourceState.Loading,
            trimmedState = AudioFiles.TrimmedState.Loading,
            controlsState = ControlsState.initialValue,
        )
    }
}
