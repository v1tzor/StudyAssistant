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

package ru.aleshin.studyassistant.tasks.impl.presentation.ui.share.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import kotlinx.datetime.format.DateTimeComponents
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.core.common.extensions.formatByTimeZone
import ru.aleshin.studyassistant.core.ui.mappers.toLanguageString
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.theme.material.full
import ru.aleshin.studyassistant.core.ui.views.PlaceholderBox
import ru.aleshin.studyassistant.core.ui.views.menu.AvatarView
import ru.aleshin.studyassistant.core.ui.views.weekdayDayMonthFormat
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkTaskComponentUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.MediatedHomeworkUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.fetchAllTasks
import ru.aleshin.studyassistant.tasks.impl.presentation.models.users.AppUserUi
import ru.aleshin.studyassistant.tasks.impl.presentation.theme.TasksThemeRes

/**
 * @author Stanislav Aleshin on 24.07.2024.
 */
@Composable
@OptIn(ExperimentalLayoutApi::class)
internal fun SentSharedHomeworksView(
    modifier: Modifier = Modifier,
    homeworks: List<MediatedHomeworkUi>,
    targetDate: Instant,
    sendDate: Instant,
    currentTime: Instant,
    recipients: List<AppUserUi>,
    onCancelSend: () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = targetDate.formatByTimeZone(
                        format = DateTimeComponents.Formats.weekdayDayMonthFormat(
                            StudyAssistantRes.strings
                        ),
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleSmall,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = buildString {
                        append((currentTime - sendDate).toLanguageString())
                        append(" ", TasksThemeRes.strings.agoSuffix)
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                homeworks.forEach { homework ->
                    SentHomeworkTasksView(
                        subjectName = homework.subjectName,
                        theoreticalTasks = homework.theoreticalTasks.components,
                        practicalTasks = homework.practicalTasks.components,
                        presentationTasks = homework.presentationTasks.components,
                    )
                }
            }
            Row(
                modifier = Modifier.padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                recipients.forEach { recipient ->
                    AvatarView(
                        modifier = Modifier.size(28.dp),
                        imageUrl = recipient.avatar,
                        firstName = recipient.username.split(' ').getOrNull(0) ?: "*",
                        secondName = recipient.username.split(' ').getOrNull(1),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    if (recipients.size == 1) {
                        Text(
                            modifier = Modifier.padding(start = 4.dp),
                            text = recipient.username,
                            color = MaterialTheme.colorScheme.onSurface,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
            FilledTonalButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onCancelSend,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text(text = TasksThemeRes.strings.cancelSentHomeworkTitle, maxLines = 1)
            }
        }
    }
}

@Composable
internal fun SentSharedHomeworksPlaceholder(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PlaceholderBox(
                modifier = Modifier.size(130.dp, 20.dp),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceContainer,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PlaceholderBox(
                    modifier = Modifier.size(125.dp, 54.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                )
                PlaceholderBox(
                    modifier = Modifier.size(125.dp, 54.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                )
                PlaceholderBox(
                    modifier = Modifier.size(125.dp, 54.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PlaceholderBox(
                    modifier = Modifier.size(125.dp, 54.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                )
                PlaceholderBox(
                    modifier = Modifier.size(125.dp, 54.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                )
                PlaceholderBox(
                    modifier = Modifier.size(125.dp, 54.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PlaceholderBox(
                    modifier = Modifier.size(24.dp),
                    shape = MaterialTheme.shapes.full,
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                )
                PlaceholderBox(
                    modifier = Modifier.size(24.dp),
                    shape = MaterialTheme.shapes.full,
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                )
                PlaceholderBox(
                    modifier = Modifier.size(24.dp),
                    shape = MaterialTheme.shapes.full,
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                )
            }
            PlaceholderBox(
                modifier = Modifier.fillMaxWidth().size(40.dp),
                shape = MaterialTheme.shapes.full,
                color = MaterialTheme.colorScheme.secondaryContainer,
            )
        }
    }
}

@Composable
internal fun NoneSentSharedHomeworksView(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.padding(top = 4.dp).fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Box(modifier = Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(StudyAssistantRes.icons.practicalTasks),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = TasksThemeRes.strings.noneSentHomeworksTitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
private fun SentHomeworkTasksView(
    modifier: Modifier = Modifier,
    subjectName: String?,
    theoreticalTasks: List<HomeworkTaskComponentUi>,
    practicalTasks: List<HomeworkTaskComponentUi>,
    presentationTasks: List<HomeworkTaskComponentUi>,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = subjectName ?: StudyAssistantRes.strings.noneTitle,
                color = MaterialTheme.colorScheme.onSurface,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.titleMedium,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                HomeworkTasksCountView(
                    painter = painterResource(StudyAssistantRes.icons.theoreticalTasks),
                    count = theoreticalTasks.fetchAllTasks().size,
                )
                HomeworkTasksCountView(
                    painter = painterResource(StudyAssistantRes.icons.practicalTasks),
                    count = practicalTasks.fetchAllTasks().size,
                )
                HomeworkTasksCountView(
                    painter = painterResource(StudyAssistantRes.icons.presentationTasks),
                    count = presentationTasks.fetchAllTasks().size,
                )
            }
        }
    }
}

@Composable
private fun HomeworkTasksCountView(
    modifier: Modifier = Modifier,
    painter: Painter,
    count: Int,
    description: String? = null,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier.size(18.dp),
            painter = painter,
            contentDescription = description,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = count.toString(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}