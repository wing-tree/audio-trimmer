package wing.tree.audio.trimmer.viewmodel

import android.app.Application
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import wing.tree.audio.trimmer.data.constant.ZERO
import wing.tree.audio.trimmer.data.extension.long
import wing.tree.audio.trimmer.data.model.AudioFile

class TrimmedViewModel(application: Application) : AndroidViewModel(application = application) {
    fun load() {
        val mediaMetadataRetriever = MediaMetadataRetriever()

        getApplication<Application>().filesDir.listFiles()?.mapIndexedNotNull { index, file ->
            mediaMetadataRetriever.setDataSource(file.absolutePath)

            val mimetype = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)

            if (mimetype?.startsWith("audio/") == true) {
                val duration = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
                val size = file.length()

                AudioFile(
                    id = index.long,
                    displayName = file.name,
                    duration = AudioFile.Duration(duration ?: ZERO.long),
                    size = size,
                    uri = Uri.parse(file.path),
                )
            } else {
                null
            }
        } ?: emptyList()
    }
}