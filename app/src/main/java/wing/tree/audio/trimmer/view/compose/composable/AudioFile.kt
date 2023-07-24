package wing.tree.audio.trimmer.view.compose.composable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import wing.tree.audio.trimmer.extension.EMPTY
import wing.tree.audio.trimmer.extension.int
import wing.tree.audio.trimmer.model.AudioFile

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AudioFile(
    audioFile: AudioFile,
    expanded: Boolean,
    onClick: (AudioFile.Action) -> Unit,
    modifier: Modifier = Modifier
) {
    println("mmmmmmmmmxxxxxxxxxx")
    Column {
        ListItem(
            headlineContent = {
                Text(
                    text = audioFile.displayName,
                    modifier = Modifier.basicMarquee()
                )
            },
            modifier = modifier.clickable {
                onClick(AudioFile.Action.Trim(audioFile))
            },
            supportingContent = {
                Text(text = "${audioFile.duration}")
            },
            leadingContent = {
                val size = with(LocalDensity.current) {
                    40.dp.toPx()
                }

                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(audioFile.albumArt)
                        .size(size.int)
                        .crossfade(true)
                        .build(),
                    contentDescription = null
                )
            },
            trailingContent = {
                Row {
                    IconButton(
                        onClick = {
                            if (expanded) {
                                onClick(AudioFile.Action.Collapse(audioFile))
                            } else {
                                onClick(AudioFile.Action.Expand(audioFile))
                            }
                        },
                    ) {
                        val rotationZ by animateFloatAsState(
                            targetValue = if (expanded) {
                                180.0F
                            } else {
                                0.0F
                            },
                            label = String.EMPTY
                        )

                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.graphicsLayer {
                                this.rotationZ = rotationZ
                            }
                        )
                    }
                }
            }
        )

        AnimatedVisibility(visible = expanded) {
            Row {
                IconButton(
                    onClick = {
                        onClick(AudioFile.Action.Play(audioFile))
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                }
            }
        }
    }
}
