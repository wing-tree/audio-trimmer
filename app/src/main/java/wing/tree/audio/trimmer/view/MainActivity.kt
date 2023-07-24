package wing.tree.audio.trimmer.view

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import wing.tree.audio.trimmer.R
import wing.tree.audio.trimmer.extension.EMPTY
import wing.tree.audio.trimmer.extension.ZERO
import wing.tree.audio.trimmer.extension.composable
import wing.tree.audio.trimmer.extension.shareAudioFile
import wing.tree.audio.trimmer.model.AudioFile
import wing.tree.audio.trimmer.model.AudioFile.Companion.EXTRA_AUDIO_FILE
import wing.tree.audio.trimmer.model.Route
import wing.tree.audio.trimmer.ui.state.MainUiState.ControlsState
import wing.tree.audio.trimmer.ui.theme.AudioTrimmerTheme
import wing.tree.audio.trimmer.view.compose.composable.Controls
import wing.tree.audio.trimmer.view.compose.composable.NavHost
import wing.tree.audio.trimmer.view.compose.composable.Source
import wing.tree.audio.trimmer.view.compose.composable.Trimmed
import wing.tree.audio.trimmer.viewmodel.MainViewModel
import wing.tree.audio.trimmer.viewmodel.factory.MainViewModelFactory

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainViewModel> {
        MainViewModelFactory(application = application)
    }

    private val permissions: Array<String> get() = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
            arrayOf(
                android.Manifest.permission.READ_MEDIA_AUDIO,
                android.Manifest.permission.READ_MEDIA_IMAGES,
            )
        
        else -> arrayOf(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        )
    }

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            val granted = it.values.all {
                true
            }

            if (granted) {

            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        when {
            checkSelfPermissions(permissions) -> {

            }

            shouldShowRequestPermissionsRationale(permissions) -> {

            }

            else -> activityResultLauncher.launch(permissions)
        }

        setContent {
            AudioTrimmerTheme {
                val navController = rememberNavController()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                Scaffold(
                    bottomBar = {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            AnimatedContent(
                                targetState = uiState.controlsState.audioFile != null,
                                modifier = Modifier.fillMaxWidth(),
                                transitionSpec = {
                                    slideInVertically() togetherWith slideOutVertically()
                                },
                                label = String.EMPTY,
                            ) { targetState ->
                                if (targetState) {
                                    Controls(
                                        state = uiState.controlsState,
                                        onClick = {
                                            when (it) {
                                                ControlsState.Action.PAUSE -> viewModel.pause()
                                                ControlsState.Action.PLAY -> viewModel.resume()
                                                ControlsState.Action.REPLAY -> viewModel.seekTo(Float.ZERO)
                                                ControlsState.Action.STOP -> viewModel.stop()
                                            }
                                        },
                                        onValueChange = {
                                            viewModel.seekTo(it)
                                        },
                                        onValueChangeFinished = {

                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                } else {
                                    Spacer(modifier = Modifier.fillMaxWidth())
                                }
                            }

                            NavigationBar {
                                val currentBackStackEntry by navController.currentBackStackEntryAsState()

                                Route.values().forEach { route ->
                                    NavigationBarItem(
                                        icon = {
                                            Icon(
                                                painter = painterResource(
                                                    id = when (route) {
                                                        Route.SOURCE -> R.drawable.round_source_24
                                                        Route.TRIMMED -> R.drawable.round_content_cut_24
                                                    }
                                                ),
                                                contentDescription = route.name,
                                            )
                                        },
                                        label = {
                                            Text(text = route.name)
                                        },
                                        selected = currentBackStackEntry?.destination?.route  == route.name,
                                        onClick = {
                                            navController.navigate(route.name) {
                                                popUpTo(route.name)

                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                ) {
                    NavHost(
                        navController = navController,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues = it),
                        startDestination = Route.SOURCE,
                    ) {
                        composable(Route.SOURCE) {
                            Source(
                                state = uiState.sourceState,
                                expanded = uiState.expanded,
                                onItemClick = onItemClick,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }

                        composable(Route.TRIMMED) {
                            Trimmed(
                                state = uiState.trimmedState,
                                expanded = uiState.expanded,
                                onItemClick = onItemClick,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        viewModel.load()
    }

    private fun checkSelfPermissions(permissions: Array<String>): Boolean {
        return permissions.all {
            checkSelfPermission(it).granted()
        }
    }

    private val onItemClick: (AudioFile.Action) -> Unit = { action ->
        when (action) {
            is AudioFile.Action.Collapse -> viewModel.collapse()
            is AudioFile.Action.Expand -> viewModel.expand(action.audioFile)
            is AudioFile.Action.Pause -> viewModel.pause()
            is AudioFile.Action.Play -> viewModel.play(action.audioFile)
            is AudioFile.Action.Share -> shareAudioFile(action.audioFile)
            is AudioFile.Action.Trim -> {
                startActivity(
                    Intent(
                        this,
                        AudioTrimmerActivity::class.java,
                    ).apply {
                        putExtra(EXTRA_AUDIO_FILE, action.audioFile)
                    }
                )
            }
        }
    }

    private fun shouldShowRequestPermissionsRationale(permissions: Array<String>): Boolean {
        return permissions.any {
            shouldShowRequestPermissionRationale(it)
        }
    }

    private fun Int.granted() = this == PackageManager.PERMISSION_GRANTED
}
