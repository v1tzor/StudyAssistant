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
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.common.di.MainDirectDIAware
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.domain.managers.reminders.EndClassesReminderManager
import ru.aleshin.studyassistant.core.domain.managers.reminders.HomeworksReminderManager
import ru.aleshin.studyassistant.core.domain.managers.reminders.StartClassesReminderManager
import ru.aleshin.studyassistant.core.domain.managers.reminders.WorkloadWarningManager
import ru.aleshin.studyassistant.core.domain.repositories.NotificationSettingsRepository

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
class NotificationRescheduleWorker(
    context: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(context, workerParameters), MainDirectDIAware {

    private val notificationSettingsRepository = instance<NotificationSettingsRepository>()
    private val startClassesReminderManager = instance<StartClassesReminderManager>()
    private val endClassesReminderManager = instance<EndClassesReminderManager>()
    private val homeworksReminderManager = instance<HomeworksReminderManager>()
    private val workloadWarningManager = instance<WorkloadWarningManager>()
    private val dateManager = instance<DateManager>()

    override suspend fun doWork(): Result = try {
        val settings = notificationSettingsRepository.fetchSettings().first()
        startClassesReminderManager.startOrRetryReminderService()
        if (settings.endOfClasses) {
            endClassesReminderManager.startOrRetryReminderService()
        } else {
            endClassesReminderManager.stopReminderService(emptyList())
        }
        settings.unfinishedHomeworks?.let { time ->
            val currentTime = dateManager.fetchCurrentInstant()
            val targetDateTime = LocalDateTime(
                date = currentTime.dateTime().date,
                time = LocalTime.fromMillisecondOfDay(time.toInt()),
            )
            homeworksReminderManager.startOrRetryReminderService(
                targetDateTime.toInstant(TimeZone.currentSystemDefault())
            )
        } ?: homeworksReminderManager.stopReminderService()
        if (settings.highWorkload != null) {
            workloadWarningManager.startOrRetryWarningService()
        } else {
            workloadWarningManager.stopWarningService()
        }
        Result.success()
    } catch (_: Exception) {
        Result.retry()
    }

    companion object {
        const val WORK_KEY = "NOTIFICATION_RESCHEDULE"
    }
}
