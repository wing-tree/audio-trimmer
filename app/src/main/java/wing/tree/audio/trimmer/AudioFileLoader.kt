package wing.tree.audio.trimmer

import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.database.getLongOrNull
import wing.tree.audio.trimmer.extension.ZERO
import wing.tree.audio.trimmer.extension.long
import wing.tree.audio.trimmer.model.AudioFile

class AudioFileLoader(private val context: Context) {
    private val mediaMetadataRetriever = MediaMetadataRetriever()

    val sourceAudioFiles: List<AudioFile> get() = context.sourceAudioFiles
    val trimmedAudioFiles: List<AudioFile> get() = context.filesDir.listFiles()?.mapIndexedNotNull { index, file ->
        try {
            mediaMetadataRetriever.setDataSource(file.absolutePath)

            val mimeType = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)

            if (mimeType?.startsWith("audio/") == true) {
                val duration = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
                val size = file.length()

                AudioFile(
                    id = index.long,
                    displayName = file.name,
                    duration = AudioFile.Duration(duration ?: Long.ZERO),
                    size = size,
                    uri = Uri.fromFile(file)
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
        } ?: emptyList()

    private val Context.sourceAudioFiles: List<AudioFile> get() = run {
        val audioFiles = mutableListOf<AudioFile>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.ALBUM_ID,
        )

        // TODO make optional
        val selection = ""//""${MediaStore.Video.Media.DURATION} >= ?"
        val selectionArgs = arrayOf<String>()

        // TODO make optional.
        val sortOrder = "${MediaStore.Video.Media.DISPLAY_NAME} ASC"
        val query = contentResolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            sortOrder,
        )

        query?.use { cursor ->
            with(cursor) {
                while (moveToNext()) {
                    val id = getLong(getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                    val displayName = getString(getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME))
                    val duration = getLong(getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                    val size = getLong(getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE))
                    val albumId = getLongOrNull(getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
                    val uri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id,
                    )

                    val albumArt = albumId?.let {
                        ContentUris.withAppendedId(
                            Uri.parse("content://media/external/audio/albumart"),
                            it,
                        )
                    }

                    audioFiles.add(
                        AudioFile(
                            id = id,
                            displayName = displayName,
                            duration = AudioFile.Duration(duration),
                            size = size,
                            uri = uri,
                            albumArt = albumArt,
                        )
                    )
                }
            }
        }

        return audioFiles
    }
}
