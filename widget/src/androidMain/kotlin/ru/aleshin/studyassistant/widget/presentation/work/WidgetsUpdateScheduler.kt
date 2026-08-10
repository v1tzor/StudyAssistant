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

package ru.aleshin.studyassistant.widget.presentation.work

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * @author Stanislav Aleshin on 10.08.2026.
 */
object WidgetsUpdateScheduler {

    fun enqueueImmediate(context: Context) {
        val workManager = WorkManager.getInstance(context.applicationContext)
        workManager.enqueueUniqueWork(
            IMMEDIATE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<WidgetsUpdateWorker>().build(),
        )
        workManager.enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<WidgetsUpdateWorker>(1, TimeUnit.HOURS).build(),
        )
    }

    fun scheduleBoundary(context: Context, boundary: Long) {
        val delay = (boundary - System.currentTimeMillis()).coerceAtLeast(MINIMUM_DELAY)
        WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
            BOUNDARY_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<WidgetsUpdateWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build(),
        )
    }

    fun cancelAll(context: Context) {
        val workManager = WorkManager.getInstance(context.applicationContext)
        workManager.cancelUniqueWork(PERIODIC_WORK_NAME)
        workManager.cancelUniqueWork(BOUNDARY_WORK_NAME)
    }

    private const val IMMEDIATE_WORK_NAME = "STUDY_WIDGETS_IMMEDIATE_UPDATE"
    private const val PERIODIC_WORK_NAME = "STUDY_WIDGETS_PERIODIC_UPDATE"
    private const val BOUNDARY_WORK_NAME = "STUDY_WIDGETS_BOUNDARY_UPDATE"
    private const val MINIMUM_DELAY = 1_000L
}
