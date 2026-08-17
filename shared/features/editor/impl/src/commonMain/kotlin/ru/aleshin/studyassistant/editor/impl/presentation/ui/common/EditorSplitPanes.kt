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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import ru.aleshin.studyassistant.core.ui.theme.tokens.AdaptiveLayoutDefaults

/**
 * @author Stanislav Aleshin on 17.08.2026.
 */
@Composable
internal fun EditorSplitPanes(
    modifier: Modifier = Modifier,
    startWeight: Float = 1f,
    endWeight: Float = 1f,
    startPane: @Composable () -> Unit,
    endPane: @Composable () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(
                horizontal = AdaptiveLayoutDefaults.ExpandedHorizontalPadding,
                vertical = AdaptiveLayoutDefaults.SpaceLarge,
            ),
        horizontalArrangement = Arrangement.spacedBy(AdaptiveLayoutDefaults.PaneSpacing),
    ) {
        Box(modifier = Modifier.weight(startWeight).fillMaxHeight()) {
            startPane()
        }
        Box(modifier = Modifier.weight(endWeight).fillMaxHeight()) {
            endPane()
        }
    }
}

@Composable
internal fun EditorConstrainedPane(
    modifier: Modifier = Modifier,
    maxWidth: Dp = AdaptiveLayoutDefaults.MediumContentMaxWidth,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopStart,
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = maxWidth)
                .fillMaxWidth()
                .padding(
                    horizontal = AdaptiveLayoutDefaults.ExpandedHorizontalPadding,
                    vertical = AdaptiveLayoutDefaults.SpaceLarge,
                ),
        ) {
            content()
        }
    }
}
