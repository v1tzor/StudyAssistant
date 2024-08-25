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

import androidx.work.ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE
import androidx.work.ExistingWorkPolicy.REPLACE
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.flow.first
import ru.aleshin.studyassistant.core.common.extensions.shiftDay
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.data.workers.EndClassesReminderWorker
import ru.aleshin.studyassistant.core.data.workers.EndClassesReminderWorker.Companion.NOTIFICATION_WORK_TAG
import ru.aleshin.studyassistant.core.data.workers.EndClassesReminderWorker.Companion.REPEAT_WORK_KEY
import ru.aleshin.studyassistant.core.data.workers.EndClassesReminderWorker.Companion.WORK_KEY
import ru.aleshin.studyassistant.core.domain.managers.EndClassesReminderManager
import ru.aleshin.studyassistant.core.domain.managers.WorkStatus
import java.util.concurrent.TimeUnit

/**
 * @author Stanislav Aleshin on 24.08.2024.
 */
actual class EndClassesReminderManagerImpl(
    private val workManager: WorkManager,
    private val dateManager: DateManager,
) : EndClassesReminderManager {

    override suspend fun fetchWorkStatus(): WorkStatus {
        val workInfo = workManager.getWorkInfosForUniqueWorkFlow(REPEAT_WORK_KEY).first()
        return workInfo.firstOrNull()?.state.mapToWorkStatus()
    }

    override fun startOrRetryReminderService() {
        val workRequest = OneTimeWorkRequestBuilder<EndClassesReminderWorker>().build()
        workManager.cancelAllWorkByTag(NOTIFICATION_WORK_TAG)
        workManager.enqueueUniqueWork(WORK_KEY, REPLACE, workRequest)

        val currentTime = dateManager.fetchCurrentInstant()
        val targetTime = currentTime.shiftDay(1).startThisDay()
        val delay = (targetTime - currentTime).inWholeMilliseconds

        val repeatWorkRequest = PeriodicWorkRequestBuilder<EndClassesReminderWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.DAYS,
        ).apply {
            setInitialDelay(delay, TimeUnit.MILLISECONDS)
        }.build()

        workManager.enqueueUniquePeriodicWork(REPEAT_WORK_KEY, CANCEL_AND_REENQUEUE, repeatWorkRequest)
    }

    override fun stopReminderService() {
        workManager.cancelUniqueWork(WORK_KEY)
        workManager.cancelUniqueWork(REPEAT_WORK_KEY)
        workManager.cancelAllWorkByTag(NOTIFICATION_WORK_TAG)
    }
}