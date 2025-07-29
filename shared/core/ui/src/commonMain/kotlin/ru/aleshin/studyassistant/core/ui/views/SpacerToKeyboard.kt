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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.core.common.extensions.pxToDp
import ru.aleshin.studyassistant.core.ui.theme.tokens.LocalWindowSize

/**
 * @author Stanislav Aleshin on 17.07.2025.
 */
@Composable
fun SpacerToKeyboard(
    modifier: Modifier = Modifier,
    additionOffset: Dp = 12.dp
) {
    val density = LocalDensity.current
    val windowSize = LocalWindowSize.current

    val imeHeight = WindowInsets.ime.getBottom(density).pxToDp(density)
    val bottomBarHeight = WindowInsets.navigationBars.getBottom(density).pxToDp(density)

    var bottomOffsetDp by remember { mutableStateOf(0.dp) }

    val bottomPadding = remember(imeHeight, bottomOffsetDp, bottomBarHeight) {
        (imeHeight - bottomOffsetDp - bottomBarHeight - additionOffset).coerceAtLeast(0.dp)
    }

    Box(
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                val position = coordinates.positionInWindow()
                val bottomPx = with(density) { (position.y + coordinates.size.height).toDp() }

                bottomOffsetDp = windowSize.heightWindowDpSize - bottomPx
            }
            .padding(
                bottom = bottomPadding
            )
    )
}