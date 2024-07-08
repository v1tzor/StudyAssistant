/*
 * Copyright 2024 Stanislav Aleshin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.aleshin.studyassistant.core.ui.views

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * @author Stanislav Aleshin on 21.06.2024.
 */
@Composable
fun VerticalLeftTimeProgress(
    modifier: Modifier = Modifier,
    leftTimeProgress: Float?,
    trackWidth: Dp = 3.dp,
    thumbHeight: Dp = 4.dp,
    verticalSpacing: Dp = 4.dp,
    passTrackColor: Color = MaterialTheme.colorScheme.primary,
    nextTrackColor: Color = MaterialTheme.colorScheme.primaryContainer,
    thumbColor: Color = MaterialTheme.colorScheme.primary,
) {
    val animatedProgress by animateFloatAsState(targetValue = leftTimeProgress ?: -1f)
    Canvas(modifier = modifier.width(16.dp)) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val trackWidthPx = trackWidth.toPx()

        if (animatedProgress != -1f) {
            val verticalSpacingPx = verticalSpacing.toPx()
            val thumbHeightPx = thumbHeight.toPx()
            val passTrackHeight = (canvasHeight - thumbHeightPx - 2 * verticalSpacingPx) * animatedProgress

            drawLine(
                color = passTrackColor,
                start = Offset(x = canvasWidth / 2f, y = 0f),
                end = Offset(x = canvasWidth / 2f, y = passTrackHeight),
                strokeWidth = trackWidthPx,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = thumbColor,
                start = Offset(x = 0f, y = passTrackHeight + verticalSpacingPx),
                end = Offset(x = canvasWidth, y = passTrackHeight + verticalSpacingPx),
                strokeWidth = thumbHeightPx,
                cap = StrokeCap.Round
            )
            drawLine(
                color = nextTrackColor,
                start = Offset(x = canvasWidth / 2f, y = passTrackHeight + 2 * verticalSpacingPx + thumbHeightPx),
                end = Offset(x = canvasWidth / 2f, y = canvasHeight),
                strokeWidth = trackWidthPx,
                cap = StrokeCap.Round
            )
        } else {
            drawLine(
                color = nextTrackColor,
                start = Offset(x = canvasWidth / 2f, y = 0f),
                end = Offset(x = canvasWidth / 2f, y = canvasHeight),
                strokeWidth = trackWidthPx,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun HorizontalLeftTimeProgress(
    modifier: Modifier = Modifier,
    leftTimeProgress: Float?,
    trackHeight: Dp = 3.dp,
    thumbWidth: Dp = 4.dp,
    horizontalSpacing: Dp = 4.dp,
    passTrackColor: Color = MaterialTheme.colorScheme.primary,
    nextTrackColor: Color = MaterialTheme.colorScheme.primaryContainer,
    thumbColor: Color = MaterialTheme.colorScheme.primary,
) {
    val animatedProgress by animateFloatAsState(targetValue = leftTimeProgress ?: -1f)
    Canvas(modifier = modifier.height(16.dp)) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val trackHeightPx = trackHeight.toPx()

        if (animatedProgress != -1f) {
            val horizontalSpacingPx = horizontalSpacing.toPx()
            val thumbWidthPx = thumbWidth.toPx()
            val passTrackWidth = (canvasWidth - thumbWidthPx - 2 * horizontalSpacingPx) * animatedProgress

            drawLine(
                color = passTrackColor,
                start = Offset(x = 0f, y = canvasHeight / 2f),
                end = Offset(x = passTrackWidth, y = canvasHeight / 2f),
                strokeWidth = trackHeightPx,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = thumbColor,
                start = Offset(x = passTrackWidth + horizontalSpacingPx, y = 0f),
                end = Offset(x = passTrackWidth + horizontalSpacingPx, y = canvasHeight),
                strokeWidth = thumbWidthPx,
                cap = StrokeCap.Round
            )
            drawLine(
                color = nextTrackColor,
                start = Offset(x = passTrackWidth + 2 * horizontalSpacingPx + thumbWidthPx, y = canvasHeight / 2f),
                end = Offset(x = canvasWidth, y = canvasHeight / 2f),
                strokeWidth = trackHeightPx,
                cap = StrokeCap.Round
            )
        } else {
            drawLine(
                color = nextTrackColor,
                start = Offset(x = 0f, y = canvasHeight / 2f),
                end = Offset(x = canvasWidth, y = canvasHeight / 2f),
                strokeWidth = trackHeightPx,
                cap = StrokeCap.Round
            )
        }
    }
}