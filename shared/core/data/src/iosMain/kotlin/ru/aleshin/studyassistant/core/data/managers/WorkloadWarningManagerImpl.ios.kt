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

import ru.aleshin.studyassistant.core.domain.managers.RepeatWorkStatus
import ru.aleshin.studyassistant.core.domain.managers.WorkloadWarningManager

/**
 * @author Stanislav Aleshin on 24.08.2024.
 */
actual class WorkloadWarningManagerImpl : WorkloadWarningManager {

    override suspend fun fetchWorkStatus(): RepeatWorkStatus {
        return RepeatWorkStatus.CANCELED
    }

    override fun startOrRetryWarningService() {
        // TODO: In planned
    }

    override fun stopWarningService() {
        // TODO: In planned
    }
}