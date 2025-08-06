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

package ru.aleshin.studyassistant.core.data.workers

import android.app.AlarmManager
import android.app.AlarmManager.RTC_WAKEUP
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_CANCEL_CURRENT
import android.app.PendingIntent.FLAG_MUTABLE
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Instant
import org.kodein.di.DI
import org.kodein.di.DirectDIAware
import org.kodein.di.bindProvider
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.common.di.coreCommonModule
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.extensions.fetchCurrentLanguage
import ru.aleshin.studyassistant.core.common.extensions.setHoursAndMinutes
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.common.messages.LocalNotificationReceiver
import ru.aleshin.studyassistant.core.common.platform.services.CrashlyticsService
import ru.aleshin.studyassistant.core.data.di.coreDataModule
import ru.aleshin.studyassistant.core.domain.entities.common.numberOfRepeatWeek
import ru.aleshin.studyassistant.core.domain.entities.schedules.Schedule
import ru.aleshin.studyassistant.core.domain.repositories.BaseScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.CalendarSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.CustomScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.NotificationSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.OrganizationsRepository
import ru.aleshin.studyassistant.core.ui.theme.tokens.StudyAssistantStrings
import ru.aleshin.studyassistant.core.ui.theme.tokens.fetchAppLanguage
import ru.aleshin.studyassistant.core.ui.theme.tokens.fetchCoreStrings

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
    private val coreStrings: StudyAssistantStrings
        get() = fetchCoreStrings(fetchAppLanguage(applicationContext.fetchCurrentLanguage()))

    private val alarmManager by lazy { applicationContext.getSystemService(AlarmManager::class.java) }
    private val dateManager = instance<DateManager>()
    private val calendarSettingsRepository = instance<CalendarSettingsRepository>()
    private val notificationSettingRepository = instance<NotificationSettingsRepository>()
    private val baseScheduleRepository = instance<BaseScheduleRepository>()
    private val customScheduleRepository = instance<CustomScheduleRepository>()
    private val organizationsRepository = instance<OrganizationsRepository>()

    override suspend fun doWork(): Result {
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

        clearOldNotifications()

        groupedClasses?.forEach { classesEntry ->
            val endClassesTime = classesEntry.value.last().timeRange.to
            val currentTime = dateManager.fetchCurrentInstant()
            val targetTime = currentDate.setHoursAndMinutes(endClassesTime)

            if (targetTime > currentTime) {
                val title = coreStrings.endClassesReminderTitle
                val body = classesEntry.key.shortName

                val id = classesEntry.key.uid.hashCode() + NOTIFICATION_ID_APPEND
                val intent = LocalNotificationReceiver.createIntent(applicationContext, title, body)
                val flag = FLAG_CANCEL_CURRENT or FLAG_MUTABLE
                val pendingIntent = PendingIntent.getBroadcast(applicationContext, id, intent, flag)
                val time = targetTime.toEpochMilliseconds()
                alarmManager.setExactAndAllowWhileIdle(RTC_WAKEUP, time, pendingIntent)
            }
        }

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

    private suspend fun clearOldNotifications() {
        val organizations = organizationsRepository.fetchAllShortOrganization().first()
        organizations.forEach { organization ->
            val id = organization.uid.hashCode() + NOTIFICATION_ID_APPEND
            val intent = LocalNotificationReceiver.createCancelIntent(applicationContext)
            val cancelFlag = FLAG_CANCEL_CURRENT or FLAG_MUTABLE
            val cancelPendingIntent = PendingIntent.getBroadcast(applicationContext, id, intent, cancelFlag)
            if (cancelPendingIntent != null) {
                alarmManager.cancel(cancelPendingIntent)
                cancelPendingIntent.cancel()
            }
        }
    }

    companion object {
        const val WORK_KEY = "END_CLASSES_REMINDER_SCHEDULER"
        const val REPEAT_WORK_KEY = "END_CLASSES_REMINDER_SCHEDULER_REPEAT"
        const val NOTIFICATION_ID_APPEND = 924
    }
}