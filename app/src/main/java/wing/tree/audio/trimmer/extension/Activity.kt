package wing.tree.audio.trimmer.extension

import android.app.Activity
import android.content.Intent
import android.net.Uri

fun Activity.shareAudio(uri: Uri) {
    val share = Intent(Intent.ACTION_SEND).apply {
        type = "audio/*"

        putExtra(Intent.EXTRA_STREAM, uri)
    }

    startActivity(Intent.createChooser(share, "Share Sound File"))
}
