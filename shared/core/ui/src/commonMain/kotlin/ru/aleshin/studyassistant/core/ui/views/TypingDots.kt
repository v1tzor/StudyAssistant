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

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * @author Stanislav Aleshin on 01.08.2025.
 */
@Composable
fun TypingDots(
    modifier: Modifier = Modifier,
    dotCount: Int = 3,
    dotSize: Dp = 8.dp,
    dotColor: Color = MaterialTheme.colorScheme.primary,
    cycleDuration: Int = 1000
) {
    val infiniteTransition = rememberInfiniteTransition(label = "Typing dots")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(cycleDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Typing progress"
    )

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        repeat(dotCount) { index ->
            val proximity = getShiftedProximity(progress, dotCount, index)
            val alpha = proximity
            val scale = 0.5f + (0.5f * proximity)

            Spacer(
                modifier = Modifier
                    .size(dotSize)
                    .graphicsLayer {
                        this.alpha = alpha
                        this.scaleX = scale
                        this.scaleY = scale
                    }
                    .background(color = dotColor, shape = CircleShape)
            )
            if (index < dotCount - 1) Spacer(modifier = Modifier.width(dotSize / 2))
        }
    }
}

private fun getShiftedProximity(progress: Float, totalCircles: Int, indexCircle: Int): Float {
    fun getDotCenterPosition(total: Int, index: Int): Float {
        return (index * 2 * total + total) / (2f * total * total)
    }
    val dotArea = 1f / totalCircles
    val center = getDotCenterPosition(totalCircles, indexCircle)
    val proximity = (1 - kotlin.math.abs(progress - center) / dotArea).coerceIn(0f, 1f)

    val startThreshold = dotArea / 2f
    val endThreshold = dotArea * (totalCircles - 1) + (dotArea / 2f)
    val isNearEnd = progress > endThreshold
    val isNearStart = progress < startThreshold

    val shifted = when {
        indexCircle == 0 && isNearEnd -> {
            1 - (1 - kotlin.math.abs(progress - getDotCenterPosition(totalCircles, totalCircles - 1)) / dotArea)
        }
        indexCircle == totalCircles - 1 && isNearStart -> {
            1 - (1 - kotlin.math.abs(progress - getDotCenterPosition(totalCircles, 0)) / dotArea)
        }
        else -> 0f
    }
    return (proximity + shifted).coerceIn(0f, 1f)
}