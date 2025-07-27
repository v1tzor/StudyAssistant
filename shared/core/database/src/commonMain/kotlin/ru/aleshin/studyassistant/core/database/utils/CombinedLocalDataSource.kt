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

package ru.aleshin.studyassistant.core.database.utils

import ru.aleshin.studyassistant.core.database.utils.LocalDataSource.FullSynced
import ru.aleshin.studyassistant.core.database.utils.LocalDataSource.OnlyOffline

/**
 * Interface that provides access to both offline-only and synced variants of a local data source.
 *
 * Used in repositories or sync managers that need to operate differently depending on user type:
 * - Free users work in offline mode only.
 * - Premium (or authenticated) users require remote synchronization.
 *
 * This abstraction allows dynamic switching between offline and syncable data layers.
 *
 * @param T The base type of the local entity (must inherit from [BaseLocalEntity]).
 * @param O The implementation of the [OnlyOffline] local data source.
 * @param S The implementation of the [FullSynced] local data source.
 *
 * @author Stanislav Aleshin on 20.07.2025.
 */
interface CombinedLocalDataSource<T : BaseLocalEntity, O : OnlyOffline, S : FullSynced<T>> {

    /**
     * Returns the local data source used in offline-only mode.
     *
     * This version of the local storage is not synchronized with a remote server
     * and is typically used for users without network access or premium features.
     */
    fun offline(): O

    /**
     * Returns the local data source that is synchronized with a remote backend.
     *
     * This version supports bidirectional sync, offline queueing, and real-time updates.
     */
    fun sync(): S
}