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
import ru.aleshin.studyassistant.core.common.extensions.shiftMillis
import ru.aleshin.studyassistant.core.common.extensions.toString
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.domain.entities.common.NumberOfRepeatWeek
import ru.aleshin.studyassistant.core.domain.entities.common.numberOfRepeatWeek
import ru.aleshin.studyassistant.core.domain.entities.schedules.Schedule
import ru.aleshin.studyassistant.core.domain.managers.reminders.StartClassesReminderManager
import ru.aleshin.studyassistant.core.domain.repositories.BaseScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.CalendarSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.CustomScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.NotificationSettingsRepository
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.day_suffix as core_day_suffix
import ru.aleshin.studyassistant.core.ui.resources.hour_suffix as core_hour_suffix
import ru.aleshin.studyassistant.core.ui.resources.minute_suffix as core_minute_suffix
import ru.aleshin.studyassistant.core.ui.resources.ongoing_class_reminder_body as core_ongoing_class_reminder_body
import ru.aleshin.studyassistant.core.ui.resources.ongoing_class_reminder_unknown_title as core_ongoing_class_reminder_unknown_title
import ru.aleshin.studyassistant.core.ui.resources.start_classes_reminder_title_prefix as core_start_classes_reminder_title_prefix
import ru.aleshin.studyassistant.core.ui.resources.start_classes_reminder_title_suffix as core_start_classes_reminder_title_suffix

/**
 * @author Stanislav Aleshin on 24.08.2024.
 */
actual class StartClassesReminderManagerImpl(
    private val notificationScheduler: NotificationScheduler,
    private val notificationSettingsRepository: NotificationSettingsRepository,
    private val calendarSettingsRepository: CalendarSettingsRepository,
    private val baseScheduleRepository: BaseScheduleRepository,
    private val customScheduleRepository: CustomScheduleRepository,
    private val dateManager: DateManager,
) : StartClassesReminderManager {

    actual override suspend fun startOrRetryReminderService() {
        val titlePrefix = getString(CoreRes.string.core_start_classes_reminder_title_prefix)
        val titleSuffix = getString(CoreRes.string.core_start_classes_reminder_title_suffix)
        val daySuffix = getString(CoreRes.string.core_day_suffix)
        val minuteSuffix = getString(CoreRes.string.core_minute_suffix)
        val hourSuffix = getString(CoreRes.string.core_hour_suffix)
        val ongoingBody = getString(CoreRes.string.core_ongoing_class_reminder_body)
        val unknownClassTitle = getString(CoreRes.string.core_ongoing_class_reminder_unknown_title)
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

                notificationSettings.beginningOfClasses?.let { beforeDelay ->
                    classes.groupBy { it.organization }.filterKeys { organization ->
                        organization.uid !in notificationSettings.exceptionsForBeginningOfClasses
                    }.forEach { (organization, organizationClasses) ->
                        val targetTime = date
                            .setHoursAndMinutes(organizationClasses.first().timeRange.from)
                            .shiftMillis(-beforeDelay)
                        if (targetTime > currentTime) {
                            val id = "$START_NOTIFICATION_PREFIX:${organization.uid}:$date".hashCode()
                            val title = buildString {
                                append(titlePrefix)
                                append(
                                    beforeDelay.toDuration(DurationUnit.MILLISECONDS)
                                        .toString(daySuffix, minuteSuffix, hourSuffix)
                                )
                                append(titleSuffix)
                            }
                            add(id)
                            notificationScheduler.scheduleNotification(
                                id = id,
                                title = title,
                                body = organization.shortName,
                                time = targetTime,
                            )
                        }
                    }
                }

                classes.forEach { classModel ->
                    val startTime = date.setHoursAndMinutes(classModel.timeRange.from)
                    val endTime = date.setHoursAndMinutes(classModel.timeRange.to)
                    if (endTime > currentTime) {
                        val id = "$ONGOING_NOTIFICATION_PREFIX:${classModel.uid}:$date".hashCode()
                        val title = classModel.subject?.name
                            ?: classModel.customData
                            ?: unknownClassTitle
                        val details = buildList {
                            add(classModel.organization.shortName)
                            classModel.office.takeIf(String::isNotBlank)?.let(::add)
                        }.joinToString(separator = " · ")
                        add(id)
                        notificationScheduler.scheduleOngoingNotification(
                            id = id,
                            title = title,
                            body = ongoingBody.replace("%1\$s", details),
                            time = if (startTime > currentTime) startTime else currentTime,
                            endTime = endTime,
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
        private const val NOTIFICATION_GROUP = "IOS_START_AND_ONGOING_CLASSES_REMINDERS"
        private const val START_NOTIFICATION_PREFIX = "start"
        private const val ONGOING_NOTIFICATION_PREFIX = "ongoing"
    }
}
