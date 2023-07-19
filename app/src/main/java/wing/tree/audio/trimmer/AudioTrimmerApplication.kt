package wing.tree.audio.trimmer

import android.app.Application

class AudioTrimmerApplication : Application() {
    companion object {
        init {
            System.loadLibrary("native-lib")
        }
    }
}
