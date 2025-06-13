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
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.PlaceholderBox
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkUi
import ru.aleshin.studyassistant.tasks.impl.presentation.theme.TasksThemeRes

/**
 * @author Stanislav Aleshin on 29.06.2024.
 */
@Composable
internal fun AllHomeworksExecutionAnalysisView(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    overdueTasks: List<HomeworkUi>,
    detachedActiveTasks: List<HomeworkUi>,
    completedHomeworksCount: Int,
    onShowErrors: () -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = TasksThemeRes.strings.allHomeworksExecutionAnalysisTitle,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleSmall,
        )
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(
                modifier = Modifier.height(44.dp).fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = StudyAssistantRes.colors.accents.greenContainer,
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = StudyAssistantRes.colors.accents.onGreenContainer,
                    )
                    Text(
                        modifier = Modifier.animateContentSize().weight(1f),
                        text = TasksThemeRes.strings.homeworksCompletedTitle,
                        color = StudyAssistantRes.colors.accents.onGreenContainer,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Crossfade(
                        modifier = Modifier.animateContentSize(),
                        targetState = isLoading,
                    ) { loading ->
                        if (!loading) {
                            Text(
                                text = completedHomeworksCount.toString(),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                                style = MaterialTheme.typography.titleLarge,
                            )
                        } else {
                            PlaceholderBox(
                                modifier = Modifier.size(20.dp, 26.dp),
                                shape = MaterialTheme.shapes.small,
                                color = StudyAssistantRes.colors.accents.green,
                            )
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    modifier = Modifier.height(44.dp).weight(1f),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.errorContainer,
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(TasksThemeRes.icons.homeworkError),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                        )
                        Text(
                            modifier = Modifier.animateContentSize().weight(1f),
                            text = TasksThemeRes.strings.homeworkErrorsTitle,
                            color = MaterialTheme.colorScheme.error,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                            style = MaterialTheme.typography.labelLarge,
                        )
                        Crossfade(
                            modifier = Modifier.animateContentSize(),
                            targetState = isLoading,
                        ) { loading ->
                            if (!loading) {
                                val errors = remember(overdueTasks, detachedActiveTasks) {
                                    listOf(overdueTasks.count(), detachedActiveTasks.count())
                                }
                                Text(
                                    text = errors.sum().toString(),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 1,
                                    style = MaterialTheme.typography.titleLarge,
                                )
                            } else {
                                PlaceholderBox(
                                    modifier = Modifier.size(20.dp, 26.dp),
                                    shape = MaterialTheme.shapes.small,
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    }
                }
                Surface(
                    onClick = onShowErrors,
                    modifier = Modifier.size(32.dp, 44.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.errorContainer,
                ) {
                    Icon(
                        modifier = Modifier.wrapContentSize(Alignment.Center).size(24.dp),
                        painter = painterResource(TasksThemeRes.icons.openMenu),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}