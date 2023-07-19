package wing.tree.audio.trimmer.view.compose.composable

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import wing.tree.audio.trimmer.data.model.AudioFile
import wing.tree.audio.trimmer.extension.int

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AudioFile(
    audioFile: AudioFile,
    onClick: (AudioFile.Action) -> Unit,
    modifier: Modifier = Modifier
) {
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
                        onClick(AudioFile.Action.Play(audioFile))
                    },
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = null,
                    )
                }
            }
        }
    )
}
