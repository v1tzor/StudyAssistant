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

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.extensions.epochTimeDuration
import ru.aleshin.studyassistant.core.common.extensions.equalsDay
import ru.aleshin.studyassistant.core.common.extensions.shiftMillis
import ru.aleshin.studyassistant.core.common.functional.DomainResult
import ru.aleshin.studyassistant.core.common.functional.FlowDomainResult
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.domain.entities.classes.Class
import ru.aleshin.studyassistant.core.domain.entities.organizations.Millis
import ru.aleshin.studyassistant.core.domain.entities.organizations.NumberedDuration
import ru.aleshin.studyassistant.core.domain.entities.schedules.custom.CustomSchedule
import ru.aleshin.studyassistant.core.domain.managers.EndClassesReminderManager
import ru.aleshin.studyassistant.core.domain.managers.StartClassesReminderManager
import ru.aleshin.studyassistant.core.domain.repositories.CustomScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.NotificationSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.editor.impl.domain.common.EditorEitherWrapper
import ru.aleshin.studyassistant.editor.impl.domain.entities.EditorFailures
import ru.aleshin.studyassistant.editor.impl.domain.entities.ShiftTimeError

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
internal interface CustomScheduleInteractor {

    suspend fun addOrUpdateSchedule(schedule: CustomSchedule): DomainResult<EditorFailures, UID>
    suspend fun fetchScheduleById(uid: UID): FlowDomainResult<EditorFailures, CustomSchedule?>
    suspend fun deleteScheduleById(uid: UID): UnitDomainResult<EditorFailures>

    suspend fun updateStartOfDay(
        schedule: CustomSchedule,
        time: Instant,
    ): UnitDomainResult<EditorFailures>

    suspend fun updateClassesDuration(
        schedule: CustomSchedule,
        baseDuration: Millis,
        specificDurations: List<NumberedDuration>
    ): UnitDomainResult<EditorFailures>

    suspend fun updateBreaksDuration(
        schedule: CustomSchedule,
        baseDuration: Millis,
        specificDurations: List<NumberedDuration>
    ): UnitDomainResult<EditorFailures>

