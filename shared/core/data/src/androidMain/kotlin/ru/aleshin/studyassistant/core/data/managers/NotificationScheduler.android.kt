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

package ru.aleshin.studyassistant.core.data.managers

import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.data.workers.NotificationWorker
import java.util.concurrent.TimeUnit

/**
 * @author Stanislav Aleshin on 20.08.2024.
 */
actual class NotificationScheduler(
    private val workManager: WorkManager,
    private val dateManager: DateManager,
) {

    actual fun scheduleNotification(
        id: String,
        title: String,
        body: String,
        time: Instant
    ) {
        val currentTime = dateManager.fetchCurrentInstant()
        val delayDuration = time - currentTime
        val delay = if (delayDuration.isPositive()) delayDuration.inWholeMilliseconds else 0L

        val inputData = Data.Builder().apply {
            putString(TITLE_KEY, title)
            putString(BODY_KEY, body)
        }.build()

        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>().apply {
            setInitialDelay(delay, TimeUnit.MILLISECONDS)
            setInputData(inputData)
        }.build()

        workManager.enqueueUniqueWork(id, ExistingWorkPolicy.REPLACE, workRequest)
    }

    actual fun scheduleRepeatNotification(
        id: String,
        title: String,
        body: String,
        time: Instant,
        interval: Long
    ) {
        val currentTime = dateManager.fetchCurrentInstant()
        val delayDuration = time - currentTime
        val delay = if (delayDuration.isPositive()) delayDuration.inWholeMilliseconds else 0L

        val inputData = Data.Builder().apply {
            putString(TITLE_KEY, title)
            putString(BODY_KEY, body)
        }.build()

        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(interval, TimeUnit.MILLISECONDS).apply {
            setInitialDelay(delay, TimeUnit.MILLISECONDS)
            setInputData(inputData)
        }.build()

        workManager.enqueueUniquePeriodicWork(id, ExistingPeriodicWorkPolicy.UPDATE, workRequest)
    }

    actual fun cancelNotification(id: String) {
        workManager.cancelUniqueWork(id)
    }

    companion object {
        const val TITLE_KEY = "SCHEDULED_NOTIFICATION_TITLE"
        const val BODY_KEY = "SCHEDULED_NOTIFICATION_BODY"
    }
}