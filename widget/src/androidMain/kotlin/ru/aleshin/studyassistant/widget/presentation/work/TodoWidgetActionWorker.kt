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
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import org.kodein.di.DirectDIAware
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.common.functional.rightOrNull
import ru.aleshin.studyassistant.widget.di.WidgetWorkerDependencies
import ru.aleshin.studyassistant.widget.domain.interactors.WidgetInteractor

/**
 * @author Stanislav Aleshin on 10.08.2026.
 */
class TodoWidgetActionWorker(
    context: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(context, workerParameters), DirectDIAware {

    override val directDI = WidgetWorkerDependencies.create()
    private val widgetInteractor = instance<WidgetInteractor>()

    override suspend fun doWork(): Result {
        val todoId = inputData.getString(TODO_ID_KEY) ?: return Result.failure()
        val isDone = inputData.getBoolean(IS_DONE_KEY, true)
        val result = widgetInteractor.setTodoDone(todoId, isDone).rightOrNull()
        return if (result != null) {
            WidgetsUpdateScheduler.enqueueImmediate(applicationContext)
            Result.success()
        } else if (runAttemptCount < MAX_RETRY_COUNT) {
            Result.retry()
        } else {
            Result.failure()
        }
    }

    companion object {
        fun enqueue(context: Context, todoId: String, isDone: Boolean) {
            val data = Data.Builder()
                .putString(TODO_ID_KEY, todoId)
                .putBoolean(IS_DONE_KEY, isDone)
                .build()
            val request = OneTimeWorkRequestBuilder<TodoWidgetActionWorker>()
                .setInputData(data)
                .build()
            WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
                "$WORK_NAME_PREFIX:${todoId.hashCode()}",
                ExistingWorkPolicy.REPLACE,
                request,
            )
        }

        private const val TODO_ID_KEY = "todo_id"
        private const val IS_DONE_KEY = "is_done"
        private const val WORK_NAME_PREFIX = "STUDY_WIDGET_TODO_ACTION"
        private const val MAX_RETRY_COUNT = 3
    }
}
