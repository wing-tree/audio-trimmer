package wing.tree.audio.trimmer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import wing.tree.audio.trimmer.AudioFileLoader
import wing.tree.audio.trimmer.extension.ZERO
import wing.tree.audio.trimmer.extension.long
import wing.tree.audio.trimmer.model.AudioFile
import wing.tree.audio.trimmer.ui.state.MainUiState
import wing.tree.audio.trimmer.ui.state.MainUiState.AudioFiles
import wing.tree.audio.trimmer.ui.state.MainUiState.ControlsState
import wing.tree.audio.trimmer.view.compose.state.PlayerState

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val expanded = MutableStateFlow<AudioFile?>(null)
    private val listener: Player.Listener = object : Player.Listener {
        @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)

            when (playbackState) {
                Player.STATE_IDLE -> {
                    job?.cancel()

                    playerState.update {
                        it.copy(
                            currentPosition = Long.ZERO,
                            duration = Long.ZERO,
                            state = PlayerState.State.IDLE,
                        )
                    }
                }

                Player.STATE_READY -> {
                    job?.cancel()
                    job = viewModelScope.launch {
                        while (isActive) {
                            val currentPosition = exoPlayer.currentPosition.coerceAtLeast(Long.ZERO)
                            val duration = exoPlayer.duration.coerceAtLeast(Long.ZERO)

                            playerState.update {
                                it.copy(
                                    currentPosition = currentPosition,
                                    duration = duration,
                                )
                            }

                            delay(10)
                        }
                    }
                }

                Player.STATE_ENDED -> {
                    playerState.update {
                        it.copy(
                            currentPosition = it.duration,
                            state = PlayerState.State.ENDED
                        )
                    }

                    job?.cancel()
                }

                else -> {
                    //println("zzzz111:$playbackState") // 버퍼상태
                }
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)

            when {
                isPlaying -> playerState.update {
                    it.copy(state = PlayerState.State.PLAYING)
                }

                exoPlayer.playbackState == Player.STATE_BUFFERING -> playerState.update {
                    it.copy(state = PlayerState.State.BUFFERING)
                }

                exoPlayer.playbackState == Player.STATE_READY -> playerState.update {
                    it.copy(state = PlayerState.State.PAUSE)
                }

                else -> playerState.update {
                    it.copy(state = PlayerState.State.ENDED)
                }
            }
        }
    }

    private val audioFileLoader = AudioFileLoader(application)
    private val currentAudioFile = MutableStateFlow<AudioFile?>(null)
    private val exoPlayer = ExoPlayer.Builder(application).build().apply {
        addListener(listener)
    }

    private val ioDispatcher = Dispatchers.IO
    private val playerState = MutableStateFlow(PlayerState.initialValue)
    private val sourceState = MutableStateFlow<AudioFiles.SourceState>(AudioFiles.SourceState.Loading)
    private val trimmedState = MutableStateFlow<AudioFiles.TrimmedState>(AudioFiles.TrimmedState.Loading)

    private val controlsState = combine(
        currentAudioFile,
        playerState,
    ) { currentAudioFile, playerState ->
        ControlsState(
            audioFile = currentAudioFile,
            playerState = playerState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = ControlsState.initialValue,
    )

    private var job: Job? = null

    val uiState = combine(
        controlsState,
        sourceState,
        trimmedState,
        expanded
    ) { controlsState, sourceState, trimmedState, expanded ->
        MainUiState(
            controlsState = controlsState,
            sourceState = sourceState,
            trimmedState = trimmedState,
            expanded = expanded,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = MainUiState.initialValue,
    )

    override fun onCleared() {
        with(exoPlayer) {
            removeListener(listener)
            release()
        }

        job?.cancel()

        super.onCleared()
    }

    fun collapse() {
        expanded.value = null
    }

    fun expand(audioFile: AudioFile) {
        expanded.value = audioFile
    }

    fun load() {
        viewModelScope.launch {
            launch {
                val audioFiles = withContext(ioDispatcher) {
                    audioFileLoader.sourceAudioFiles.toImmutableList()
                }

                sourceState.value = AudioFiles.SourceState.Content(audioFiles = audioFiles)
            }

            launch {
                val audioFiles = withContext(ioDispatcher) {
                    audioFileLoader.trimmedAudioFiles.toImmutableList()
                }

                trimmedState.value = AudioFiles.TrimmedState.Content(audioFiles = audioFiles)
            }
        }
    }

    fun pause() {
        exoPlayer.pause()
    }

    fun play(audioFile: AudioFile) {
        currentAudioFile.value = audioFile

        val mediaItem = MediaItem.fromUri(audioFile.uri)

        with(exoPlayer) {
            if (isPlaying) {
                stop()
            }

            job?.cancel()

            clearMediaItems()
            setMediaItem(mediaItem)
            prepare()
            play()
        }
    }

    fun resume() {
        exoPlayer.play()
    }

    fun seekTo(value: Float) {
        job?.cancel()

        exoPlayer.seekTo(value.long)
    }

    fun stop() {
        exoPlayer.stop()
        job?.cancel()

        currentAudioFile.value = null
        playerState.value = PlayerState.initialValue
    }
}
