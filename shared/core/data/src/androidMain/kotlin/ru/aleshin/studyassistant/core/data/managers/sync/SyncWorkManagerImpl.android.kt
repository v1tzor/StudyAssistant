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

package ru.aleshin.studyassistant.core.data.managers.sync

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy.KEEP
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.flow.first
import ru.aleshin.studyassistant.core.data.managers.reminders.mapToWorkStatus
import ru.aleshin.studyassistant.core.data.workers.SyncDataWorker
import ru.aleshin.studyassistant.core.data.workers.SyncDataWorker.Companion.WORK_KEY
import ru.aleshin.studyassistant.core.domain.managers.RepeatWorkStatus
import ru.aleshin.studyassistant.core.domain.managers.sync.SyncWorkManager
import java.util.concurrent.TimeUnit

/**
 * @author Stanislav Aleshin on 02.08.2025.
 */
actual class SyncWorkManagerImpl(
    private val workManager: WorkManager,
) : SyncWorkManager {

    actual override suspend fun fetchWorkStatus(): RepeatWorkStatus {
        val workInfo = workManager.getWorkInfosForUniqueWorkFlow(WORK_KEY).first()
        return workInfo.firstOrNull()?.state.mapToWorkStatus()
    }

    actual override fun startOrRetrySyncService() {
        val constraints = Constraints.Builder().apply {
            setRequiredNetworkType(NetworkType.CONNECTED)
            setRequiresBatteryNotLow(true)
        }.build()

        val workRequest = PeriodicWorkRequestBuilder<SyncDataWorker>(
            repeatInterval = SYNC_REPEAT_INTERVAL_IN_HOUR,
            repeatIntervalTimeUnit = TimeUnit.HOURS,
        ).apply {
            setConstraints(constraints)
            setInitialDelay(SYNC_START_DELAY_IN_MINUTES, TimeUnit.MINUTES)
            setBackoffCriteria(BackoffPolicy.LINEAR, RETRY_INTERVAL_IN_MINUTES, TimeUnit.MINUTES)
        }.build()

        workManager.enqueueUniquePeriodicWork(WORK_KEY, KEEP, workRequest)
    }

    actual override fun stopSyncService() {
        workManager.cancelUniqueWork(WORK_KEY)
    }

    companion object {
        const val SYNC_REPEAT_INTERVAL_IN_HOUR = 1L
        const val SYNC_START_DELAY_IN_MINUTES = 30L
        const val RETRY_INTERVAL_IN_MINUTES = 5L
    }
}