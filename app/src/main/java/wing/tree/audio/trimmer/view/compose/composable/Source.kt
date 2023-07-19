package wing.tree.audio.trimmer.view.compose.composable

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import wing.tree.audio.trimmer.data.model.AudioFile
import wing.tree.audio.trimmer.extension.EMPTY
import wing.tree.audio.trimmer.ui.state.MainUiState.AudioFiles.SourceState

@Composable
fun Source(
    state: SourceState,
    onItemClick: (AudioFile.Action) -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedContent(
        targetState = state,
        modifier = modifier,
        transitionSpec = {
            fadeIn() togetherWith fadeOut()
        },
        label = String.EMPTY,
        contentKey = {
            it::class
        },
    ) { targetState ->
        when (targetState) {
            SourceState.Loading -> Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            is SourceState.Content -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(
                    items = targetState.audioFiles,
                    key = AudioFile::id,
                ) {
                    AudioFile(
                        audioFile = it,
                        onClick = onItemClick,
                    )
                }
            }

            is SourceState.Error -> {}
        }
    }
}
