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
package ru.aleshin.studyassistant.core.common.extensions

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import ru.aleshin.studyassistant.core.common.functional.Constants

/**
 * @author Stanislav Aleshin on 12.06.2023.
 */
const val DISABLED_ALPHA = 0.6F

fun LazyGridScope.emptyItem(modifier: Modifier = Modifier) {
    item { Spacer(modifier = modifier.fillMaxWidth()) }
}

@Composable
fun Modifier.alphaByEnabled(enabled: Boolean, disabledAlpha: Float = DISABLED_ALPHA): Modifier {
    val value by animateFloatAsState(
        targetValue = if (enabled) 1f else disabledAlpha,
        animationSpec = tween(),
    )
    return alpha(alpha = value)
}

@ExperimentalFoundationApi
fun PagerState.pageProgress(): Float {
    return currentPage.toFloat() / pageCount
}

@Composable
fun WindowInsets.Companion.safeNavigationBarsInPx(density: Density): Int {
    return this.navigationBars.getBottom(density) + Constants.Window.SAFE_AREA_PX
}

@Composable
fun WindowInsets.Companion.navigationBarsInDp(): Dp {
    return this.navigationBars.asPaddingValues().calculateBottomPadding()
}

@Stable
fun floatSpring(
    dampingRatio: Float = Spring.DampingRatioNoBouncy,
    stiffness: Float = Spring.StiffnessMediumLow,
    visibilityThreshold: Float? = 0.1f,
): SpringSpec<Float> {
    return spring(dampingRatio, stiffness, visibilityThreshold)
}

@Composable
fun TextStyle.boldWeight() = copy(
    fontWeight = FontWeight.Bold,
)

@Composable
fun TextStyle.extraBoldWeight() = copy(
    fontWeight = FontWeight.ExtraBold,
)

@Composable
fun TextStyle.BlackWeight() = copy(
    fontWeight = FontWeight.Black,
)

fun Int.pxToDp(density: Density) = with(density) {
    this@pxToDp.toDp()
}