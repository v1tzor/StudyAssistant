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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import functional.UID
import kotlinx.datetime.DayOfWeek
import ru.aleshin.studyassistant.editor.api.ui.DayOfNumberedWeekUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.classes.ClassUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.schedules.BaseScheduleUi
import ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.contract.ScheduleEditorViewState
import ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.views.ScheduleView
import ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.views.ScheduleViewPlaceholder
import views.PullToRefreshContainer

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun ScheduleEditorContent(
    state: ScheduleEditorViewState,
    modifier: Modifier = Modifier,
    refreshState: PullToRefreshState = rememberPullToRefreshState(),
    onRefresh: () -> Unit,
    onCreateClass: (DayOfNumberedWeekUi, BaseScheduleUi?) -> Unit,
    onEditClass: (ClassUi, DayOfNumberedWeekUi) -> Unit,
    onDeleteClass: (UID, BaseScheduleUi) -> Unit,
) = with(state) {
    Box(modifier = modifier.nestedScroll(refreshState.nestedScrollConnection)) {
        Crossfade(
            modifier = Modifier.fillMaxSize().padding(start = 16.dp, top = 16.dp, end = 16.dp),
            targetState = isLoading,
            animationSpec = spring(stiffness = Spring.StiffnessLow),
        ) { loading ->
            if (!loading) {
                val listState = rememberLazyListState()
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(DayOfWeek.entries.toTypedArray()) { dayOfWeek ->
                        val dayOfWeekSchedule = weekSchedule?.weekDaySchedules?.get(dayOfWeek)
                        ScheduleView(
                            dayOfWeek = dayOfWeek,
                            schedule = dayOfWeekSchedule,
                            onCreateClass = {
                                onCreateClass(DayOfNumberedWeekUi(dayOfWeek, selectedWeek), dayOfWeekSchedule)
                            },
                            onEditClass = { editClass ->
                                onEditClass(editClass, DayOfNumberedWeekUi(dayOfWeek, selectedWeek))
                            },
                            onDeleteClass = { targetClass ->
                                if (dayOfWeekSchedule != null) onDeleteClass(targetClass.uid, dayOfWeekSchedule)
                            },
                        )
                    }
                    item { Spacer(modifier = Modifier.height(60.dp)) }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(DayOfWeek.entries.size) { ScheduleViewPlaceholder() }
                }
            }
        }
        PullToRefreshContainer(
            state = refreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            isLoading = isLoading,
            onRefresh = onRefresh,
        )
    }
}