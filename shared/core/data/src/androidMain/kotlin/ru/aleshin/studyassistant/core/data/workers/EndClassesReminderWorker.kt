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

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Instant
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.common.di.MainDirectDIAware
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.extensions.fetchCurrentLanguage
import ru.aleshin.studyassistant.core.common.extensions.setHoursAndMinutes
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.data.managers.NotificationScheduler.Companion.BODY_KEY
import ru.aleshin.studyassistant.core.data.managers.NotificationScheduler.Companion.TITLE_KEY
import ru.aleshin.studyassistant.core.domain.entities.common.numberOfRepeatWeek
import ru.aleshin.studyassistant.core.domain.entities.schedules.Schedule
import ru.aleshin.studyassistant.core.domain.repositories.BaseScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.CalendarSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.CustomScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.NotificationSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.core.ui.theme.tokens.fetchAppLanguage
import ru.aleshin.studyassistant.core.ui.theme.tokens.fetchCoreStrings
import java.util.concurrent.TimeUnit

/**
 * @author Stanislav Aleshin on 22.08.2024.
 */
class EndClassesReminderWorker(
    context: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(context, workerParameters), MainDirectDIAware {

    private val workManager by lazy { WorkManager.getInstance(applicationContext) }
    private val dateManager = instance<DateManager>()
    private val usersRepository = instance<UsersRepository>()
    private val calendarSettingsRepository = instance<CalendarSettingsRepository>()
    private val notificationSettingRepository = instance<NotificationSettingsRepository>()
    private val baseScheduleRepository = instance<BaseScheduleRepository>()
    private val customScheduleRepository = instance<CustomScheduleRepository>()

    override suspend fun doWork(): Result {
        val currentUser = usersRepository.fetchCurrentAppUser()?.uid ?: return Result.failure()
        val notificationSettings = notificationSettingRepository.fetchSettings(currentUser).first()

        val currentDate = dateManager.fetchBeginningCurrentInstant()
        val schedule = fetchScheduleByDate(currentUser, currentDate)
        val groupedClasses = schedule.mapToValue(
            onBaseSchedule = { it?.classes?.groupBy { classModel -> classModel.organization } },
            onCustomSchedule = { it?.classes?.groupBy { classModel -> classModel.organization } },
        )?.filter { classesEntry ->
            notificationSettings.exceptionsForEndOfClasses.contains(classesEntry.key.uid).not() &&
                classesEntry.value.isNotEmpty()
        }

        val coreStrings = fetchCoreStrings(fetchAppLanguage(applicationContext.fetchCurrentLanguage()))

        groupedClasses?.forEach { classesEntry ->
            val endClassesTime = classesEntry.value.last().timeRange.to
            val targetTime = currentDate.setHoursAndMinutes(endClassesTime)

            val currentTime = dateManager.fetchCurrentInstant()
            val delayDuration = targetTime - currentTime

            if (delayDuration.isPositive()) {
                val title = coreStrings.endClassesReminderTitle
                val body = classesEntry.key.shortName

                val inputData = Data.Builder().apply {
                    putString(TITLE_KEY, title)
                    putString(BODY_KEY, body)
                }.build()

                val request = OneTimeWorkRequestBuilder<NotificationWorker>().apply {
                    addTag(NOTIFICATION_WORK_TAG)
                    setInitialDelay(delayDuration.inWholeMilliseconds, TimeUnit.MILLISECONDS)
                    setInputData(inputData)
                }.build()
                val workKey = NOTIFICATION_KEY_PREFIX + classesEntry.key.uid

                workManager.enqueueUniqueWork(workKey, ExistingWorkPolicy.REPLACE, request)
            }
        }

        return Result.success()
    }

    private suspend fun fetchScheduleByDate(currentUser: UID, date: Instant): Schedule {
        val maxNumberOfWeek = calendarSettingsRepository.fetchSettings(currentUser).first().numberOfWeek
        val currentNumberOfWeek = date.dateTime().date.numberOfRepeatWeek(maxNumberOfWeek)

        val baseSchedule = baseScheduleRepository.fetchScheduleByDate(date, currentNumberOfWeek, currentUser).first()
        val customSchedule = customScheduleRepository.fetchScheduleByDate(date, currentUser).first()

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
        const val NOTIFICATION_KEY_PREFIX = "END_CLASSES_REMINDER_"
        const val NOTIFICATION_WORK_TAG = "END_CLASSES_REMINDER"
    }
}