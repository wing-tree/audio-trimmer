package wing.tree.audio.trimmer.view.compose.composable

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import wing.tree.audio.trimmer.extension.EMPTY
import wing.tree.audio.trimmer.model.AudioFile
import wing.tree.audio.trimmer.ui.state.MainUiState.AudioFiles.TrimmedState

@Composable
fun Trimmed(
    state: TrimmedState,
    expanded: AudioFile?,
    onItemClick: (AudioFile.Action) -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedContent(
        targetState = state,
        label = String.EMPTY,
        contentKey = {
            it::class
        },
    ) {
        when (it) {
            TrimmedState.Loading -> Box(modifier = modifier) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            is TrimmedState.Content -> LazyColumn(modifier = modifier) {
                itemsIndexed(
                    items = it.audioFiles,
                    key = { _, item ->
                        item.id
                    }
                ) { index, item ->
                    AudioFile(
                        audioFile = item,
                        expanded = item.id == expanded?.id,
                        onClick = onItemClick
                    )

                    if (index < it.audioFiles.lastIndex) {
                        Divider()
                    }
                }
            }

            is TrimmedState.Error -> {}
        }
    }
}
