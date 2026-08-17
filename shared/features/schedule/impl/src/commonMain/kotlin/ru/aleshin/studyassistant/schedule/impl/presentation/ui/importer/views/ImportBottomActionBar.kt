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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.views

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.ui.theme.tokens.AdaptiveLayoutDefaults
import ru.aleshin.studyassistant.schedule.impl.resources.Res
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_apply_button
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_edit_source_button

/**
 * @author Stanislav Aleshin on 16.08.2026.
 */
@Composable
internal fun ImportBottomActionBar(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoadingAccept: Boolean,
    contentMaxWidth: Dp? = null,
    horizontalPadding: Dp = AdaptiveLayoutDefaults.CompactHorizontalPadding,
    onSaveClick: () -> Unit,
    onEditSourceClick: () -> Unit,
) {
    val barModifier = if (contentMaxWidth != null) {
        Modifier
            .widthIn(max = contentMaxWidth)
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = horizontalPadding, vertical = AdaptiveLayoutDefaults.SpaceLarge)
    } else {
        Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = horizontalPadding, vertical = AdaptiveLayoutDefaults.SpaceLarge)
    }
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopStart,
    ) {
    Row(
        modifier = barModifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(
            onClick = onSaveClick,
            modifier = Modifier.weight(1f),
            enabled = enabled,
        ) {
            Crossfade(
                targetState = isLoadingAccept,
                animationSpec = spring(
                    stiffness = Spring.StiffnessMediumLow,
                    visibilityThreshold = Spring.DefaultDisplacementThreshold,
                ),
            ) { loading ->
                if (!loading) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PlayCircle,
                            contentDescription = null,
                        )
                        Text(
                            text = stringResource(Res.string.schedule_import_apply_button),
                            maxLines = 1,
                        )
                    }
                } else {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 3.dp,
                    )
                }
            }
        }
        FilledTonalButton(
            onClick = onEditSourceClick,
            enabled = enabled,
        ) {
            Text(
                text = stringResource(Res.string.schedule_import_edit_source_button),
                maxLines = 1,
            )
        }
    }
    }
}
