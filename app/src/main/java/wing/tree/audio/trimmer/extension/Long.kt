package wing.tree.audio.trimmer.extension

import wing.tree.audio.trimmer.data.extension.isZero
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.MINUTES
import java.util.concurrent.TimeUnit.HOURS

val Long.Companion.ZERO: Long get() = 0L
val Long.float: Float get() = toFloat()
val Long.format: String
    get() = run {
        val hours = TimeUnit.MILLISECONDS.toHours(this)
        val minutes = TimeUnit.MILLISECONDS
            .toMinutes(this)
            .minus(HOURS.toMinutes(hours))

        val seconds = TimeUnit.MILLISECONDS
            .toSeconds(this)
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
