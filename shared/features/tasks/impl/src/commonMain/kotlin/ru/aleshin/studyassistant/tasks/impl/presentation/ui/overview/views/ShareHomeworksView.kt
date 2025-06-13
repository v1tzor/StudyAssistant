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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import kotlinx.datetime.format.DateTimeComponents.Formats
import ru.aleshin.studyassistant.core.common.extensions.floatSpring
import ru.aleshin.studyassistant.core.common.extensions.formatByTimeZone
import ru.aleshin.studyassistant.core.common.functional.Constants.Placeholder.SHARED_HOMEWORKS
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.PlaceholderBox
import ru.aleshin.studyassistant.core.ui.views.menu.AvatarView
import ru.aleshin.studyassistant.core.ui.views.shortDayMonthTimeFormat
import ru.aleshin.studyassistant.tasks.impl.presentation.models.share.ReceivedMediatedHomeworksDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.share.SentMediatedHomeworksDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.share.SharedHomeworksDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.theme.TasksThemeRes

/**
 * @author Stanislav Aleshin on 27.03.2025.
 */
@Composable
internal fun ShareHomeworksView(
    modifier: Modifier = Modifier,
    isLoadingShare: Boolean,
    sharedHomeworks: SharedHomeworksDetailsUi?,
    onOpenSharedHomeworks: () -> Unit,
) {
    Surface(
        modifier = modifier.height(248.dp),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ShareHomeworksViewHeader(
                isLoading = isLoadingShare,
                lastActivity = remember(sharedHomeworks) {
                    sharedHomeworks?.received?.values?.maxOfOrNull { it.sendDate }
                },
                onOpenSharedHomeworks = onOpenSharedHomeworks,
            )
            ShareHomeworksViewContent(
                isLoading = isLoadingShare,
                sharedHomeworks = sharedHomeworks,
                onOpenSharedHomeworks = onOpenSharedHomeworks,
            )
        }
    }
}