    class Base(
        private val scheduleRepository: CustomScheduleRepository,
        private val notificationSettingsRepository: NotificationSettingsRepository,
        private val usersRepository: UsersRepository,
        private val startClassesReminderManager: StartClassesReminderManager,
        private val endClassesReminderManager: EndClassesReminderManager,
        private val eitherWrapper: EditorEitherWrapper,
    ) : CustomScheduleInteractor {

        private val targetUser: UID
            get() = usersRepository.fetchCurrentUserOrError().uid

        override suspend fun addOrUpdateSchedule(schedule: CustomSchedule) = eitherWrapper.wrap {
            return@wrap scheduleRepository.addOrUpdateSchedule(schedule, targetUser).apply {
                updateReminderServices()
            }
        }

        override suspend fun fetchScheduleById(uid: UID) = eitherWrapper.wrapFlow {
            scheduleRepository.fetchScheduleById(uid, targetUser).map { schedule ->
                schedule?.copy(classes = schedule.classes.sortedBy { it.timeRange.from.dateTime().time })
            }
        }

        override suspend fun deleteScheduleById(uid: UID) = eitherWrapper.wrap {
            scheduleRepository.deleteScheduleById(uid, targetUser).apply {
                updateReminderServices()
            }
        }

        override suspend fun updateStartOfDay(
            schedule: CustomSchedule,
            time: Instant,
        ) = eitherWrapper.wrapUnit {
            val startOfDay = schedule.classes.first().timeRange.from
            val endOfDay = schedule.classes.last().timeRange.to

            val startTimeDifference = epochTimeDuration(start = startOfDay, end = time)

            if (!startOfDay.shiftMillis(startTimeDifference).equalsDay(startOfDay) ||
                !endOfDay.shiftMillis(startTimeDifference).equalsDay(endOfDay)
            ) {
                throw ShiftTimeError()
            }

            val updatedClasses = schedule.classes.map { targetClass ->
                targetClass.copy(
                    timeRange = TimeRange(
                        from = targetClass.timeRange.from.shiftMillis(startTimeDifference),
                        to = targetClass.timeRange.to.shiftMillis(startTimeDifference),
                    )
                )
            }
            scheduleRepository.addOrUpdateSchedule(schedule.copy(classes = updatedClasses), targetUser).apply {
                updateReminderServices()
            }
        }

        override suspend fun updateClassesDuration(
            schedule: CustomSchedule,
            baseDuration: Millis,
            specificDurations: List<NumberedDuration>
        ) = eitherWrapper.wrapUnit {
            val updatedClasses = buildList<Class> {
                schedule.classes.forEachIndexed { index, classModel ->
                    val targetClass = lastOrNull() ?: classModel
                    val targetDuration = specificDurations.find { it.number == index.inc() }?.duration ?: baseDuration

                    // Update target class duration
                    val updatedTargetClass = targetClass.copy(
                        timeRange = TimeRange(
                            from = targetClass.timeRange.from,
                            to = targetClass.timeRange.from.shiftMillis(targetDuration),
                        )
                    )
                    if (getOrNull(index) == null) add(updatedTargetClass) else set(index, updatedTargetClass)

                    if (index != schedule.classes.lastIndex) {
                        val nextClass = schedule.classes[index + 1]
                        val endTimeDifference = epochTimeDuration(
                            start = classModel.timeRange.to,
                            end = updatedTargetClass.timeRange.to,
                        )

                        // Restoring the initial break between classes
                        val updatedNextClass = nextClass.copy(
                            timeRange = TimeRange(
                                from = nextClass.timeRange.from.shiftMillis(endTimeDifference),
                                to = nextClass.timeRange.to.shiftMillis(endTimeDifference),
                            )
                        )

                        if (!updatedNextClass.timeRange.to.equalsDay(nextClass.timeRange.to)) {
                            throw ShiftTimeError()
                        } else {
                            add(updatedNextClass)
                        }
                    }
                }
            }

            scheduleRepository.addOrUpdateSchedule(schedule.copy(classes = updatedClasses), targetUser).apply {
                updateReminderServices()
            }
        }

        override suspend fun updateBreaksDuration(
            schedule: CustomSchedule,
            baseDuration: Millis,
            specificDurations: List<NumberedDuration>
        ) = eitherWrapper.wrapUnit {
            val updatedClasses = buildList {
                schedule.classes.forEachIndexed { index, targetClass ->
                    val targetDuration = specificDurations.find { it.number == index }?.duration ?: baseDuration

                    if (index != 0) {
                        val previousClass = last() as Class

                        val breakDifference = epochTimeDuration(
                            start = targetClass.timeRange.from,
                            end = previousClass.timeRange.to.shiftMillis(targetDuration),
                        )

                        val updatedTargetClass = targetClass.copy(
                            timeRange = TimeRange(
                                from = previousClass.timeRange.to.shiftMillis(targetDuration),
                                to = targetClass.timeRange.to.shiftMillis(breakDifference),
                            )
                        )

                        if (!updatedTargetClass.timeRange.to.equalsDay(targetClass.timeRange.to)) {
                            throw ShiftTimeError()
                        } else {
                            add(updatedTargetClass)
                        }
                    } else {
                        add(targetClass)
                    }
                }
            }

            scheduleRepository.addOrUpdateSchedule(schedule.copy(classes = updatedClasses), targetUser).apply {
                updateReminderServices()
            }
        }

        private suspend fun updateReminderServices() {
            val notificationSettings = notificationSettingsRepository.fetchSettings(targetUser).first()
            if (notificationSettings.beginningOfClasses != null) {
                startClassesReminderManager.startOrRetryReminderService()
            }
            if (notificationSettings.endOfClasses) {
                endClassesReminderManager.startOrRetryReminderService()
            }
        }
    }
}