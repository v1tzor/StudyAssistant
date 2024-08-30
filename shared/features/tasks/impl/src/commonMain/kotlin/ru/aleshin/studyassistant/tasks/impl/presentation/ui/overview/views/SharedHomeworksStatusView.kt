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

package ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.views

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.MediumInfoBadge
import ru.aleshin.studyassistant.core.ui.views.PlaceholderBox
import ru.aleshin.studyassistant.tasks.impl.presentation.models.share.SharedHomeworksUi
import ru.aleshin.studyassistant.tasks.impl.presentation.theme.TasksThemeRes

/**
 * @author Stanislav Aleshin on 24.07.2024.
 */
@Composable
internal fun SharedHomeworksStatusView(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    sharedHomeworks: SharedHomeworksUi?,
    onOpenSharedHomeworks: () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, bottom = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(TasksThemeRes.icons.sharedHomeworks),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    modifier = Modifier.weight(1f),
                    text = TasksThemeRes.strings.shareHomeworksHeader,
                    fontWeight = FontWeight.Bold,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleSmall,
                )
                Crossfade(
                    targetState = isLoading,
                    animationSpec = spring(
                        stiffness = Spring.StiffnessMediumLow,
                        visibilityThreshold = Spring.DefaultDisplacementThreshold,
                    )
                ) { loading ->
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (!loading) {
                            if (!sharedHomeworks?.sent.isNullOrEmpty()) {
                                MediumInfoBadge(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.primary,
                                ) {
                                    Text(text = (sharedHomeworks?.sent?.size ?: 0).toString(), maxLines = 1)
                                }
                            }
                            MediumInfoBadge(
                                containerColor = StudyAssistantRes.colors.accents.orangeContainer,
                                contentColor = StudyAssistantRes.colors.accents.orange,
                            ) {
                                Text(text = (sharedHomeworks?.received?.size ?: 0).toString(), maxLines = 1)
                            }
                        } else {
                            PlaceholderBox(
                                modifier = Modifier.size(20.dp),
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                            )
                            PlaceholderBox(
                                modifier = Modifier.size(20.dp),
                                shape = RoundedCornerShape(6.dp),
                                color = StudyAssistantRes.colors.accents.orangeContainer,
                            )
                        }
                    }
                }
            }
            Button(
                onClick = onOpenSharedHomeworks,
                modifier = Modifier.fillMaxWidth().height(32.dp),
                enabled = !isLoading,
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            ) {
                Text(
                    text = TasksThemeRes.strings.showSharedHomeworksTitle,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}