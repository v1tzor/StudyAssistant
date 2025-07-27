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

import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.di.MainDirectDIAware
import ru.aleshin.studyassistant.core.domain.managers.reminders.HomeworksReminderManager
import ru.aleshin.studyassistant.core.domain.managers.reminders.RepeatWorkStatus

/**
 * @author Stanislav Aleshin on 22.08.2024.
 */
actual class HomeworksReminderManagerImpl : HomeworksReminderManager, MainDirectDIAware {

    actual override suspend fun fetchWorkStatus(): RepeatWorkStatus {
        return RepeatWorkStatus.CANCELED
    }

    actual override fun startOrRetryReminderService(time: Instant) {
        // TODO: In planned

        // val request = BGProcessingTaskRequest(IDENTIFIER)
        // request.earliestBeginDate = calculateNextExecutionTime(time.dateTime().hour, time.dateTime().minute)
        // BGTaskScheduler.sharedScheduler.submitTaskRequest(request, null)
    }

    actual override fun stopReminderService() {
        // TODO: In planned

        // BGTaskScheduler.sharedScheduler.cancelTaskRequestWithIdentifier(IDENTIFIER)
    }

    companion object {
        const val IDENTIFIER = "ru.aleshin.studyassistant.homeworksreminder"
    }
}