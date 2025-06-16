package com.promptmaster.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.positionChange

@Composable
fun ZoomableContent(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                forEachGesture {
                    awaitPointerEventScope {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        var transformStarted = false
                        do {
                            val event = awaitPointerEvent()

                            // Calculate zoom and pan manually from pointer changes
                            val zoomChange = event.changes.fold(1f) { acc, change ->
                                val previousDistance = (change.previousPosition - event.changes.first().previousPosition).getDistance()
                                val currentDistance = (change.position - event.changes.first().position).getDistance()
                                if (previousDistance == 0f) acc else acc * (currentDistance / previousDistance)
                            }

                            val panChange = event.changes.fold(Offset.Zero) { acc, change ->
                                acc + (change.position - change.previousPosition)
                            }

                            if (zoomChange != 1f || panChange != Offset.Zero) {
                                transformStarted = true
                                scale = (scale * zoomChange).coerceIn(1f, 3f) // Limit zoom from 1x to 3x

                                val extraWidth = size.width * (scale - 1)
                                val extraHeight = size.height * (scale - 1)

                                val boundX = if (extraWidth > 0) extraWidth / 2f else 0f
                                val boundY = if (extraHeight > 0) extraHeight / 2f else 0f

                                val newOffsetX = offsetX + panChange.x * scale
                                val newOffsetY = offsetY + panChange.y * scale

                                offsetX = newOffsetX.coerceIn(-boundX, boundX)
                                offsetY = newOffsetY.coerceIn(-boundY, boundY)
                            }

                            // Consume changes to prevent them from propagating to children
                            event.changes.forEach { it.consume() }

                        } while (event.changes.any { it.pressed })

                        // Gesture ended (all fingers up or cancelled)
                        if (transformStarted) {
                            scale = 1f
                            offsetX = 0f
                            offsetY = 0f
                        }
                    }
                }
            }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationX = offsetX
                translationY = offsetY
            }
    ) {
        content()
    }
}
