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
import androidx.work.Worker
import androidx.work.WorkerParameters
import ru.aleshin.studyassistant.core.common.functional.Constants
import ru.aleshin.studyassistant.core.common.notifications.NotificationCreator
import ru.aleshin.studyassistant.core.common.notifications.parameters.NotificationCategory
import ru.aleshin.studyassistant.core.common.notifications.parameters.NotificationPriority
import ru.aleshin.studyassistant.core.data.R
import ru.aleshin.studyassistant.core.data.managers.NotificationScheduler.Companion.BODY_KEY
import ru.aleshin.studyassistant.core.data.managers.NotificationScheduler.Companion.TITLE_KEY

/**
 * @author Stanislav Aleshin on 20.08.2024.
 */
class NotificationWorker(
    context: Context,
    workerParameters: WorkerParameters,
) : Worker(context, workerParameters) {

    private val notificationCreator by lazy {
        NotificationCreator.Base(applicationContext)
    }

    override fun doWork(): Result {
        val title = inputData.getString(TITLE_KEY)
        val body = inputData.getString(BODY_KEY)

        if (title != null && body != null) {
            val notify = notificationCreator.createNotify(
                channelId = Constants.Notification.CHANNEL_ID,
                title = title,
                text = body,
                priority = NotificationPriority.MAX,
                category = NotificationCategory.CATEGORY_REMINDER,
                smallIcon = R.drawable.ic_launcher_notification,
            )

            notificationCreator.showNotify(notify)
            return Result.success()
        } else {
            return Result.failure()
        }
    }
}