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

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * @author Stanislav Aleshin on 27.08.2024.
 */
@Composable
fun FreeOrPaidContent(
    isPaidUser: Boolean?,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier,
    animationSpec: FiniteAnimationSpec<Float> = spring(
        stiffness = Spring.StiffnessMediumLow,
        visibilityThreshold = Spring.DefaultDisplacementThreshold,
    ),
    placeholders: @Composable (() -> Unit)? = null,
    paidContent: @Composable (() -> Unit)? = null,
    freeContent: @Composable () -> Unit,
) {
    Crossfade(
        targetState = if (isLoading) null else isPaidUser,
        modifier = modifier,
        animationSpec = animationSpec,
        label = "FreeOrPaidContent",
    ) { paidStatus ->
        when (paidStatus) {
            true -> paidContent?.invoke() ?: freeContent()
            false -> freeContent.invoke()
            null -> placeholders?.invoke()
        }
    }
}