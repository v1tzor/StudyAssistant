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

package ru.aleshin.studyassistant.core.data.managers.reminders

import kotlinx.coroutines.flow.first
import kotlinx.datetime.Instant
import org.jetbrains.compose.resources.getString
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.extensions.setHoursAndMinutes
import ru.aleshin.studyassistant.core.common.extensions.shiftDay
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.domain.entities.common.NumberOfRepeatWeek
import ru.aleshin.studyassistant.core.domain.entities.common.numberOfRepeatWeek
import ru.aleshin.studyassistant.core.domain.entities.schedules.Schedule
import ru.aleshin.studyassistant.core.domain.managers.reminders.EndClassesReminderManager
import ru.aleshin.studyassistant.core.domain.repositories.BaseScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.CalendarSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.CustomScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.NotificationSettingsRepository
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.end_classes_reminder_title as core_end_classes_reminder_title

/**
 * @author Stanislav Aleshin on 24.08.2024.
 */
actual class EndClassesReminderManagerImpl(
    private val notificationScheduler: NotificationScheduler,
    private val notificationSettingsRepository: NotificationSettingsRepository,
    private val calendarSettingsRepository: CalendarSettingsRepository,
    private val baseScheduleRepository: BaseScheduleRepository,
    private val customScheduleRepository: CustomScheduleRepository,
    private val dateManager: DateManager,
) : EndClassesReminderManager {

    actual override suspend fun startOrRetryReminderService() {
        val reminderTitle = getString(CoreRes.string.core_end_classes_reminder_title)
        val notificationSettings = notificationSettingsRepository.fetchSettings().first()
        val calendarSettings = calendarSettingsRepository.fetchSettings().first()
        val currentTime = dateManager.fetchCurrentInstant()
        val currentDate = dateManager.fetchBeginningCurrentInstant()

        val notificationIds = buildList {
            repeat(SCHEDULE_DAYS) { dayShift ->
                val date = currentDate.shiftDay(dayShift)
                val schedule = fetchScheduleByDate(date, calendarSettings.numberOfWeek)
                val classes = schedule.mapToValue(
                    onBaseSchedule = { it?.classes },
                    onCustomSchedule = { it?.classes },
                ).orEmpty().filter { classModel ->
                    calendarSettings.holidays.none { holiday ->
                        val dateFilter = TimeRange(holiday.start, holiday.end).containsDate(date)
                        val organizationFilter = holiday.organizations.contains(classModel.organization.uid)
                        dateFilter && organizationFilter
                    }
                }.sortedBy { it.timeRange.from.dateTime().time }

                classes.groupBy { it.organization }.filterKeys { organization ->
                    organization.uid !in notificationSettings.exceptionsForEndOfClasses
                }.forEach { (organization, organizationClasses) ->
                    val targetTime = date.setHoursAndMinutes(organizationClasses.last().timeRange.to)
                    if (targetTime > currentTime) {
                        val id = "$NOTIFICATION_PREFIX:${organization.uid}:$date".hashCode()
                        add(id)
                        notificationScheduler.scheduleNotification(
                            id = id,
                            title = reminderTitle,
                            body = organization.shortName,
                            time = targetTime,
                        )
                    }
                }
            }
        }
        notificationScheduler.updateNotificationGroup(NOTIFICATION_GROUP, notificationIds)
    }

    actual override suspend fun stopReminderService(allOrganizations: List<UID>) {
        notificationScheduler.clearNotificationGroup(NOTIFICATION_GROUP)
    }

    private suspend fun fetchScheduleByDate(
        date: Instant,
        maxNumberOfWeek: NumberOfRepeatWeek,
    ): Schedule {
        val currentNumberOfWeek = date.dateTime().date.numberOfRepeatWeek(maxNumberOfWeek)
        val baseSchedule = baseScheduleRepository.fetchScheduleByDate(date, currentNumberOfWeek).first()
        val customSchedule = customScheduleRepository.fetchScheduleByDate(date).first()
        return if (customSchedule != null) {
            Schedule.Custom(customSchedule)
        } else {
            Schedule.Base(baseSchedule)
        }
    }

    companion object {
        private const val SCHEDULE_DAYS = 5
        private const val NOTIFICATION_GROUP = "IOS_END_CLASSES_REMINDERS"
        private const val NOTIFICATION_PREFIX = "end"
    }
}
