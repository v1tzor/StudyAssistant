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
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.end_classes_reminder_title as core_end_classes_reminder_title

/**
 * @author Stanislav Aleshin on 22.08.2024.
 */
class EndClassesReminderWorker(
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
        val reminderTitle = getString(CoreRes.string.core_end_classes_reminder_title)
        val holidays = calendarSettingsRepository.fetchSettings().first().holidays
        val notificationSettings = notificationSettingRepository.fetchSettings().first()

        val currentDate = dateManager.fetchBeginningCurrentInstant()
        val schedule = fetchScheduleByDate(currentDate)
        val groupedClasses = schedule.mapToValue(
            onBaseSchedule = { it?.classes?.groupBy { classModel -> classModel.organization } },
            onCustomSchedule = { it?.classes?.groupBy { classModel -> classModel.organization } },
        )?.mapValues { entry ->
            entry.value.filter { classModel ->
                holidays.none {
                    val dateFilter = TimeRange(it.start, it.end).containsDate(currentDate)
                    val orgFilter = it.organizations.contains(classModel.organization.uid)
                    return@none dateFilter && orgFilter
                }
            }
        }?.filter { classesEntry ->
            notificationSettings.exceptionsForEndOfClasses.contains(classesEntry.key.uid).not() &&
                    classesEntry.value.isNotEmpty()
        }

        val notificationIds = buildList {
            groupedClasses?.forEach { classesEntry ->
                val endClassesTime = classesEntry.value.last().timeRange.to
                val currentTime = dateManager.fetchCurrentInstant()
                val targetTime = currentDate.setHoursAndMinutes(endClassesTime)

                if (targetTime > currentTime) {
                    val title = reminderTitle
                    val body = classesEntry.key.shortName

                    val id = classesEntry.key.uid.hashCode() + NOTIFICATION_ID_APPEND
                    add(id)
                    notificationScheduler.scheduleNotification(id, title, body, targetTime)
                }
            }
        }
        notificationScheduler.updateNotificationGroup(NOTIFICATION_GROUP, notificationIds)

        return Result.success()
    }

    private suspend fun fetchScheduleByDate(date: Instant): Schedule {
        val maxNumberOfWeek = calendarSettingsRepository.fetchSettings().first().numberOfWeek
        val currentNumberOfWeek = date.dateTime().date.numberOfRepeatWeek(maxNumberOfWeek)

        val baseSchedule = baseScheduleRepository.fetchScheduleByDate(date, currentNumberOfWeek).first()
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
        const val WORK_KEY = "END_CLASSES_REMINDER_SCHEDULER"
        const val REPEAT_WORK_KEY = "END_CLASSES_REMINDER_SCHEDULER_REPEAT"
        const val NOTIFICATION_GROUP = "END_CLASSES_REMINDERS"
        const val NOTIFICATION_ID_APPEND = 924
    }
}
