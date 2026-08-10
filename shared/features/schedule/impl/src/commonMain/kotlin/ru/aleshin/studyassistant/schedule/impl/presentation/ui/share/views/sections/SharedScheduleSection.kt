/*
 * Copyright 2026 Stanislav Aleshin
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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.views.sections

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.isoDayNumber
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.common.extensions.floatSpring
import ru.aleshin.studyassistant.core.presentation.models.schedules.BaseScheduleUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.schedule.NumberOfWeekItem
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.views.ScheduleWeekChip
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.views.SharedScheduleView
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.views.SharedScheduleViewPlaceholder
import ru.aleshin.studyassistant.schedule.impl.resources.Res
import ru.aleshin.studyassistant.schedule.impl.resources.shared_schedule_header

/**
 * @author Stanislav Aleshin on 08.08.2026.
 */
@Composable
internal fun SharedScheduleSection(
    modifier: Modifier = Modifier,
    linkedSchedules: List<BaseScheduleUi>,
    maxNumberOfWeek: Int,
    isLoading: Boolean = false,
    schedulesRowState: LazyListState = rememberLazyListState(),
) {
    val coroutineScope = rememberCoroutineScope()
    var numberOfWeek by rememberSaveable { mutableIntStateOf(NumberOfWeekItem.ONE.isoWeekNumber) }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(Res.string.shared_schedule_header),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.weight(1f))
            ScheduleWeekChip(
                selected = NumberOfWeekItem.valueOf(numberOfWeek),
                maxNumberOfWeek = maxNumberOfWeek,
                onSelect = { selectedWeek ->
                    numberOfWeek = selectedWeek.isoWeekNumber
                    coroutineScope.launch { schedulesRowState.animateScrollToItem(0) }
                },
            )
        }
        Crossfade(
            targetState = isLoading,
            animationSpec = floatSpring(),
        ) { loading ->
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                state = schedulesRowState,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                userScrollEnabled = !loading,
            ) {
                items(
                    items = DayOfWeek.entries,
                    key = { dayOfWeek -> dayOfWeek.isoDayNumber },
                ) { dayOfWeek ->
                    if (loading) {
                        SharedScheduleViewPlaceholder()
                    } else {
                        val schedule = linkedSchedules.find { item ->
                            item.dayOfWeek == dayOfWeek &&
                                item.week.isoRepeatWeekNumber == numberOfWeek
                        }
                        SharedScheduleView(
                            dayOfWeek = dayOfWeek,
                            classes = schedule?.classes.orEmpty(),
                        )
                    }
                }
            }
        }
    }
}
