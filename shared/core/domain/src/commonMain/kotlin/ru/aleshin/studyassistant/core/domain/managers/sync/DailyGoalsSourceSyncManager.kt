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

package ru.aleshin.studyassistant.core.domain.managers.sync

import ru.aleshin.studyassistant.core.domain.entities.sync.SourceSyncKey
import ru.aleshin.studyassistant.core.domain.utils.sync.SourceSyncManager

/**
 * @author Stanislav Aleshin on 23.07.2025.
 */
interface DailyGoalsSourceSyncManager : SourceSyncManager {

    override val sourceSyncKey: SourceSyncKey get() = GOAL_SOURCE_KEY

    companion object Companion {
        const val GOAL_SOURCE_KEY = "GOAL"
    }
}