@Composable
private fun ShareHomeworksViewHeader(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    lastActivity: Instant?,
    onOpenSharedHomeworks: () -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = TasksThemeRes.strings.shareHomeworksHeader,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = buildString {
                    append(TasksThemeRes.strings.shareHomeworksPrefixTitle)
                    if (!isLoading) {
                        if (lastActivity != null) {
                            append(lastActivity.formatByTimeZone(format = Formats.shortDayMonthTimeFormat()))
                        } else {
                            append(StudyAssistantRes.strings.noneTitle)
                        }
                    }
                },
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.MiddleEllipsis,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        OutlinedButton(
            onClick = onOpenSharedHomeworks,
            modifier = Modifier.height(32.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(
                text = TasksThemeRes.strings.showAllTitle,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
private fun ShareHomeworksViewContent(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    sharedHomeworks: SharedHomeworksDetailsUi?,
    onOpenSharedHomeworks: () -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ReceivedHomeworksSection(
            modifier = Modifier.weight(1f),
            isLoading = isLoading,
            receivedHomeworks = sharedHomeworks?.received ?: emptyMap(),
            onOpenSharedHomeworks = { onOpenSharedHomeworks() },
        )
        SentHomeworksSection(
            modifier = Modifier.weight(1f),
            isLoading = isLoading,
            sentHomeworks = sharedHomeworks?.sent ?: emptyMap(),
            onOpenSharedHomeworks = { onOpenSharedHomeworks() },
        )
    }
}

@Composable
private fun ReceivedHomeworksSection(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    receivedHomeworks: Map<UID, ReceivedMediatedHomeworksDetailsUi> = emptyMap(),
    onOpenSharedHomeworks: (ReceivedMediatedHomeworksDetailsUi) -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = TasksThemeRes.strings.shareHomeworksReceivedTitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                style = MaterialTheme.typography.labelSmall,
            )
            Surface(
                modifier = Modifier.size(20.dp),
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                if (!isLoading) {
                    Text(
                        modifier = Modifier.wrapContentSize(Alignment.Center),
                        text = remember(receivedHomeworks) { receivedHomeworks.count().toString() },
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
        Crossfade(
            modifier = Modifier.weight(1f),
            targetState = isLoading,
            animationSpec = floatSpring(),
        ) { loading ->
            if (!loading) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    if (receivedHomeworks.isNotEmpty()) {
                        val homeworks = receivedHomeworks.values.toList()
                        items(homeworks, key = { it.uid }) { mediatedHomework ->
                            SharedHomeworkItem(
                                modifier = Modifier.fillParentMaxWidth().animateItem(),
                                onClick = { onOpenSharedHomeworks(mediatedHomework) },
                                isSender = false,
                                avatar = mediatedHomework.sender.avatar,
                                userName = mediatedHomework.sender.username,
                                countTasks = mediatedHomework.homeworks.count(),
                            )
                        }
                    } else {
                        item {
                            NoneSharedHomeworkItem(modifier = Modifier.fillParentMaxSize())
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = false,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(SHARED_HOMEWORKS) {
                        SharedHomeworkPlaceholder()
                    }
                }
            }
        }
    }
}

@Composable
private fun SentHomeworksSection(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    sentHomeworks: Map<UID, SentMediatedHomeworksDetailsUi> = emptyMap(),
    onOpenSharedHomeworks: (SentMediatedHomeworksDetailsUi) -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = TasksThemeRes.strings.shareHomeworksSentTitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                style = MaterialTheme.typography.labelSmall,
            )
            Surface(
                modifier = Modifier.size(20.dp),
                shape = RoundedCornerShape(6.dp),
                color = StudyAssistantRes.colors.accents.orangeContainer,
            ) {
                if (!isLoading) {
                    Text(
                        modifier = Modifier.wrapContentSize(Alignment.Center),
                        text = remember(sentHomeworks) { sentHomeworks.count().toString() },
                        color = StudyAssistantRes.colors.accents.orange,
                        maxLines = 1,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
        Crossfade(
            modifier = Modifier.weight(1f),
            targetState = isLoading,
            animationSpec = floatSpring(),
        ) { loading ->
            if (!loading) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    if (sentHomeworks.isNotEmpty()) {
                        val homeworks = sentHomeworks.values.toList()
                        items(homeworks, key = { it.uid }) { mediatedHomework ->
                            val mainUserName = mediatedHomework.recipients.getOrNull(0)?.username
                                ?: StudyAssistantRes.strings.noneTitle
                            SharedHomeworkItem(
                                modifier = Modifier.fillParentMaxWidth().animateItem(),
                                onClick = { onOpenSharedHomeworks(mediatedHomework) },
                                isSender = true,
                                avatar = mediatedHomework.recipients.getOrNull(0)?.avatar,
                                userName = if (mediatedHomework.recipients.size > 1) {
                                    buildString {
                                        append(mainUserName)
                                        append(' ')
                                        append('(')
                                        append(mediatedHomework.recipients.size)
                                        append(')')
                                    }
                                } else {
                                    mainUserName
                                },
                                countTasks = mediatedHomework.homeworks.count(),
                            )
                        }
                    } else {
                        item {
                            NoneSharedHomeworkItem(modifier = Modifier.fillParentMaxSize())
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = false,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(SHARED_HOMEWORKS) {
                        SharedHomeworkPlaceholder()
                    }
                }
            }
        }
    }
}

@Composable
private fun SharedHomeworkItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isSender: Boolean,
    avatar: String?,
    userName: String,
    countTasks: Int,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.widthIn(max = 190.dp),
        enabled = enabled,
        color = if (isSender) {
            Color.Transparent
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        border = if (isSender) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        } else {
            null
        },
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AvatarView(
                modifier = Modifier.size(24.dp),
                firstName = userName.split(' ').getOrNull(0) ?: "*",
                secondName = userName.split(' ').getOrNull(1),
                imageUrl = avatar,
                style = MaterialTheme.typography.bodyMedium,
            )
            Column {
                Text(
                    text = userName,
                    color = MaterialTheme.colorScheme.onSurface,
                    overflow = TextOverflow.MiddleEllipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.labelMedium,
                )
                Text(
                    text = TasksThemeRes.strings.sharedHomeworkTasksCountPrefix + countTasks.toString(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    overflow = TextOverflow.MiddleEllipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

@Composable
private fun NoneSharedHomeworkItem(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = TasksThemeRes.strings.shareHomeworksListEmptyTitle,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                style = MaterialTheme.typography.titleSmall,
            )
        }
    }
}

@Composable
private fun SharedHomeworkPlaceholder(modifier: Modifier = Modifier) {
    PlaceholderBox(
        modifier = modifier.height(40.dp).fillMaxWidth().widthIn(max = 190.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = MaterialTheme.shapes.medium,
    )
}