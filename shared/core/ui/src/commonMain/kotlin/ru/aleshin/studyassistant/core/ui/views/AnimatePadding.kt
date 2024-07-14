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

import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp

/**
 * @author Stanislav Aleshin on 13.07.2024.
 */
@Composable
fun Modifier.animatePadding(
    targetState: Boolean,
    paddingValues: PaddingValues,
    label: String = "Default",
): Modifier {
    val direction = LocalLayoutDirection.current
    val transition = updateTransition(targetState = targetState, label = label)
    val start by transition.animateDp(label = "$label start padding") { state ->
        if (state) paddingValues.calculateStartPadding(direction) else 0.dp
    }
    val top by transition.animateDp(label = "$label top padding") { state ->
        if (state) paddingValues.calculateStartPadding(direction) else 0.dp
    }
    val end by transition.animateDp(label = "$label end padding") { state ->
        if (state) paddingValues.calculateEndPadding(direction) else 0.dp
    }
    val bottom by transition.animateDp(label = "$label bottom padding") { state ->
        if (state) paddingValues.calculateBottomPadding() else 0.dp
    }

    return padding(start, top, end, bottom)
}