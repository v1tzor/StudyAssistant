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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.extensions.equalsDay
import ru.aleshin.studyassistant.core.common.functional.Constants.Placeholder
import ru.aleshin.studyassistant.tasks.impl.presentation.models.goals.GoalCreateModelUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.goals.GoalShortUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.share.SentMediatedHomeworksDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.contract.HomeworksViewState
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.views.DailyHomeworksDetailsView
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.views.DailyHomeworksDetailsViewPlaceholder
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.views.ShareHomeworksBottomSheet

/**
 * @author Stanislav Aleshin on 03.07.2024
 */
@Composable
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
internal fun HomeworksContent(
    state: HomeworksViewState,
    modifier: Modifier,
    targetDate: Instant,
    listState: LazyListState = rememberLazyListState(),
    onAddHomework: (Instant) -> Unit,
    onEditHomework: (HomeworkDetailsUi) -> Unit,
    onDoHomework: (HomeworkDetailsUi) -> Unit,
    onSkipHomework: (HomeworkDetailsUi) -> Unit,
    onRepeatHomework: (HomeworkDetailsUi) -> Unit,
    onShareHomeworks: (SentMediatedHomeworksDetailsUi) -> Unit,
    onScheduleGoal: (GoalCreateModelUi) -> Unit,
    onDeleteGoal: (GoalShortUi) -> Unit,
) = with(state) {
    var isShowedTargetDay by rememberSaveable { mutableStateOf(true) }
    Crossfade(
        modifier = modifier.padding(top = 12.dp),
        targetState = isLoading,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
    ) { loading ->
        if (loading) {
            LazyRow(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                userScrollEnabled = false,
            ) {
                items(Placeholder.HOMEWORKS) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DailyHomeworksDetailsViewPlaceholder()
                        DailyHomeworksDetailsVerticalDivider()
                    }
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
                        DailyHomeworksDetailsView(
                            date = homeworksEntry.first,
                            currentDate = currentDate,
                            isPassed = homeworksEntry.first < currentDate,
                            dailyHomeworks = homeworksEntry.second,
                            onAddHomework = onAddHomework,
                            onDoHomework = onDoHomework,
                            onOpenHomeworkTask = onEditHomework,
                            onSkipHomework = onSkipHomework,
                            onRepeatHomework = onRepeatHomework,
                            onShareHomeworks = { isShowSharedHomeworksSheet = true },
                            onScheduleGoal = onScheduleGoal,
                            onDeleteGoal = onDeleteGoal,
                        )

                        DailyHomeworksDetailsVerticalDivider()

                        if (isShowSharedHomeworksSheet) {
                            ShareHomeworksBottomSheet(
                                currentTime = Clock.System.now(),
                                targetDate = homeworksEntry.first,
                                homeworks = homeworksEntry.second.fetchAllHomeworks(),
                                allFriends = friends,
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
private fun DailyHomeworksDetailsVerticalDivider(
    modifier: Modifier = Modifier,
    headerHeight: Dp = 28.dp,
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
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 4.dp.toPx()))
        )
    }
}