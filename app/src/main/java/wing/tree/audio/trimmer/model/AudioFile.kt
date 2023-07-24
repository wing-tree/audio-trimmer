package wing.tree.audio.trimmer.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import wing.tree.audio.trimmer.extension.float
import wing.tree.audio.trimmer.extension.isZero
import java.util.concurrent.TimeUnit

@Parcelize
data class AudioFile(
    val id: Long,
    val displayName: String,
    val duration: Duration,
    val size: Long,
    val uri: Uri,
    val albumArt: Uri? = null,
) : Parcelable {
    val extension: String get() = displayName.substringAfterLast(".")

    @JvmInline
    @Parcelize
    value class Duration(val value: Long) : Parcelable {
        override fun toString(): String {
            val hours = TimeUnit.MILLISECONDS.toHours(value)
            val minutes = TimeUnit.MILLISECONDS
                .toMinutes(value)
                .minus(TimeUnit.HOURS.toMinutes(hours))

            val seconds = TimeUnit.MILLISECONDS
                .toSeconds(value)
                .minus(TimeUnit.HOURS.toSeconds(hours))
                .minus(TimeUnit.MINUTES.toSeconds(minutes))

            return if (hours.isZero()) {
                String.format(
                    format = "%02d:%02d",
                    minutes,
                    seconds,
                )
            } else {
                String.format(
                    format = "%02d:%02d:02d",
                    hours,
                    minutes,
                    seconds,
                )
            }
        }

        val float: Float get() = value.float
    }

    sealed interface Action {
        val audioFile: AudioFile

        data class Collapse(override val audioFile: AudioFile) : Action
        data class Expand(override val audioFile: AudioFile) : Action
        data class Pause(override val audioFile: AudioFile) : Action
        data class Play(override val audioFile: AudioFile) : Action
        data class Trim(override val audioFile: AudioFile) : Action
    }

    companion object {
        const val EXTRA_AUDIO_FILE = "extra.AUDIO_FILE"
    }
}
