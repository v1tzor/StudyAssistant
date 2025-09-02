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

package ru.aleshin.studyassistant.core.domain.utils.sync

import ru.aleshin.studyassistant.core.domain.entities.sync.SourceSyncKey

/**
 * Custom SyncManager
 *
 * Interface representing a synchronization manager responsible for syncing local and remote data.
 *
 * Used when the app supports offline changes which are later synchronized with a remote database
 * once the device regains internet connectivity.
 *
 * @author Stanislav Aleshin on 22.07.2025.
 */
interface SourceSyncManager {

    /**
     * The key for classifying changes in the database by data source
     */
    val sourceSyncKey: SourceSyncKey

    /**
     * Starts two-directional synchronization between local and remote sources.
     * Local changes are pushed first, then local data is updated with remote state,
     * and finally live remote updates are collected and applied.
     */
    suspend fun startBackgroundSync()

    /**
     * Single two-directional synchronization between local and remote sources.
     * Local changes are pushed first, then local data is updated with remote state
     *
     * @return [Boolean] Success sync status
     */
    suspend fun singleSyncRound(): Boolean

    /**
     * Stops all currently running sync operations.
     */
    fun stopSourceSync()

    /**
     * Deletes all offline changes and the user's local cache
     */
    suspend fun clearSourceData()
}