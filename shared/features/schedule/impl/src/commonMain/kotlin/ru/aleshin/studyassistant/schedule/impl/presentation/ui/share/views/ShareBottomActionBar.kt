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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.views

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.schedule.impl.presentation.theme.ScheduleThemeRes

/**
 * @author Stanislav Aleshin on 16.08.2024.
 */
@Composable
internal fun ShareBottomActionBar(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoadingAccept: Boolean,
    onAcceptSharedSchedule: () -> Unit,
    onRejectSharedSchedule: () -> Unit,
) {
    Row(
        modifier = modifier.navigationBarsPadding().padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(
            onClick = onAcceptSharedSchedule,
            modifier = Modifier.weight(1f),
            enabled = enabled,
        ) {
            Crossfade(
                targetState = isLoadingAccept,
                animationSpec = spring(
                    stiffness = Spring.StiffnessMediumLow,
                    visibilityThreshold = Spring.DefaultDisplacementThreshold,
                )
            ) { loading ->
                if (!loading) {
                    Text(
                        text = ScheduleThemeRes.strings.acceptSharedScheduleButtonTitle,
                        maxLines = 1
                    )
                } else {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 3.dp,
                    )
                }
            }
        }
        FilledTonalButton(
            onClick = onRejectSharedSchedule,
            enabled = enabled,
        ) {
            Text(text = ScheduleThemeRes.strings.rejectSharedScheduleButtonTitle, maxLines = 1)
        }
    }
}