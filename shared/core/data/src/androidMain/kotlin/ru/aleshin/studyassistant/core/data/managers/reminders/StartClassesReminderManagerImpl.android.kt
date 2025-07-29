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

package ru.aleshin.studyassistant.core.data.managers.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_CANCEL_CURRENT
import android.app.PendingIntent.FLAG_MUTABLE
import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE
import androidx.work.ExistingWorkPolicy.REPLACE
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.flow.first
import ru.aleshin.studyassistant.core.common.extensions.shiftDay
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.common.messages.LocalNotificationReceiver
import ru.aleshin.studyassistant.core.data.workers.StartClassesReminderWorker
import ru.aleshin.studyassistant.core.data.workers.StartClassesReminderWorker.Companion.NOTIFICATION_ID_APPEND
import ru.aleshin.studyassistant.core.data.workers.StartClassesReminderWorker.Companion.REPEAT_WORK_KEY
import ru.aleshin.studyassistant.core.data.workers.StartClassesReminderWorker.Companion.WORK_KEY
import ru.aleshin.studyassistant.core.domain.managers.RepeatWorkStatus
import ru.aleshin.studyassistant.core.domain.managers.reminders.StartClassesReminderManager
import java.util.concurrent.TimeUnit

/**
 * @author Stanislav Aleshin on 24.08.2024.
 */
actual class StartClassesReminderManagerImpl(
    private val context: Context,
    private val alarmManager: AlarmManager,
    private val workManager: WorkManager,
    private val dateManager: DateManager,
) : StartClassesReminderManager {

    actual override suspend fun fetchWorkStatus(): RepeatWorkStatus {
        val workInfo = workManager.getWorkInfosForUniqueWorkFlow(REPEAT_WORK_KEY).first()
        return workInfo.firstOrNull()?.state.mapToWorkStatus()
    }

    actual override fun startOrRetryReminderService() {
        val workRequest = OneTimeWorkRequestBuilder<StartClassesReminderWorker>().build()
        workManager.enqueueUniqueWork(WORK_KEY, REPLACE, workRequest)

        val currentTime = dateManager.fetchCurrentInstant()
        val targetTime = currentTime.shiftDay(1).startThisDay()
        val delay = (targetTime - currentTime).inWholeMilliseconds

        val repeatWorkRequest = PeriodicWorkRequestBuilder<StartClassesReminderWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.DAYS,
        ).apply {
            setInitialDelay(delay, TimeUnit.MILLISECONDS)
            setBackoffCriteria(BackoffPolicy.LINEAR, 30, TimeUnit.SECONDS)
        }.build()

        workManager.enqueueUniquePeriodicWork(REPEAT_WORK_KEY, CANCEL_AND_REENQUEUE, repeatWorkRequest)
    }

    actual override fun stopReminderService(allOrganizations: List<UID>) {
        workManager.cancelUniqueWork(WORK_KEY)
        workManager.cancelUniqueWork(REPEAT_WORK_KEY)
        allOrganizations.forEach { uid ->
            val id = uid.hashCode() + NOTIFICATION_ID_APPEND
            val intent = LocalNotificationReceiver.createCancelIntent(context)
            val cancelFlag = FLAG_CANCEL_CURRENT or FLAG_MUTABLE
            val cancelPendingIntent = PendingIntent.getBroadcast(context, id, intent, cancelFlag)
            if (cancelPendingIntent != null) {
                alarmManager.cancel(cancelPendingIntent)
                cancelPendingIntent.cancel()
            }
        }
    }
}