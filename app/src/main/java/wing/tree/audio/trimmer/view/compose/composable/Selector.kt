package wing.tree.audio.trimmer.view.compose.composable

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import wing.tree.audio.trimmer.extension.ONE
import wing.tree.audio.trimmer.extension.int
import wing.tree.audio.trimmer.view.compose.state.SelectorState
import wing.tree.audio.trimmer.view.roundToNearestMultipleOf
import kotlin.math.roundToInt

@Composable
fun Selector(
    state: SelectorState,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        val coroutineScope = rememberCoroutineScope()
        val density = LocalDensity.current
        val isPlaying by state.isPlaying
        var width by state.width
        val maxWidth = this.maxWidth

        var offset by state.offset

        var panim by remember(key1 = isPlaying) {
            mutableFloatStateOf(0f)
        }

        LaunchedEffect(key1 = isPlaying) {
            if (isPlaying) {
                val initialValue = with(density) { 2.dp.toPx() }
                val targetValue = with(density) { (width - 1.dp).toPx() }

                animate(
                    initialValue = initialValue,
                    targetValue = targetValue,
                    animationSpec = tween(durationMillis = 18000, easing = LinearEasing),
                ) { value, _ ->
                    panim = value
                }
            }
        }

        Row(
            modifier = Modifier
                .offset {
                    offset.copy(
                        x = offset.x.plus(state.contentPadding.toPx().int)
                    )
                }
        ) {
            Box(
                modifier = Modifier
                    .width(width = state.handleWidth)
                    .fillMaxHeight()
                    .background(
                        color = state.color,
                        shape = RoundedCornerShape(
                            topStart = 8.dp,
                            bottomStart = 8.dp
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .size(width = width, height = 50.dp)
                    .border(width = state.barWidth, color = state.color.copy(alpha = 0.6f))
                    .pointerInput(key1 = true) {
                        detectDragGestures(
                            onDragCancel = {
                                coroutineScope.launch {
                                    val initialValue = offset.x.toFloat()
                                    val targetValue = initialValue
                                        .roundToNearestMultipleOf(15f)
                                        .toFloat()

                                    animate(
                                        initialValue = initialValue,
                                        targetValue = targetValue,
                                    ) { value, _ ->
                                        offset = IntOffset(
                                            value.roundToInt(),
                                            offset.y
                                        )
                                    }
                                }
                            },
                            onDragEnd = {
                                coroutineScope.launch {
                                    val initialValue = offset.x.toFloat()
                                    val targetValue = initialValue
                                        .roundToNearestMultipleOf(15f)
                                        .toFloat()

                                    animate(
                                        initialValue = initialValue,
                                        targetValue = targetValue,
                                    ) { value, _ ->
                                        offset = IntOffset(
                                            value.roundToInt(),
                                            offset.y
                                        )
                                    }
                                }
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()

                                if (isPlaying) return@detectDragGestures

                                val maxV = maxWidth
                                    .toPx()
                                    .toInt()
                                    .minus(
                                        width
                                            .toPx()
                                            .toInt()
                                    )

                                val x = offset.x
                                    .plus(dragAmount.x)
                                    .roundToInt()
                                    .coerceIn(
                                        minimumValue = 0,
                                        maximumValue = maxV,
                                    )

                                offset = IntOffset(
                                    x,
                                    offset.y
                                )
                            }
                        )
                    }
                    .drawWithContent {
                        drawContent()

                        drawLine(
                            color = Color.DarkGray,
                            start = Offset(panim, 0f),
                            end = Offset(panim, size.height),
                            strokeWidth = state.barWidth.toPx()
                        )
                    }
            ) {
                with(LocalDensity.current) {
                    Text(text = offset.x.toDp().toString())
                }
            }

            Box(
                modifier = Modifier
                    .width(width = state.handleWidth)
                    .fillMaxHeight()
                    .background(
                        color = state.color,
                        shape = RoundedCornerShape(
                            topEnd = 8.dp,
                            bottomEnd = 8.dp
                        )
                    ).pointerInput(key1 = true) {
                        detectDragGestures(
                            onDragCancel = {
                                // 동일한 정확도의 width 조정 필요.
                                coroutineScope.launch {
                                    val initialValue = offset.x.toFloat()
                                    val targetValue = initialValue
                                        .roundToNearestMultipleOf(15f)
                                        .toFloat()

                                    animate(
                                        initialValue = initialValue,
                                        targetValue = targetValue,
                                    ) { value, _ ->
                                        offset = IntOffset(
                                            value.roundToInt(),
                                            offset.y
                                        )
                                    }
                                }
                            },
                            onDragEnd = {
                                coroutineScope.launch {
                                    val initialValue = offset.x.toFloat()
                                    val targetValue = initialValue
                                        .roundToNearestMultipleOf(15f)
                                        .toFloat()

                                    animate(
                                        initialValue = initialValue,
                                        targetValue = targetValue,
                                    ) { value, _ ->
                                        offset = IntOffset(
                                            value.roundToInt(),
                                            offset.y
                                        )
                                    }
                                }
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()

                                if (isPlaying) return@detectDragGestures

                                val maxV = maxWidth
                                    .toPx()
                                    .toInt()
                                    .minus(
                                        width
                                            .toPx()
                                            .toInt()
                                    )

                                val x = offset.x
                                    .plus(dragAmount.x)
                                    .roundToInt()
//                                    .coerceIn(
//                                        minimumValue = 0,
//                                        maximumValue = maxV,
//                                    )

                                width += x.toDp()
                            }
                        )
                    }
            )
        }


    }
}
