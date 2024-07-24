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

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import kotlinx.datetime.format.DateTimeComponents
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.core.common.extensions.formatByTimeZone
import ru.aleshin.studyassistant.core.common.functional.Constants.Placeholder.SHARED_HOMEWORK_SUBJECTS
import ru.aleshin.studyassistant.core.ui.mappers.toLanguageString
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.theme.material.full
import ru.aleshin.studyassistant.core.ui.views.PlaceholderBox
import ru.aleshin.studyassistant.core.ui.views.menu.AvatarView
import ru.aleshin.studyassistant.core.ui.views.weekdayDayMonthFormat
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.MediatedHomeworkUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.users.AppUserUi
import ru.aleshin.studyassistant.tasks.impl.presentation.theme.TasksThemeRes
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.common.HomeworkTaskTestView
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.common.HomeworkTaskView

/**
 * @author Stanislav Aleshin on 20.07.2024.
 */
@Composable
internal fun ReceivedSharedHomeworksView(
    modifier: Modifier = Modifier,
    homeworks: List<MediatedHomeworkUi>,
    targetDate: Instant,
    sendDate: Instant,
    currentTime: Instant,
    sender: AppUserUi,
    onOpenProfile: () -> Unit,
    onAccept: () -> Unit,
    onReject: () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column {
            val subjectNames = homeworks.map { it.subjectName }
            var selectedSubject by remember { mutableStateOf(homeworks[0].subjectName) }
            val selectedHomework = homeworks.find { it.subjectName == selectedSubject }

            ReceivedSharedHomeworksViewHeader(
                subjectNames = subjectNames,
                targetDate = targetDate,
                sendDate = sendDate,
                currentTime = currentTime,
                sender = sender,
                selectedSubject = selectedSubject,
                onOpenProfile = onOpenProfile,
                onSelectSubject = { selectedSubject = it },
            )
            HorizontalDivider()
            ReceivedSharedHomeworksViewContent(
                mediatedHomework = selectedHomework,
            )
            ReceivedSharedHomeworksViewFooter(
                onAccept = onAccept,
                onReject = onReject,
            )
        }
    }
}

@Composable
internal fun ReceivedSharedHomeworksPlaceholder(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column {
            Column(
                modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PlaceholderBox(
                        modifier = Modifier.size(130.dp, 20.dp),
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceContainer,
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PlaceholderBox(
                            modifier = Modifier.size(32.dp),
                            shape = MaterialTheme.shapes.full,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        )
                        PlaceholderBox(
                            modifier = Modifier.size(150.dp, 24.dp),
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        )
                    }
                }
                Row {
                    repeat(SHARED_HOMEWORK_SUBJECTS) {
                        PlaceholderBox(
                            modifier = Modifier.weight(1f).height(28.dp),
                            shape = MaterialTheme.shapes.full,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                        )
                    }
                }
            }
            HorizontalDivider()
            Column(
                modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row {
                    PlaceholderBox(
                        modifier = Modifier.fillMaxWidth().height(32.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceContainer,
                    )
                    PlaceholderBox(
                        modifier = Modifier.fillMaxWidth().height(32.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceContainer,
                    )
                    PlaceholderBox(
                        modifier = Modifier.fillMaxWidth().height(32.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceContainer,
                    )
                }
            }
            Row(
                modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PlaceholderBox(
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = MaterialTheme.shapes.full,
                    color = MaterialTheme.colorScheme.primaryContainer,
                )
                PlaceholderBox(
                    modifier = Modifier.size(120.dp, 40.dp),
                    shape = MaterialTheme.shapes.full,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                )
            }
        }
    }
}

@Composable
internal fun NoneReceivedSharedHomeworksView(
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
                    text = TasksThemeRes.strings.noneReceivedHomeworksTitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
private fun ReceivedSharedHomeworksViewHeader(
    modifier: Modifier = Modifier,
    subjectNames: List<String>,
    targetDate: Instant,
    sendDate: Instant,
    currentTime: Instant,
    sender: AppUserUi,
    selectedSubject: String,
    onSelectSubject: (String) -> Unit,
    onOpenProfile: () -> Unit,
) {
    Column(
        modifier = modifier.padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
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
            Surface(
                onClick = onOpenProfile,
                shape = MaterialTheme.shapes.full,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
            ) {
                Row(
                    modifier = Modifier.padding(end = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AvatarView(
                        modifier = Modifier.size(32.dp),
                        firstName = sender.username.split(' ').getOrNull(0) ?: "*",
                        secondName = sender.username.split(' ').getOrNull(1),
                        imageUrl = sender.avatar,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = sender.username,
                        color = MaterialTheme.colorScheme.onSurface,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        }
        val subjectsRowState = rememberLazyListState()
        LazyRow(
            modifier = modifier.fillMaxWidth().height(28.dp),
            state = subjectsRowState,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items(subjectNames) { subject ->
                SubjectNameItem(
                    onClick = { onSelectSubject(subject) },
                    selected = subject == selectedSubject,
                    name = subject,
                )
            }
        }
        LaunchedEffect(selectedSubject) {
            val index = subjectNames.indexOf(selectedSubject)
            if (index != -1) subjectsRowState.animateScrollToItem(index)
        }
    }
}

@Composable
private fun SubjectNameItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selected: Boolean,
    name: String,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = 28.dp),
        enabled = enabled,
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        shape = MaterialTheme.shapes.full,
    ) {
        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
            Text(
                text = name,
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                maxLines = 1,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun ReceivedSharedHomeworksViewContent(
    modifier: Modifier = Modifier,
    mediatedHomework: MediatedHomeworkUi?,
) {
    Column(
        modifier = modifier
            .animateContentSize()
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (mediatedHomework?.test != null) {
            HomeworkTaskTestView(topic = mediatedHomework.test)
        }
        if (mediatedHomework?.theoreticalTasks?.origin?.isNotBlank() == true) {
            HomeworkTaskView(
                icon = painterResource(StudyAssistantRes.icons.theoreticalTasks),
                title = StudyAssistantRes.strings.theoreticalTasksTitle,
                tasks = mediatedHomework.theoreticalTasks.components,
            )
        }
        if (mediatedHomework?.practicalTasks?.origin?.isNotBlank() == true) {
            HomeworkTaskView(
                icon = painterResource(StudyAssistantRes.icons.practicalTasks),
                title = StudyAssistantRes.strings.practicalTasksTitle,
                tasks = mediatedHomework.practicalTasks.components,
            )
        }
        if (mediatedHomework?.presentationTasks?.origin?.isNotBlank() == true) {
            HomeworkTaskView(
                icon = painterResource(StudyAssistantRes.icons.presentationTasks),
                title = StudyAssistantRes.strings.presentationsTasksTitle,
                tasks = mediatedHomework.presentationTasks.components,
            )
        }
    }
}

@Composable
private fun ReceivedSharedHomeworksViewFooter(
    modifier: Modifier = Modifier,
    onAccept: () -> Unit,
    onReject: () -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(onClick = onAccept, modifier = Modifier.weight(1f)) {
            Text(text = TasksThemeRes.strings.acceptHomeworkTitle)
        }
        FilledTonalButton(onClick = onReject) {
            Text(text = TasksThemeRes.strings.rejectHomeworkTitle, maxLines = 1)
        }
    }
}