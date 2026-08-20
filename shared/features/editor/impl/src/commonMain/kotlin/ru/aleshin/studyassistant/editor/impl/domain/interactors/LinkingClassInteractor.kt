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

package ru.aleshin.studyassistant.editor.impl.domain.interactors

import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.extensions.endOfWeek
import ru.aleshin.studyassistant.core.common.extensions.shiftDay
import ru.aleshin.studyassistant.core.common.extensions.shiftWeek
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.common.functional.FlowDomainResult
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.classes.Class
import ru.aleshin.studyassistant.core.domain.entities.classes.ClassesForLinkedMap
import ru.aleshin.studyassistant.core.domain.entities.common.NumberOfRepeatWeek
import ru.aleshin.studyassistant.core.domain.entities.common.numberOfRepeatWeek
import ru.aleshin.studyassistant.core.domain.entities.schedules.base.BaseSchedule
import ru.aleshin.studyassistant.core.domain.entities.schedules.custom.CustomSchedule
import ru.aleshin.studyassistant.core.domain.repositories.BaseScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.CalendarSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.CustomScheduleRepository
import ru.aleshin.studyassistant.editor.impl.domain.common.EditorEitherWrapper
import ru.aleshin.studyassistant.editor.impl.domain.entities.EditorFailures

/**
 * @author Stanislav Aleshin on 22.06.2024.
 */
internal interface LinkingClassInteractor {

    suspend fun fetchFreeClassesForHomework(
        subject: UID,
        date: Instant,
    ): FlowDomainResult<EditorFailures, ClassesForLinkedMap>

    class Base(
        private val baseScheduleRepository: BaseScheduleRepository,
        private val customScheduleRepository: CustomScheduleRepository,
        private val calendarRepository: CalendarSettingsRepository,
        private val eitherWrapper: EditorEitherWrapper,
    ) : LinkingClassInteractor {

        override suspend fun fetchFreeClassesForHomework(
            subject: UID,
            date: Instant,
        ) = eitherWrapper.wrapFlow {
            val maxNumberOfWeek = calendarRepository.fetchSettings().first().numberOfWeek

            val searchedTimeRange = TimeRange(
                from = date.shiftDay(-1),
                to = date.endOfWeek().shiftWeek(1),
            )

            val customSchedulesFlow = customScheduleRepository.fetchSchedulesByTimeRange(
                timeRange = searchedTimeRange,
            )
            val baseSchedulesFlow = baseScheduleRepository.fetchSchedulesByVersion(
                version = searchedTimeRange,
                numberOfWeek = null,
            )

            return@wrapFlow combine(customSchedulesFlow, baseSchedulesFlow) { customSchedules, baseSchedules ->
                val customByDate = customSchedules.associateBy { schedule ->
                    schedule.date.startThisDay()
                }
                buildMap<Instant, List<Class>> {
                    searchedTimeRange.periodDates().forEach { rawDate ->
                        val targetDate = rawDate.startThisDay()
                        val customSchedule = customByDate[targetDate]
                        val dayClasses = classesForDate(
                            date = targetDate,
                            maxNumberOfWeek = maxNumberOfWeek,
                            customSchedule = customSchedule,
                            baseSchedules = baseSchedules,
                        )
                        val numbered = numberedSubjectClasses(dayClasses, subject)
                        if (numbered.isNotEmpty()) {
                            put(targetDate, numbered)
                        }
                    }
                }
            }
        }
    }
}

private fun classesForDate(
    date: Instant,
    maxNumberOfWeek: NumberOfRepeatWeek,
    customSchedule: CustomSchedule?,
    baseSchedules: List<BaseSchedule>,
): List<Class> {
    if (customSchedule != null) {
        return customSchedule.classes
    }
    val week = date.dateTime().date.numberOfRepeatWeek(maxNumberOfWeek)
    val dayOfWeek = date.dateTime().dayOfWeek
    return baseSchedules
        .filter { schedule ->
            schedule.dayOfWeek == dayOfWeek &&
                schedule.week == week &&
                schedule.dateVersion.containsDate(date)
        }
        .maxByOrNull { schedule -> schedule.dateVersion.to }
        ?.classes
        .orEmpty()
}

private fun numberedSubjectClasses(
    classes: List<Class>,
    subject: UID,
): List<Class> {
    if (classes.isEmpty()) return emptyList()
    val groupedClasses = classes.groupBy { classModel -> classModel.organization.uid }
        .mapValues { entry ->
            entry.value.sortedBy { classModel -> classModel.timeRange.from.dateTime().time }
        }
    return classes.filter { classModel ->
        classModel.subject?.uid == subject
    }.map { classModel ->
        val organizationClasses = groupedClasses[classModel.organization.uid]
        val number = organizationClasses?.indexOf(classModel)?.inc() ?: 0
        classModel.copy(number = number)
    }
}
