package wing.tree.audio.trimmer.view

import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import wing.tree.audio.trimmer.view.compose.composable.Selector
import wing.tree.audio.trimmer.view.compose.state.SelectorState
import wing.tree.audio.trimmer.viewmodel.AudioTrimmerViewModel
import wing.tree.audio.trimmer.viewmodel.factory.AudioTrimmerViewModelFactory
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

class AudioTrimmerActivity : ComponentActivity() {
    private val selectedPlayer = MediaPlayer()
    private val trimmedPlayer = MediaPlayer()

    private val viewModel by viewModels<AudioTrimmerViewModel> {
        AudioTrimmerViewModelFactory(
            application = application,
            intent = intent
        )
    }

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val handleWidth = 16.dp

        setContent {
            val coroutineScope = rememberCoroutineScope()
            val state = viewModel.ace.collectAsStateWithLifecycle()
            val duration = viewModel.ace2.collectAsStateWithLifecycle()
            val lazyListState = rememberLazyListState()

            var firstVisibleListItemScrollOffset by remember {
                mutableIntStateOf(0)
            }

            var secondVisibleListItemScrollOffset by remember {
                mutableIntStateOf(0)
            }

            var listScrollOffset by remember {
                mutableIntStateOf(0)
            }

            var openDialog by remember {
                mutableStateOf(false)
            }

            val trimmedFile = viewModel.trimmedFile.collectAsStateWithLifecycle()

            val selectorState = remember {
                SelectorState(
                    handleWidth = handleWidth
                )
            }

            val trimAce = viewModel.trimAce.collectAsStateWithLifecycle()

            with(lazyListState) {
                LaunchedEffect(this) {
                    snapshotFlow {
                        layoutInfo
                    }.collect {
                        it.visibleItemsInfo.getOrNull(1)?.offset ?: 0
                        val firstVisibleItemIndex = firstVisibleItemIndex
                        val firstVisibleItemScrollOffset = firstVisibleItemScrollOffset
                        val a = it.mainAxisItemSpacing
                        val size = (it.visibleItemsInfo.firstOrNull()?.size?.plus(a) ?: 0)

                        val accumulatedScrollOffsetValue = if (firstVisibleItemIndex == 0) {
                            firstVisibleItemScrollOffset
                        } else {
                            firstVisibleItemScrollOffset + firstVisibleItemIndex.times(size)
                        }

                        firstVisibleListItemScrollOffset = firstVisibleItemScrollOffset
                        secondVisibleListItemScrollOffset = it.visibleItemsInfo.getOrNull(1)?.offset ?: 0
                        listScrollOffset = accumulatedScrollOffsetValue
                    }
                }
            }

            Column(
                Modifier
                    .fillMaxSize()
                    .background(Color.Yellow)) {
                Box(
                    modifier = Modifier
                        .height(50.dp)
                        .nestedScroll(object : NestedScrollConnection {
                            override suspend fun onPostFling(
                                consumed: Velocity,
                                available: Velocity
                            ): Velocity {
                                val value = firstVisibleListItemScrollOffset
                                    .toFloat()
                                    .unaryMinus()

                                val value2 = secondVisibleListItemScrollOffset.toFloat()

                                val value3 = if (value.absoluteValue > value2.absoluteValue) {
                                    value2
                                } else {
                                    value
                                }

                                lazyListState.animateScrollBy(value3)

                                return super.onPostFling(consumed, available)
                            }
                        })
                ) {
                    CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
                        LazyRow(
                            modifier = Modifier
                                .height(50.dp)
                                .background(Color.Cyan),
                            state = lazyListState,
                            contentPadding = PaddingValues(horizontal = handleWidth, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {

                            items(state.value) {
                                Spacer(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .heightIn(min = 2.dp)
                                        .height(it.dp * 3)
                                        .background(Color.White),
                                )
                            }
                        }
                    }

                    Selector(
                        state = selectorState,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                LazyRow(
                    modifier = Modifier
                        .height(50.dp)
                        .background(Color.Cyan),
                    // state = lazyListState,
                    contentPadding = PaddingValues(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {

                    items(trimAce.value) {
                        Spacer(
                            modifier = Modifier
                                .width(3.dp)
                                .heightIn(min = 2.dp)
                                .height(it.dp * 3)
                                .background(Color.White),
                        )
                    }
                }

                val dpOff = with(LocalDensity.current) {
                    selectorState.offset.value.x.toDp()
                }
                val dpOffList = with(LocalDensity.current) {
                    listScrollOffset.toDp()
                }

                val dpOffListDiv = with(LocalDensity.current) {
                    listScrollOffset.toDp().div(5.dp).dp
                }

                val ad = dpOff.div(5.dp).dp

                Text(text = "borte0-density:${LocalDensity.current.density}")
                Text(text = "borte1-1:offsetPx:${selectorState.offset.value.x}")
                Text(text = "borte1-2:offsetDp:${dpOff}")
                Text(text = "borte1-3:offsetDp/divStickWidth:${ad}\n")
                Text(text = "borte2:listOffset:${listScrollOffset}")
                Text(text = "borte2-2:listOffset:${dpOffList}")
                Text(text = "borte2-2:listOffsetDiv:${dpOffListDiv}\n")
                Text(text = "borte3:sum Offset:${listScrollOffset + selectorState.offset.value.x}")
                Text(text = "borte3:sum OffsetDp:${dpOff.div(5.dp).dp + dpOffList}")
                Text(text = "borte3:sum OffsetDpDivAll:${dpOff.div(5.dp).dp + dpOffListDiv}")
                Text(text = state.value.count().toString())
                Text(text = duration.value.toString())


                Button(onClick = {
                    openDialog = true
                }) {
                    Text(text = "trim")
                }

                Button(onClick = {
                    with(selectedPlayer) {
                        if (isPlaying) {
                            stop()
                            reset()
                        } else {
                            val uri = viewModel.audioFile?.uri ?: return@with
                            val start = (dpOff.div(5.dp).dp + dpOffListDiv).value
                            val end = start + selectorState.width.value.div(5.dp)
                            val duration = end.minus(start).toInt()

                            println("ssssss:$start,,$end,,$duration")

                            setDataSource(this@AudioTrimmerActivity, uri)
                            prepare()
                            seekTo(start.toInt() * 1000)
                            start()
                            selectorState.play()

                            coroutineScope.launch {
                                delay(duration * 1000L)
                                selectedPlayer.stop()
                                selectedPlayer.reset()
                                selectorState.stop()
                            }
                        }
                    }
                }) {
                    Text("Play Selected")
                }

                Button(onClick = {
                    with(trimmedPlayer) {
                        if (isPlaying) {
                            stop()
                            reset()
                        } else {
                            val f = trimmedFile.value?.path ?: return@with
                            setDataSource(this@AudioTrimmerActivity, f.toUri())
                            prepare()
                            start()
                        }
                    }
                }) {
                    Text("Play Trimmed.")
                }
            }

            if (openDialog) {
                Dialog(
                    onDismissRequest = { openDialog = false }
                ) {
                    var filename by remember {
                        mutableStateOf("")
                    }

                    Surface {
                        Column {
                            OutlinedTextField(
                                value = filename,
                                onValueChange = {
                                    filename = it
                                }
                            )

                            val dpOff = with(LocalDensity.current) {
                                selectorState.offset.value.x.toDp()
                            }

                            val dpOffListDiv = with(LocalDensity.current) {
                                listScrollOffset.toDp().div(5.dp).dp
                            }

                            TextButton(
                                onClick = {
                                    val start = (dpOff.div(5.dp).dp + dpOffListDiv).value
                                    val end = start + selectorState.width.value.div(5.dp)

                                    viewModel.trim(
                                        filename = filename,
                                        start = start,
                                        end = end
                                    )

                                    openDialog = false
                                }
                            ) {
                                Text(text = "trim")
                            }
                        }
                    }
                }
            }
        }
    }
}

fun Float.roundToNearestMultipleOf(multiplier: Float): Int {
    return ((this / multiplier).roundToInt() * multiplier).roundToInt()
}
