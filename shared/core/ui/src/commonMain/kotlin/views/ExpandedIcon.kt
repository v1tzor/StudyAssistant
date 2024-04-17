/*
 * Copyright 2023 Stanislav Aleshin
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
package ru.aleshin.core.ui.views

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color

/**
 * @author Stanislav Aleshin on 08.02.2024.
 */
@Composable
fun ExpandedIcon(
    modifier: Modifier = Modifier,
    isExpanded: Boolean,
    color: Color = MaterialTheme.colorScheme.onSurface,
    description: String? = null,
) {
    Box(modifier = modifier.animateContentSize()) {
        val angle: Float by animateFloatAsState(
            targetValue = if (isExpanded) 0F else 180F,
            animationSpec = tween(
                durationMillis = 500,
                easing = FastOutSlowInEasing
            ),
            label = "Expanded icon angle",
        )
        Icon(
            modifier = Modifier.rotate(angle),
            imageVector = Icons.Default.ArrowDropDown,
            contentDescription = description,
            tint = color,
        )
    }
}
