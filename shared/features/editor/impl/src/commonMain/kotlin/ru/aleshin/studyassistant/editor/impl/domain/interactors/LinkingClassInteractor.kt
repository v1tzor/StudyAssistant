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

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.extensions.endOfWeek
import ru.aleshin.studyassistant.core.common.extensions.shiftDay
import ru.aleshin.studyassistant.core.common.extensions.shiftWeek
import ru.aleshin.studyassistant.core.common.functional.FlowDomainResult
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.classes.Class
import ru.aleshin.studyassistant.core.domain.entities.classes.ClassesForLinkedMap
import ru.aleshin.studyassistant.core.domain.repositories.BaseScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.CalendarSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.CustomScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
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
        private val usersRepository: UsersRepository,
        private val eitherWrapper: EditorEitherWrapper,
    ) : LinkingClassInteractor {

        @OptIn(ExperimentalCoroutinesApi::class)
        override suspend fun fetchFreeClassesForHomework(
            subject: UID,
            date: Instant,
        ) = eitherWrapper.wrapFlow {
            val targetUser = usersRepository.fetchCurrentUserOrError().uid
            val maxNumberOfWeek = calendarRepository.fetchSettings(targetUser).first().numberOfWeek

            val searchedTimeRange = TimeRange(
                from = date.shiftDay(-1),
                to = date.endOfWeek().shiftWeek(1),
            )

            val customSchedulesFlow = customScheduleRepository.fetchSchedulesByTimeRange(
                timeRange = searchedTimeRange,
                targetUser = targetUser,
            )
            val baseSchedulesFlow = baseScheduleRepository.fetchSchedulesByTimeRange(
                timeRange = searchedTimeRange,
                maxNumberOfWeek = maxNumberOfWeek,
                targetUser = targetUser,
            )

            return@wrapFlow customSchedulesFlow.flatMapLatest { customSchedules ->
                baseSchedulesFlow.map { baseSchedules ->
                    buildMap<Instant, List<Pair<Int, Class>>> {
                        customSchedules.forEach { customSchedule ->
                            if (customSchedule.classes.isNotEmpty()) {
                                val groupedClasses = customSchedule.classes.groupBy { it.organization.uid }.mapValues {
                                    it.value.sortedBy { classModel -> classModel.timeRange.from.dateTime().time }
                                }
                                val classesWithTargetSubject = customSchedule.classes.filter { classModel ->
                                    val subjectFilter = classModel.subject?.uid == subject
                                    return@filter subjectFilter
                                }
                                val numberedClassesWithTargetSubject = classesWithTargetSubject.map { classModel ->
                                    val organizationClasses = groupedClasses[classModel.organization.uid]
                                    val number = organizationClasses?.indexOf(classModel)?.inc() ?: 0
                                    Pair(number, classModel)
                                }
                                put(customSchedule.date, numberedClassesWithTargetSubject)
                            }
                        }

                        val availableBaseSchedules = baseSchedules.filter { !containsKey(it.key) }

                        availableBaseSchedules.toList().forEach { baseScheduleEntry ->
                            val baseSchedule = baseScheduleEntry.second
                            if (baseSchedule?.classes?.isNotEmpty() == true) {
                                val groupedClasses = baseSchedule.classes.groupBy { it.organization.uid }.mapValues {
                                    it.value.sortedBy { classModel -> classModel.timeRange.from.dateTime().time }
                                }
                                val classesWithTargetSubject = baseSchedule.classes.filter { classModel ->
                                    val subjectFilter = classModel.subject?.uid == subject
                                    return@filter subjectFilter
                                }
                                val numberedClassesWithTargetSubject = classesWithTargetSubject.map { classModel ->
                                    val organizationClasses = groupedClasses[classModel.organization.uid]
                                    val number = organizationClasses?.indexOf(classModel)?.inc() ?: 0
                                    Pair(number, classModel)
                                }
                                put(baseScheduleEntry.first, numberedClassesWithTargetSubject)
                            }
                        }
                    }
                }
            }
        }
    }
}