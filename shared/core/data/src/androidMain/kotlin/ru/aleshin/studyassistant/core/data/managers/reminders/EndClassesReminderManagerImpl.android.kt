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

import androidx.work.BackoffPolicy
import androidx.work.ExistingPeriodicWorkPolicy.UPDATE
import androidx.work.ExistingWorkPolicy.REPLACE
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import ru.aleshin.studyassistant.core.common.extensions.shiftDay
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.data.workers.EndClassesReminderWorker
import ru.aleshin.studyassistant.core.data.workers.EndClassesReminderWorker.Companion.NOTIFICATION_GROUP
import ru.aleshin.studyassistant.core.data.workers.EndClassesReminderWorker.Companion.REPEAT_WORK_KEY
import ru.aleshin.studyassistant.core.data.workers.EndClassesReminderWorker.Companion.WORK_KEY
import ru.aleshin.studyassistant.core.domain.managers.reminders.EndClassesReminderManager
import java.util.concurrent.TimeUnit

/**
 * @author Stanislav Aleshin on 24.08.2024.
 */
actual class EndClassesReminderManagerImpl(
    private val workManager: WorkManager,
    private val dateManager: DateManager,
    private val notificationScheduler: NotificationScheduler,
) : EndClassesReminderManager {

    actual override suspend fun startOrRetryReminderService() {
        val workRequest = OneTimeWorkRequestBuilder<EndClassesReminderWorker>().build()
        workManager.enqueueUniqueWork(WORK_KEY, REPLACE, workRequest)

        val currentTime = dateManager.fetchCurrentInstant()
        val targetTime = currentTime.shiftDay(1).startThisDay()
        val delay = (targetTime - currentTime).inWholeMilliseconds

        val repeatWorkRequest = PeriodicWorkRequestBuilder<EndClassesReminderWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.DAYS,
        ).apply {
            setInitialDelay(delay, TimeUnit.MILLISECONDS)
            setBackoffCriteria(BackoffPolicy.LINEAR, 30, TimeUnit.SECONDS)
        }.build()

        workManager.enqueueUniquePeriodicWork(
            REPEAT_WORK_KEY,
            UPDATE,
            repeatWorkRequest
        )
    }

    actual override suspend fun stopReminderService(allOrganizations: List<UID>) {
        workManager.cancelUniqueWork(WORK_KEY)
        workManager.cancelUniqueWork(REPEAT_WORK_KEY)
        notificationScheduler.clearNotificationGroup(NOTIFICATION_GROUP)
    }
}
