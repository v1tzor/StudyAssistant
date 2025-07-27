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

import androidx.work.WorkInfo
import ru.aleshin.studyassistant.core.domain.managers.reminders.RepeatWorkStatus

/**
 * @author Stanislav Aleshin on 25.08.2024.
 */
fun WorkInfo.State?.mapToWorkStatus() = when (this) {
    WorkInfo.State.ENQUEUED -> RepeatWorkStatus.ENQUEUED
    WorkInfo.State.RUNNING -> RepeatWorkStatus.RUNNING
    WorkInfo.State.SUCCEEDED -> RepeatWorkStatus.CANCELED
    WorkInfo.State.FAILED -> RepeatWorkStatus.CANCELED
    WorkInfo.State.BLOCKED -> RepeatWorkStatus.CANCELED
    WorkInfo.State.CANCELLED -> RepeatWorkStatus.CANCELED
    null -> RepeatWorkStatus.NOT_PLANNED
}