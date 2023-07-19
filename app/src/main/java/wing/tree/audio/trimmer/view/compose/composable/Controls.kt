package wing.tree.audio.trimmer.view.compose.composable

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import wing.tree.audio.trimmer.R
import wing.tree.audio.trimmer.data.constant.ZERO
import wing.tree.audio.trimmer.data.extension.float
import wing.tree.audio.trimmer.extension.EMPTY
import wing.tree.audio.trimmer.extension.ONE
import wing.tree.audio.trimmer.extension.float
import wing.tree.audio.trimmer.extension.format
import wing.tree.audio.trimmer.ui.state.MainUiState.ControlsState
import wing.tree.audio.trimmer.ui.state.MainUiState.ControlsState.Action
import wing.tree.audio.trimmer.view.compose.state.PlayerState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Controls(
    state: ControlsState,
    onClick: (Action) -> Unit,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        tonalElevation = NavigationBarDefaults.Elevation,
    ) {
        val audioFile = state.audioFile
        val playState = state.playerState

        var currentPosition by remember(state.currentPosition) {
            mutableFloatStateOf(state.currentPosition.float)
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AsyncImage(
                    model = audioFile?.albumArt,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                )

                Text(
                    text = audioFile?.displayName ?: String.EMPTY,
                    modifier = Modifier
                        .weight(Float.ONE)
                        .basicMarquee(),
                )

                Row {
                    IconButton(
                        onClick = {
                            when (playState.state) {
                                PlayerState.State.ENDED -> onClick(Action.REPLAY)
                                PlayerState.State.PAUSE -> onClick(Action.PLAY)
                                PlayerState.State.PLAYING -> onClick(Action.PAUSE)
                                else -> {

                                }
                            }
                        }
                    ) {
                        when (playState.state) {
                            PlayerState.State.BUFFERING,
                            PlayerState.State.PLAYING -> Icon(
                                painter = painterResource(id = R.drawable.round_pause_24),
                                contentDescription = null
                            )

                            PlayerState.State.ENDED,
                            PlayerState.State.IDLE,
                            PlayerState.State.PAUSE -> Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = null
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            onClick(Action.STOP)
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = null,
                        )
                    }
                }
            }

            Slider(
                value = currentPosition,
                onValueChange = {
                    currentPosition = it

                    onValueChange(currentPosition)
                },
                modifier = Modifier.fillMaxWidth(),
                valueRange = ZERO.float..state.duration.toFloat(),
                onValueChangeFinished = {
                    onValueChangeFinished(currentPosition)
                }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = state.currentPosition.format,
                    color = colorScheme.primary,
                )

                Text(text = state.duration.format)
            }
        }
    }
}
