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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.extensions.equalsDay
import ru.aleshin.studyassistant.core.common.functional.Constants.Placeholder
import ru.aleshin.studyassistant.tasks.impl.presentation.models.share.SentMediatedHomeworksDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.DailyHomeworksUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.users.AppUserUi
import ru.aleshin.studyassistant.tasks.impl.presentation.theme.TasksThemeRes

/**
 * @author Stanislav Aleshin on 26.03.2025.
 */
@Composable
internal fun HomeworksOverview(
    modifier: Modifier = Modifier,
    isLoadingHomeworks: Boolean,
    currentDate: Instant,
    homeworks: Map<Instant, DailyHomeworksUi>,
    allFriends: List<AppUserUi>,
    onShowAllHomeworkTasks: () -> Unit,
    onOpenHomeworkTasks: (HomeworkDetailsUi) -> Unit,
    onDoHomework: (HomeworkDetailsUi) -> Unit,
    onSkipHomework: (HomeworkDetailsUi) -> Unit,
    onRepeatHomework: (HomeworkDetailsUi) -> Unit,
    onShareHomeworks: (SentMediatedHomeworksDetailsUi) -> Unit,
) {
    Surface(
        modifier = modifier.height(400.dp),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            HomeworksOverviewHeader(
                onShowAllHomeworks = onShowAllHomeworkTasks
            )
            HomeworksOverviewContent(
                modifier = Modifier.weight(1f),
                isLoadingHomeworks = isLoadingHomeworks,
                currentDate = currentDate,
                homeworks = homeworks,
                allFriends = allFriends,
                onOpenHomeworkTasks = onOpenHomeworkTasks,
                onDoHomework = onDoHomework,
                onSkipHomework = onSkipHomework,
                onRepeatHomework = onRepeatHomework,
                onShareHomeworks = onShareHomeworks,
            )
        }
    }
}

@Composable
private fun HomeworksOverviewHeader(
    modifier: Modifier = Modifier,
    onShowAllHomeworks: () -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = TasksThemeRes.strings.homeworksSectionHeader,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = TasksThemeRes.strings.homeworksSectionSubtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        OutlinedButton(
            onClick = onShowAllHomeworks,
            modifier = Modifier.height(32.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(text = TasksThemeRes.strings.showAllHomeworksTitle)
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun HomeworksOverviewContent(
    modifier: Modifier = Modifier,
    isLoadingHomeworks: Boolean,
    currentDate: Instant,
    homeworks: Map<Instant, DailyHomeworksUi>,
    allFriends: List<AppUserUi>,
    onOpenHomeworkTasks: (HomeworkDetailsUi) -> Unit,
    onDoHomework: (HomeworkDetailsUi) -> Unit,
    onSkipHomework: (HomeworkDetailsUi) -> Unit,
    onRepeatHomework: (HomeworkDetailsUi) -> Unit,
    onShareHomeworks: (SentMediatedHomeworksDetailsUi) -> Unit,
) {
    var isShowedTargetDay by rememberSaveable { mutableStateOf(true) }
    Crossfade(
        modifier = modifier,
        targetState = isLoadingHomeworks,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
    ) { loading ->
        if (loading) {
            LazyRow(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                userScrollEnabled = false,
            ) {
                items(Placeholder.HOMEWORKS) {
                    DailyHomeworksViewPlaceholder()
                }
            }
        } else {
            val listState = rememberLazyListState()
            val homeworksMapList = remember(homeworks) { homeworks.toList() }

            LazyRow(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                userScrollEnabled = true,
            ) {
                items(homeworksMapList, key = { it.first.toString() }) { homeworksEntry ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        var isShowSharedHomeworksSheet by remember { mutableStateOf(false) }
                        DailyHomeworksView(
                            date = homeworksEntry.first,
                            currentDate = currentDate,
                            isPassed = homeworksEntry.first < currentDate,
                            dailyHomeworks = homeworksEntry.second,
                            onDoHomework = onDoHomework,
                            onOpenHomeworkTask = onOpenHomeworkTasks,
                            onSkipHomework = onSkipHomework,
                            onRepeatHomework = onRepeatHomework,
                            onShareHomeworks = { isShowSharedHomeworksSheet = true },
                        )

                        DailyHomeworksVerticalDivider()

                        if (isShowSharedHomeworksSheet) {
                            ShareHomeworksBottomSheet(
                                currentTime = Clock.System.now(),
                                targetDate = homeworksEntry.first,
                                homeworks = homeworksEntry.second.homeworks,
                                allFriends = allFriends,
                                onDismissRequest = { isShowSharedHomeworksSheet = false },
                                onConfirm = {
                                    onShareHomeworks(it)
                                    isShowSharedHomeworksSheet = false
                                },
                            )
                        }
                    }
                }
            }
            LaunchedEffect(true) {
                if (isShowedTargetDay) {
                    val currentDateIndex = homeworksMapList.indexOfFirst {
                        currentDate.equalsDay(it.first)
                    }
                    if (currentDateIndex != -1) {
                        listState.animateScrollToItem(currentDateIndex)
                        isShowedTargetDay = false
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyHomeworksVerticalDivider(
    modifier: Modifier = Modifier,
    headerHeight: Dp = 20.dp,
    verticalArrangement: Dp = 12.dp,
    thickness: Dp = 1.dp,
    color: Color = MaterialTheme.colorScheme.outlineVariant,
) {
    Canvas(modifier.fillMaxHeight().width(thickness)) {
        drawLine(
            color = color,
            strokeWidth = thickness.toPx(),
            start = Offset(thickness.toPx() / 2, 0f),
            end = Offset(thickness.toPx() / 2, headerHeight.toPx()),
        )
        drawLine(
            color = color,
            strokeWidth = thickness.toPx(),
            start = Offset(thickness.toPx() / 2, headerHeight.toPx() + verticalArrangement.toPx()),
            end = Offset(thickness.toPx() / 2, size.height),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 2.dp.toPx()))
        )
    }
}