package wing.tree.audio.trimmer.data.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import wing.tree.audio.trimmer.data.extension.isZero
import java.util.concurrent.TimeUnit.HOURS
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.MINUTES

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
            val hours = MILLISECONDS.toHours(value)
            val minutes = MILLISECONDS
                .toMinutes(value)
                .minus(HOURS.toMinutes(hours))

            val seconds = MILLISECONDS
                .toSeconds(value)
                .minus(HOURS.toSeconds(hours))
                .minus(MINUTES.toSeconds(minutes))

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

        val float: Float get() = value.toFloat() // TODO, move to app module, apply ext.
    }

    sealed interface Action {
        val audioFile: AudioFile

        data class Pause(override val audioFile: AudioFile) : Action
        data class Play(override val audioFile: AudioFile) : Action
        data class Trim(override val audioFile: AudioFile) : Action
    }

    companion object {
        const val EXTRA_AUDIO_FILE = "extra.AUDIO_FILE"
    }
}
