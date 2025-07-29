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

import androidx.work.BackoffPolicy
import androidx.work.ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.extensions.setHoursAndMinutes
import ru.aleshin.studyassistant.core.common.functional.Constants
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.data.workers.HomeworksReminderWorker
import ru.aleshin.studyassistant.core.data.workers.HomeworksReminderWorker.Companion.WORK_KEY
import ru.aleshin.studyassistant.core.domain.managers.RepeatWorkStatus
import ru.aleshin.studyassistant.core.domain.managers.reminders.HomeworksReminderManager
import java.util.concurrent.TimeUnit

/**
 * @author Stanislav Aleshin on 22.08.2024.
 */
actual class HomeworksReminderManagerImpl(
    private val workManager: WorkManager,
    private val dateManager: DateManager,
) : HomeworksReminderManager {

    actual override suspend fun fetchWorkStatus(): RepeatWorkStatus {
        val workInfo = workManager.getWorkInfosForUniqueWorkFlow(WORK_KEY).first()
        return workInfo.firstOrNull()?.state.mapToWorkStatus()
    }

    actual override fun startOrRetryReminderService(time: Instant) {
        val currentTime = dateManager.fetchCurrentInstant()
        val delayDuration = currentTime.setHoursAndMinutes(time) - currentTime
        val delay = if (delayDuration.isPositive()) {
            delayDuration.inWholeMilliseconds
        } else {
            Constants.Date.MILLIS_IN_DAY + delayDuration.inWholeMilliseconds
        }

        val workRequest = PeriodicWorkRequestBuilder<HomeworksReminderWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.DAYS,
        ).apply {
            setInitialDelay(delay, TimeUnit.MILLISECONDS)
            setBackoffCriteria(BackoffPolicy.LINEAR, 30, TimeUnit.SECONDS)
        }.build()

        workManager.enqueueUniquePeriodicWork(WORK_KEY, CANCEL_AND_REENQUEUE, workRequest)
    }

    actual override fun stopReminderService() {
        workManager.cancelUniqueWork(WORK_KEY)
    }
}