package wing.tree.audio.trimmer.viewmodel

import android.app.Application
import android.content.ContentResolver
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import wing.tree.audio.trimmer.AudioTrimmer
import wing.tree.audio.trimmer.AudioTrimmerApplication
import wing.tree.audio.trimmer.data.model.AudioFile
import wing.tree.audio.trimmer.extension.toFile
import java.io.File

class AudioTrimmerViewModel(
    application: Application,
    intent: Intent, // TODO change to audiofile.
) : AndroidViewModel(application) {
    private val audioTrimmer = AudioTrimmer(application)
    private val contentResolver = application.contentResolver

    val audioFile = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        intent.getParcelableExtra(AudioFile.EXTRA_AUDIO_FILE, AudioFile::class.java)
    } else {
        @Suppress("DEPRECATION")
        intent.getParcelableExtra(AudioFile.EXTRA_AUDIO_FILE)
    }

    val ace = MutableStateFlow(emptyList<Int>())
    val ace2 = MutableStateFlow(0.0)
    val trimmedFile = MutableStateFlow<File?>(null)
    val trimAce = MutableStateFlow(emptyList<Int>())

    init {
        audioFile?.let {
            viewModelScope.launch(Dispatchers.Default) {
                when (it.uri.scheme) {
                    ContentResolver.SCHEME_CONTENT -> {
                        contentResolver.openInputStream(it.uri).use { inputStream ->
                            inputStream?.toFile(application)?.let { file ->
                                val result = audioTrimmer.getAmplitudes(
                                    inputFilePath = file.path,
                                    compressType = 4,
                                    framesPerSecond = 1,
                                )
                                ace.update {
                                    result.amplitudesAsList()
                                }
                                ace2.update {
                                    result.duration
                                }
                            }
                        }
                    }
                    else -> {
                        val result = audioTrimmer.getAmplitudes(
                            inputFilePath = it.uri.path ?: return@launch,
                            compressType = 4,
                            framesPerSecond = 1,
                        )

                        ace.update {
                            result.amplitudesAsList()
                        }
                        ace2.update {
                            result.duration
                        }
                    }
                }
            }
        }
    }

    fun trim(filename: String, start: Float, end: Float) {
        audioFile?.let {
            viewModelScope.launch(Dispatchers.Default) {
                contentResolver.openInputStream(it.uri).use { inputStream ->
                    inputStream?.toFile(getApplication())?.let { file ->
                        val filesDir = getApplication<AudioTrimmerApplication>().filesDir
                        val result = audioTrimmer.trimAudio(
                            inputFilePath = file.path,
                            outPath = "${filesDir.path}${File.separator}$filename.${it.extension}",
                            startTime = start.toInt(),
                            endTime = end.toInt(),
                        )

                        trimmedFile.value = File(result)
                        trimAce.value = audioTrimmer
                            .getAmplitudes(result, 4, 1)
                            .amplitudesAsList()
                    }
                }
            }
        }
    }
}
