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

package ru.aleshin.studyassistant.editor.impl.domain.interactors

import kotlinx.coroutines.flow.map
import kotlinx.datetime.DayOfWeek
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.functional.FlowDomainResult
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.common.NumberOfRepeatWeek
import ru.aleshin.studyassistant.core.domain.entities.schedules.base.BaseSchedule
import ru.aleshin.studyassistant.core.domain.entities.schedules.base.BaseWeekSchedule
import ru.aleshin.studyassistant.core.domain.repositories.BaseScheduleRepository
import ru.aleshin.studyassistant.editor.impl.domain.common.EditorEitherWrapper
import ru.aleshin.studyassistant.editor.impl.domain.entities.EditorFailures

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
internal interface BaseScheduleInteractor {

    suspend fun fetchScheduleById(uid: UID): FlowDomainResult<EditorFailures, BaseSchedule?>

    suspend fun fetchWeekScheduleByVersion(timeRange: TimeRange, week: NumberOfRepeatWeek): FlowDomainResult<EditorFailures, BaseWeekSchedule>

    class Base(
        private val scheduleRepository: BaseScheduleRepository,
        private val eitherWrapper: EditorEitherWrapper,
    ) : BaseScheduleInteractor {

        override suspend fun fetchScheduleById(uid: UID) = eitherWrapper.wrapFlow {
            scheduleRepository.fetchScheduleById(uid).map { schedule ->
                schedule?.copy(classes = schedule.classes.sortedBy { it.timeRange.from.dateTime().time })
            }
        }

        override suspend fun fetchWeekScheduleByVersion(
            timeRange: TimeRange,
            week: NumberOfRepeatWeek,
        ) = eitherWrapper.wrapFlow {
            val weekDaySchedules = mutableMapOf<DayOfWeek, BaseSchedule>()
            val schedulesByVersion = scheduleRepository.fetchSchedulesByVersion(timeRange, week)

            return@wrapFlow schedulesByVersion.map { rawSchedules ->
                rawSchedules.forEach { schedule ->
                    weekDaySchedules[schedule.dayOfWeek] = schedule.copy(
                        classes = schedule.classes.sortedBy { it.timeRange.from.dateTime().time },
                    )
                }

                BaseWeekSchedule(
                    from = timeRange.from,
                    to = timeRange.to,
                    numberOfWeek = week,
                    weekDaySchedules = weekDaySchedules,
                )
            }
        }
    }
}