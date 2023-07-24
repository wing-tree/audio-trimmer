package wing.tree.audio.trimmer.extension

import android.app.Activity
import android.content.Intent
import wing.tree.audio.trimmer.model.AudioFile

fun Activity.shareAudioFile(audioFile: AudioFile) {
    val share = Intent(Intent.ACTION_SEND).apply {
        type = audioFile.mimeType

        putExtra(Intent.EXTRA_STREAM, audioFile.uri)
    }

    startActivity(Intent.createChooser(share, null))
}
