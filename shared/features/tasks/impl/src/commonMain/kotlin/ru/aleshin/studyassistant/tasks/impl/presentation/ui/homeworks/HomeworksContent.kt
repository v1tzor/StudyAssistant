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

package ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.char
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.extensions.endOfWeek
import ru.aleshin.studyassistant.core.common.extensions.equalsDay
import ru.aleshin.studyassistant.core.common.extensions.formatByTimeZone
import ru.aleshin.studyassistant.core.ui.mappers.mapToSting
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.theme.tokens.dayOfWeekShortNames
import ru.aleshin.studyassistant.core.ui.theme.tokens.monthNames
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.contract.HomeworksViewState
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.views.HomeworksDetailsViewItem
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.views.HomeworksDetailsViewNoneItem
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.views.HomeworksDetailsViewPlaceholder
import kotlin.time.Duration.Companion.days

/**
 * @author Stanislav Aleshin on 03.07.2024
 */
@Composable
@OptIn(ExperimentalFoundationApi::class)
internal fun HomeworksContent(
    state: HomeworksViewState,
    modifier: Modifier,
    targetDate: Instant,
    listState: LazyListState = rememberLazyListState(),
    onEditHomework: (HomeworkDetailsUi) -> Unit,
    onDoHomework: (HomeworkDetailsUi) -> Unit,
    onSkipHomework: (HomeworkDetailsUi) -> Unit,
    onRepeatHomework: (HomeworkDetailsUi) -> Unit,
) = with(state) {
    var isShowTargetDay by rememberSaveable { mutableStateOf(true) }
    Crossfade(
        modifier = modifier,
        targetState = isLoading,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
    ) { loading ->
        if (!loading) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                homeworks.forEach { homeworksEntry ->
                    stickyHeader(key = homeworksEntry.key.toString()) {
                        HomeworkDateHeader(
                            modifier = Modifier.animateItemPlacement(),
                            currentDate = state.currentDate,
                            date = homeworksEntry.key,
                            progressList = homeworksEntry.value.map { it.completeDate != null },
                        )
                    }
                    if (homeworksEntry.value.isNotEmpty()) {
                        items(homeworksEntry.value, key = { it.uid }) { homework ->
                            HomeworksDetailsViewItem(
                                modifier = Modifier.padding(horizontal = 16.dp).animateItemPlacement(),
                                subject = homework.subject,
                                organization = homework.organization,
                                status = homework.status,
                                theoreticalTasks = homework.theoreticalTasks.components,
                                practicalTasks = homework.practicalTasks.components,
                                presentationTasks = homework.presentationTasks.components,
                                testTopic = homework.test,
                                priority = homework.priority,
                                completeDate = homework.completeDate,
                                onEdit = { onEditHomework(homework) },
                                onRepeat = { onRepeatHomework(homework) },
                                onSkip = { onSkipHomework(homework) },
                                onDone = { onDoHomework(homework) },
                            )
                        }
                    } else {
                        item {
                            HomeworksDetailsViewNoneItem(
                                modifier = Modifier.padding(horizontal = 16.dp).animateItemPlacement(),
                            )
                        }
                    }
                    if (homeworksEntry.key.endOfWeek().equalsDay(homeworksEntry.key)) {
                        item {
                            HorizontalDivider(modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }
            LaunchedEffect(true) {
                if (isShowTargetDay && selectedTimeRange?.containsDate(targetDate) == true) {
                    val index = homeworks.toList().filter { it.first < targetDate }.sumOf {
                        val header = 1
                        val homeworks = it.second.size.takeIf { size -> size > 0 } ?: 1
                        val divider = if (it.first.endOfWeek().equalsDay(it.first)) 1 else 0
                        return@sumOf header + homeworks + divider
                    }
                    listState.animateScrollToItem(index)
                    isShowTargetDay = false
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(top = 12.dp).fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                userScrollEnabled = false,
            ) {
                item {
                    HomeworksDetailsViewPlaceholder(
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeworkDateHeader(
    modifier: Modifier = Modifier,
    currentDate: Instant,
    date: Instant,
    progressList: List<Boolean>,
) {
    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val coreStrings = StudyAssistantRes.strings
        val shortDateFormat = DateTimeComponents.Format {
            dayOfMonth()
            char(' ')
            monthName(coreStrings.monthNames())
        }
        val detailsDateFormat = DateTimeComponents.Format {
            dayOfWeek(coreStrings.dayOfWeekShortNames())
            chars(", ")
            dayOfMonth()
            char(' ')
            monthName(coreStrings.monthNames())
        }
        val daysDifference = currentDate.daysUntil(date, TimeZone.currentSystemDefault()).days.inWholeDays
        Text(
            text = when (daysDifference) {
                0L -> coreStrings.todayTitle
                1L -> coreStrings.tomorrowTitle
                -1L -> coreStrings.yesterdayTitle
                else -> date.dateTime().dayOfWeek.mapToSting(coreStrings)
            },
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = if (daysDifference in -1..1) {
                date.formatByTimeZone(detailsDateFormat)
            } else {
                date.formatByTimeZone(shortDateFormat)
            },
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            style = MaterialTheme.typography.titleSmall,
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = if (daysDifference == 0L) 2.dp else 1.dp,
            color = if (daysDifference == 0L) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            },
        )
        if (progressList.isNotEmpty()) {
            val progress = progressList.count { it } / progressList.size.toFloat()
            Text(
                text = buildString {
                    append(progressList.count { it }, "/", progressList.size)
                },
                color = if (progress == 1f) {
                    StudyAssistantRes.colors.accents.green
                } else if (date < currentDate) {
                    StudyAssistantRes.colors.accents.red
                } else {
                    StudyAssistantRes.colors.accents.orange
                },
                maxLines = 1,
                style = MaterialTheme.typography.titleSmall,
            )
        }
    }
}