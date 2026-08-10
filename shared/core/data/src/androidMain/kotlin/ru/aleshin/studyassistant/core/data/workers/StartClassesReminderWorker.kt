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

package ru.aleshin.studyassistant.core.data.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Instant
import org.jetbrains.compose.resources.getString
import org.kodein.di.DI
import org.kodein.di.DirectDIAware
import org.kodein.di.bindProvider
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.common.di.coreCommonModule
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.extensions.setHoursAndMinutes
import ru.aleshin.studyassistant.core.common.extensions.shiftMillis
import ru.aleshin.studyassistant.core.common.extensions.toString
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.common.platform.services.CrashlyticsService
import ru.aleshin.studyassistant.core.data.di.coreDataModule
import ru.aleshin.studyassistant.core.data.managers.reminders.NotificationScheduler
import ru.aleshin.studyassistant.core.domain.entities.common.numberOfRepeatWeek
import ru.aleshin.studyassistant.core.domain.entities.schedules.Schedule
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
 * @author Stanislav Aleshin on 22.08.2024.
 */
class StartClassesReminderWorker(
    context: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(context, workerParameters), DirectDIAware {

    override val directDI = DI.direct {
        bindProvider<Context> { applicationContext }
        bindProvider<CrashlyticsService> { CrashlyticsService.Empty() }
        importAll(coreCommonModule, coreDataModule)
    }
    private val notificationScheduler = instance<NotificationScheduler>()
    private val dateManager = instance<DateManager>()
    private val calendarSettingsRepository = instance<CalendarSettingsRepository>()
    private val notificationSettingRepository = instance<NotificationSettingsRepository>()
    private val baseScheduleRepository = instance<BaseScheduleRepository>()
    private val customScheduleRepository = instance<CustomScheduleRepository>()

    override suspend fun doWork(): Result {
        val titlePrefix = getString(CoreRes.string.core_start_classes_reminder_title_prefix)
        val titleSuffix = getString(CoreRes.string.core_start_classes_reminder_title_suffix)
        val daySuffix = getString(CoreRes.string.core_day_suffix)
        val minuteSuffix = getString(CoreRes.string.core_minute_suffix)
        val hourSuffix = getString(CoreRes.string.core_hour_suffix)
        val ongoingBody = getString(CoreRes.string.core_ongoing_class_reminder_body)
        val unknownClassTitle = getString(CoreRes.string.core_ongoing_class_reminder_unknown_title)
        val notificationSettings = notificationSettingRepository.fetchSettings().first()
        val holidays = calendarSettingsRepository.fetchSettings().first().holidays
        val beforeDelay = notificationSettings.beginningOfClasses

        val currentDate = dateManager.fetchBeginningCurrentInstant()
        val schedule = fetchScheduleByDate(currentDate)
        val classes = schedule.mapToValue(
            onBaseSchedule = { it?.classes },
            onCustomSchedule = { it?.classes },
        ).orEmpty().filter { classModel ->
            holidays.none {
                val dateFilter = TimeRange(it.start, it.end).containsDate(currentDate)
                val orgFilter = it.organizations.contains(classModel.organization.uid)
                return@none dateFilter && orgFilter
            }
        }.sortedBy { it.timeRange.from.dateTime().time }
        val groupedClasses = classes.groupBy { classModel -> classModel.organization }.filter { classesEntry ->
            classesEntry.value.isNotEmpty()
        }

        val currentTime = dateManager.fetchCurrentInstant()
        val notificationIds = buildList {
            if (beforeDelay != null) {
                groupedClasses.filter { classesEntry ->
                    notificationSettings.exceptionsForBeginningOfClasses.contains(classesEntry.key.uid).not()
                }.forEach { classesEntry ->
                    val targetTime = currentDate
                        .setHoursAndMinutes(classesEntry.value.first().timeRange.from)
                        .shiftMillis(-beforeDelay)
                    if (targetTime > currentTime) {
                        val title = buildString {
                            append(titlePrefix)
                            append(
                                beforeDelay.toDuration(DurationUnit.MILLISECONDS)
                                    .toString(daySuffix, minuteSuffix, hourSuffix)
                            )
                            append(titleSuffix)
                        }
                        val id = classesEntry.key.uid.hashCode() + START_NOTIFICATION_ID_APPEND
                        add(id)
                        notificationScheduler.scheduleNotification(
                            id = id,
                            title = title,
                            body = classesEntry.key.shortName,
                            time = targetTime,
                        )
                    }
                }
            }

            classes.forEach { classModel ->
                val startTime = currentDate.setHoursAndMinutes(classModel.timeRange.from)
                val endTime = currentDate.setHoursAndMinutes(classModel.timeRange.to)
                if (endTime > currentTime) {
                    val id = classModel.uid.hashCode() + ONGOING_NOTIFICATION_ID_APPEND
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
        notificationScheduler.updateNotificationGroup(NOTIFICATION_GROUP, notificationIds)

        return Result.success()
    }

    private suspend fun fetchScheduleByDate(date: Instant): Schedule {
        val maxNumberOfWeek = calendarSettingsRepository.fetchSettings().first().numberOfWeek
        val currentNumberOfWeek = date.dateTime().date.numberOfRepeatWeek(maxNumberOfWeek)

        val baseSchedule =
            baseScheduleRepository.fetchScheduleByDate(date, currentNumberOfWeek).first()
        val customSchedule = customScheduleRepository.fetchScheduleByDate(date).first()

        return if (customSchedule != null) {
            val schedule = customSchedule.copy(
                classes = customSchedule.classes.sortedBy { it.timeRange.from.dateTime().time }
            )
            Schedule.Custom(schedule)
        } else {
            val schedule = baseSchedule?.copy(
                classes = baseSchedule.classes.sortedBy { it.timeRange.from.dateTime().time }
            )
            Schedule.Base(schedule)
        }
    }

    companion object {
        const val WORK_KEY = "START_CLASSES_REMINDER_SCHEDULER"
        const val REPEAT_WORK_KEY = "START_CLASSES_REMINDER_SCHEDULER_REPEAT"
        const val NOTIFICATION_GROUP = "START_AND_ONGOING_CLASSES_REMINDERS"
        const val START_NOTIFICATION_ID_APPEND = -321
        const val ONGOING_NOTIFICATION_ID_APPEND = 1417
    }
}
