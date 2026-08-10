/*
 * Copyright 2026 Stanislav Aleshin
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

package ru.aleshin.studyassistant.core.ui.views.adaptive

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.toSize
import androidx.window.core.layout.WindowSizeClass

/**
 * @author Stanislav Aleshin on 10.09.2025.
 */
@Composable
fun AdaptiveContent(
    modifier: Modifier = Modifier,
    animationSpec: FiniteAnimationSpec<Float> = tween(),
    label: String = "AdaptiveContent",
    mediumContent: (@Composable () -> Unit)? = null,
    expandedContent: (@Composable () -> Unit)? = null,
    defaultContent: @Composable () -> Unit,
) {
    val windowSizeClass = currentWindowAdaptiveInfoV2().windowSizeClass

    Crossfade(
        targetState = windowSizeClass,
        modifier = modifier,
        animationSpec = animationSpec,
        label = label,
    ) { sizeClass ->
        when {
            sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) -> {
                expandedContent?.invoke() ?: mediumContent?.invoke() ?: defaultContent()
            }

            sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) -> {
                mediumContent?.invoke() ?: defaultContent()
            }

            else -> defaultContent()
        }
    }
}

@Composable
fun <R, T> R.adaptiveValue(
    mediumValue: (R.() -> T)? = null,
    expandedValue: (R.() -> T)? = null,
    defaultValue: R.() -> T,
): T {
    val windowSizeClass = currentWindowAdaptiveInfoV2().windowSizeClass

    return when {
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) -> {
            expandedValue?.invoke(this) ?: mediumValue?.invoke(this) ?: defaultValue()
        }

        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) -> {
            mediumValue?.invoke(this) ?: defaultValue()
        }

        else -> defaultValue()
    }
}

@Composable
fun currentScreenSize(): DpSize {
    val density = LocalDensity.current
    val windowInfo = LocalWindowInfo.current
    return with(density) { windowInfo.containerSize.toSize().toDpSize() }
}
