package wing.tree.audio.trimmer.view.compose.composable

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import wing.tree.audio.trimmer.data.model.AudioFile
import wing.tree.audio.trimmer.extension.EMPTY
import wing.tree.audio.trimmer.ui.state.MainUiState.AudioFiles.TrimmedState

@Composable
fun Trimmed(
    state: TrimmedState,
    onItemClick: (AudioFile.Action) -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedContent(
        targetState = state,
        label = String.EMPTY,
        contentKey = {
            it::class
        },
    ) { targetState ->
        when (targetState) {
            TrimmedState.Loading -> Box(modifier = modifier) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            is TrimmedState.Content -> LazyColumn(modifier = modifier) {
                items(targetState.audioFiles) {
                    AudioFile(
                        audioFile = it,
                        onClick = onItemClick,
                    )
                }
            }

            is TrimmedState.Error -> {}
        }
    }
}
