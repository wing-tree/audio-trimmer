package wing.tree.audio.trimmer.viewmodel.factory

import android.app.Application
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import wing.tree.audio.trimmer.viewmodel.AudioTrimmerViewModel

class AudioTrimmerViewModelFactory(
    private val application: Application,
    private val intent: Intent,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return AudioTrimmerViewModel(
            application = application,
            intent = intent,
        ) as T
    }
}
