package com.promptmaster.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize

@Composable
fun ZoomableContent(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var boxSize by remember { mutableStateOf(IntSize.Zero) } // State to hold the size

    val state = rememberTransformableState { zoomChange, panChange, rotationChange ->
        scale = (scale * zoomChange).coerceIn(1f, 3f) // Limit zoom from 1x to 3x

        // Calculate bounds for panning using the captured boxSize
        val currentWidth = boxSize.width.toFloat()
        val currentHeight = boxSize.height.toFloat()

        val extraWidth = currentWidth * (scale - 1)
        val extraHeight = currentHeight * (scale - 1)

        val boundX = if (extraWidth > 0) extraWidth / 2f else 0f
        val boundY = if (extraHeight > 0) extraHeight / 2f else 0f

        val newOffsetX = offsetX + panChange.x * scale
        val newOffsetY = offsetY + panChange.y * scale

        offsetX = newOffsetX.coerceIn(-boundX, boundX)
        offsetY = newOffsetY.coerceIn(-boundY, boundY)
    }

    // Observe the transformation state to reset zoom/pan when gesture ends
    LaunchedEffect(state.isTransformInProgress) { // Corrected: Observe isTransformInProgress
        if (!state.isTransformInProgress) { // Corrected: Check isTransformInProgress
            // Gesture ended, reset scale and offset
            scale = 1f
            offsetX = 0f
            offsetY = 0f
        }
    }

    Box(
        modifier = modifier
            .onSizeChanged {
                boxSize = it // Update the boxSize when it changes
            }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationX = offsetX
                translationY = offsetY
            }
            .transformable(state = state)
    ) {
        content()
    }
}

