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

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.format.DateTimeComponents
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.core.common.extensions.formatByTimeZone
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.CircularStepsRow
import ru.aleshin.studyassistant.core.ui.views.menu.AvatarView
import ru.aleshin.studyassistant.core.ui.views.sheet.MediumDragHandle
import ru.aleshin.studyassistant.core.ui.views.shortWeekdayDayMonthFormat
import ru.aleshin.studyassistant.tasks.impl.presentation.models.share.SentMediatedHomeworksDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.subjects.SubjectUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkTaskComponentUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.convertToMediated
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.fetchAllTasks
import ru.aleshin.studyassistant.tasks.impl.presentation.models.users.AppUserUi
import ru.aleshin.studyassistant.tasks.impl.presentation.theme.TasksThemeRes

/**
 * @author Stanislav Aleshin on 24.07.2024.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun ShareHomeworksBottomSheet(
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    pagerState: PagerState = rememberPagerState { ShareHomeworksStep.entries.size },
    currentTime: Instant,
    targetDate: Instant,
    homeworks: List<HomeworkDetailsUi>,
    allFriends: List<AppUserUi>,
    onDismissRequest: () -> Unit,
    onConfirm: (SentMediatedHomeworksDetailsUi) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier,
        dragHandle = { MediumDragHandle() },
        contentWindowInsets = { WindowInsets.navigationBars },
    ) {
        Column(
            modifier = Modifier.padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            var selectedHomeworks by remember { mutableStateOf(homeworks) }
            var targetRecipients by remember { mutableStateOf<List<AppUserUi>>(emptyList()) }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth(),
                userScrollEnabled = false,
            ) { page ->
                when (ShareHomeworksStep.byNumber(page)) {
                    ShareHomeworksStep.SUBJECTS -> ShareHomeworksSheetSubjectsStep(
                        selectedHomeworks = selectedHomeworks,
                        allHomeworks = homeworks,
                        onSelectedHomework = { homework ->
                            selectedHomeworks = buildList {
                                addAll(selectedHomeworks)
                                add(homework)
                            }
                        },
                        onUnselectedHomework = { homework ->
                            selectedHomeworks = buildList {
                                addAll(selectedHomeworks)
                                remove(homework)
                            }
                        },
                    )
                    ShareHomeworksStep.RECIPIENTS -> ShareHomeworksSheetRecipientsStep(
                        recipients = targetRecipients,
                        allFriends = allFriends,
                        onSelectedRecipient = { user ->
                            targetRecipients = buildList {
                                addAll(targetRecipients)
                                add(user)
                            }
                        },
                        onUnselectedRecipient = { user ->
                            targetRecipients = buildList {
                                addAll(targetRecipients)
                                remove(user)
                            }
                        },
                    )
                }
            }
            CircularStepsRow(
                stepsCount = ShareHomeworksStep.entries.size,
                currentStep = pagerState.currentPage,
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val scope = rememberCoroutineScope()
                when (ShareHomeworksStep.byNumber(pagerState.currentPage)) {
                    ShareHomeworksStep.SUBJECTS -> {
                        FilledTonalButton(
                            enabled = selectedHomeworks.isNotEmpty(),
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(ShareHomeworksStep.RECIPIENTS.number)
                                }
                            },
                            content = { Text(text = TasksThemeRes.strings.selectionSubjectStepAction) },
                        )
                    }
                    ShareHomeworksStep.RECIPIENTS -> {
                        Button(
                            enabled = selectedHomeworks.isNotEmpty() && targetRecipients.isNotEmpty(),
                            modifier = Modifier.weight(1f),
                            onClick = {
                                val sentMediatedHomeworks = SentMediatedHomeworksDetailsUi(
                                    uid = randomUUID(),
                                    date = targetDate.startThisDay(),
                                    sendDate = currentTime,
                                    recipients = targetRecipients,
                                    homeworks = selectedHomeworks.map { it.convertToMediated() },
                                )
                                onConfirm(sentMediatedHomeworks)
                            },
                            content = { Text(text = TasksThemeRes.strings.specifyRecipientsStepAction) },
                        )
                        Button(
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(ShareHomeworksStep.SUBJECTS.number)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                            content = { Text(text = StudyAssistantRes.strings.backTitle) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShareHomeworksSheetSubjectsStep(
    modifier: Modifier = Modifier,
    selectedHomeworks: List<HomeworkDetailsUi>,
    allHomeworks: List<HomeworkDetailsUi>,
    onSelectedHomework: (HomeworkDetailsUi) -> Unit,
    onUnselectedHomework: (HomeworkDetailsUi) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = TasksThemeRes.strings.selectionSubjectStepHeader,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = TasksThemeRes.strings.selectionSubjectStepLabel,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        LazyColumn(
            modifier = Modifier.fillMaxWidth().height(350.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(allHomeworks, key = { it.uid }) { homework ->
                ShareHomeworkView(
                    checked = selectedHomeworks.contains(homework),
                    deadline = homework.deadline,
                    subject = homework.subject,
                    theoreticalTasks = homework.theoreticalTasks.components,
                    practicalTasks = homework.practicalTasks.components,
                    presentationTasks = homework.presentationTasks.components,
                    onCheckedChange = { isAdd ->
                        if (isAdd) onSelectedHomework(homework) else onUnselectedHomework(homework)
                    }
                )
            }
        }
    }
}

@Composable
private fun ShareHomeworkView(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    checked: Boolean,
    deadline: Instant,
    subject: SubjectUi?,
    theoreticalTasks: List<HomeworkTaskComponentUi>,
    practicalTasks: List<HomeworkTaskComponentUi>,
    presentationTasks: List<HomeworkTaskComponentUi>,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth().height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Surface(
            modifier = Modifier.fillMaxHeight().width(4.dp),
            shape = MaterialTheme.shapes.small,
            color = subject?.color?.let { Color(it) } ?: MaterialTheme.colorScheme.outline,
            content = { Box(modifier = Modifier.fillMaxHeight()) }
        )
        ShareHomeworkViewContent(
            modifier = Modifier.weight(1f),
            deadline = deadline,
            subject = subject?.name,
            theoreticalTasks = theoreticalTasks,
            practicalTasks = practicalTasks,
            presentationTasks = presentationTasks,
        )
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.size(40.dp),
            enabled = enabled,
        )
    }
}

@Composable
private fun ShareHomeworkViewContent(
    modifier: Modifier = Modifier,
    deadline: Instant,
    subject: String?,
    theoreticalTasks: List<HomeworkTaskComponentUi>,
    practicalTasks: List<HomeworkTaskComponentUi>,
    presentationTasks: List<HomeworkTaskComponentUi>,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Column {
            Text(
                text = deadline.formatByTimeZone(
                    format = DateTimeComponents.Formats.shortWeekdayDayMonthFormat(StudyAssistantRes.strings),
                ),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.labelSmall,
            )
            Text(
                text = subject ?: StudyAssistantRes.strings.noneTitle,
                color = MaterialTheme.colorScheme.onSurface,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                style = MaterialTheme.typography.titleSmall,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            ShareHomeworkTaskCountView(
                painter = painterResource(StudyAssistantRes.icons.theoreticalTasks),
                count = theoreticalTasks.fetchAllTasks().size,
            )
            ShareHomeworkTaskCountView(
                painter = painterResource(StudyAssistantRes.icons.practicalTasks),
                count = practicalTasks.fetchAllTasks().size,
            )
            ShareHomeworkTaskCountView(
                painter = painterResource(StudyAssistantRes.icons.presentationTasks),
                count = presentationTasks.fetchAllTasks().size,
            )
        }
    }
}

@Composable
private fun ShareHomeworkTaskCountView(
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

@Composable
private fun ShareHomeworksSheetRecipientsStep(
    modifier: Modifier = Modifier,
    recipients: List<AppUserUi>,
    allFriends: List<AppUserUi>,
    onSelectedRecipient: (AppUserUi) -> Unit,
    onUnselectedRecipient: (AppUserUi) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = TasksThemeRes.strings.specifyRecipientsStepHeader,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = TasksThemeRes.strings.specifyRecipientsStepLabel,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        LazyColumn(
            modifier = Modifier.fillMaxWidth().height(350.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(allFriends, key = { it.uid }) { friendUser ->
                UserView(
                    checked = recipients.contains(friendUser),
                    name = friendUser.username,
                    email = friendUser.email,
                    avatar = friendUser.avatar,
                    onCheckedChange = { isAdd ->
                        if (isAdd) onSelectedRecipient(friendUser) else onUnselectedRecipient(friendUser)
                    },
                )
            }
        }
    }
}

@Composable
internal fun UserView(
    checked: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    name: String,
    email: String,
    avatar: String?,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AvatarView(
            modifier = Modifier.size(40.dp),
            firstName = name.split(' ').getOrElse(0) { "-" },
            secondName = name.split(' ').getOrNull(1),
            imageUrl = avatar,
        )
        Row(
            modifier = Modifier.fillMaxWidth().animateContentSize(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = name,
                        color = MaterialTheme.colorScheme.onSurface,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 2,
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Text(
                        text = email,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            Box(contentAlignment = Alignment.Center) {
                Checkbox(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                    modifier = Modifier.size(40.dp),
                    enabled = enabled,
                )
            }
        }
    }
}

internal enum class ShareHomeworksStep(val number: Int) {
    SUBJECTS(0), RECIPIENTS(1);

    companion object {
        fun byNumber(number: Int) = when (number) {
            0 -> SUBJECTS
            1 -> RECIPIENTS
            else -> SUBJECTS
        }
    }
}