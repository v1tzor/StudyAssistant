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

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import functional.UID
import kotlinx.datetime.DayOfWeek
import ru.aleshin.studyassistant.editor.api.ui.DayOfNumberedWeekUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.schedules.BaseScheduleUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.classes.ClassUi
import ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.contract.ScheduleEditorViewState
import ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.views.ScheduleView
import ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.views.ScheduleViewPlaceholder

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
@Composable
@OptIn(ExperimentalFoundationApi::class)
internal fun ScheduleEditorContent(
    state: ScheduleEditorViewState,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    onCreateClass: (DayOfNumberedWeekUi, BaseScheduleUi?) -> Unit,
    onEditClass: (ClassUi, DayOfNumberedWeekUi) -> Unit,
    onDeleteClass: (UID, BaseScheduleUi) -> Unit,
) = with(state) {
    AnimatedContent(
        modifier = modifier.fillMaxSize().padding(start = 16.dp, top = 16.dp, end = 16.dp),
        targetState = state.isLoading,
        transitionSpec = {
            fadeIn(animationSpec = tween(500, delayMillis = 180)).togetherWith(
                fadeOut(animationSpec = tween(500))
            )
        },
    ) { loading ->
        if (!loading) {
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(DayOfWeek.entries.toTypedArray()) { dayOfWeek ->
                    val schedule = weekSchedule?.weekDaySchedules?.get(dayOfWeek)
                    ScheduleView(
                        modifier = Modifier.animateItemPlacement(),
                        dayOfWeek = dayOfWeek,
                        schedule = schedule,
                        onCreateClass = {
                            onCreateClass(DayOfNumberedWeekUi(dayOfWeek, currentWeek), schedule)
                        },
                        onEditClass = { editClass ->
                            onEditClass(editClass, DayOfNumberedWeekUi(dayOfWeek, currentWeek))
                        },
                        onDeleteClass = { targetClass ->
                            if (schedule != null) onDeleteClass(targetClass.uid, schedule)
                        },
                    )
                }
            }
        } else {
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(DayOfWeek.entries.size) { ScheduleViewPlaceholder() }
            }
        }
    }
}
