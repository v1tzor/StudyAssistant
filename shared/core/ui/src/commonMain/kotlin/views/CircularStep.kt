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

package views

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import theme.material.full

/**
 * @author Stanislav Aleshin on 16.04.2024.
 */
@Composable
@ExperimentalFoundationApi
fun CircularStepsRow(
    modifier: Modifier = Modifier,
    countSteps: Int,
    currentStep: Int,
    rowState: LazyListState = rememberLazyListState(),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(8.dp),
    stepSize: CircularStepSize = CircularStepSize.Medium,
    stepActiveColor: Color = MaterialTheme.colorScheme.primary,
    stepInactiveColor: Color = MaterialTheme.colorScheme.primaryContainer,
) {
    LazyRow(
        modifier = modifier,
        state = rowState,
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items(countSteps) { index ->
            CircularStepView(
                modifier = Modifier,
                active = index == currentStep,
                size = stepSize,
                activeColor = stepActiveColor,
                inactiveColor = stepInactiveColor,
            )
        }
    }
}

@Composable
fun CircularStepView(
    modifier: Modifier = Modifier,
    active: Boolean,
    size: CircularStepSize = CircularStepSize.Medium,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.primaryContainer,
) {
    val color by animateColorAsState(if (active) activeColor else inactiveColor)
    Surface(
        modifier = modifier.animateContentSize().size(
            width = if (!active) size.width else size.width * 2,
            height = size.height
        ),
        shape = MaterialTheme.shapes.full(),
        color = color,
        content = { Box(modifier = Modifier.fillMaxSize()) }
    )
}

enum class CircularStepSize(val width: Dp, val height: Dp) {
    Small(7.dp, 7.dp),
    Medium(10.dp, 10.dp),
    Large(15.dp, 15.dp),
}