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

import entities.classes.Class
import extensions.shiftDay
import functional.FlowDomainResult
import functional.TimeRange
import functional.UID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import repositories.BaseScheduleRepository
import repositories.CalendarSettingsRepository
import repositories.CustomScheduleRepository
import repositories.HomeworksRepository
import repositories.UsersRepository
import ru.aleshin.studyassistant.editor.impl.domain.common.EditorEitherWrapper
import ru.aleshin.studyassistant.editor.impl.domain.entities.ClassesForLinked
import ru.aleshin.studyassistant.editor.impl.domain.entities.EditorFailures

/**
 * @author Stanislav Aleshin on 22.06.2024.
 */
internal interface LinkingClassInteractor {

    suspend fun fetchFreeClassesForHomework(
        subject: UID,
        date: Instant,
        currentHomework: UID?,
    ): FlowDomainResult<EditorFailures, ClassesForLinked>

    class Base(
        private val baseScheduleRepository: BaseScheduleRepository,
        private val customScheduleRepository: CustomScheduleRepository,
        private val homeworksRepository: HomeworksRepository,
        private val calendarRepository: CalendarSettingsRepository,
        private val usersRepository: UsersRepository,
        private val eitherWrapper: EditorEitherWrapper,
    ) : LinkingClassInteractor {

        private val targetUser: UID
            get() = usersRepository.fetchCurrentUserOrError().uid

        @OptIn(ExperimentalCoroutinesApi::class)
        override suspend fun fetchFreeClassesForHomework(
            subject: UID,
            date: Instant,
            currentHomework: UID?,
        ) = eitherWrapper.wrapFlow {
            val maxNumberOfWeek = calendarRepository.fetchSettings(targetUser).first().numberOfWeek

            val searchedTimeRange = TimeRange(
                from = date.shiftDay(-1),
                to = date.shiftDay(14),
            )

            val homeworks = homeworksRepository.fetchHomeworksByTimeRange(searchedTimeRange, targetUser).first()
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
                                    it.value.sortedBy { classModel -> classModel.timeRange.from }
                                }
                                val subjectClasses = customSchedule.classes.filter { classModel ->
                                    val subjectFilter = classModel.subject?.uid == subject
                                    return@filter subjectFilter
                                }
                                val subjectNumberedClasses = subjectClasses.map { classModel ->
                                    val organizationClasses = groupedClasses[classModel.organization.uid]
                                    val number = organizationClasses?.indexOf(classModel)?.inc() ?: 0
                                    Pair(number, classModel)
                                }
                                put(customSchedule.date, subjectNumberedClasses)
                            }
                        }

                        val availableBaseSchedules = baseSchedules.filter { !containsKey(it.key) }

                        availableBaseSchedules.toList().forEach { baseScheduleEntry ->
                            val baseSchedule = baseScheduleEntry.second
                            if (baseSchedule?.classes?.isNotEmpty() == true) {
                                val groupedClasses = baseSchedule.classes.groupBy { it.organization.uid }.mapValues {
                                    it.value.sortedBy { classModel -> classModel.timeRange.from }
                                }
                                val subjectClasses = baseSchedule.classes.filter { classModel ->
                                    val subjectFilter = classModel.subject?.uid == subject
                                    return@filter subjectFilter
                                }
                                val subjectNumberedClasses = subjectClasses.map { classModel ->
                                    val organizationClasses = groupedClasses[classModel.organization.uid]
                                    val number = organizationClasses?.indexOf(classModel)?.inc() ?: 0
                                    Pair(number, classModel)
                                }
                                put(baseScheduleEntry.first, subjectNumberedClasses)
                            }
                        }
                    }
                }
            }
        }
    }
}