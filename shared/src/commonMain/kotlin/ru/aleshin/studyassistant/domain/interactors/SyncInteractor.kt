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

package ru.aleshin.studyassistant.domain.interactors

import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.domain.managers.sync.SourceSyncFacade
import ru.aleshin.studyassistant.core.domain.managers.sync.SyncWorkManager
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.domain.common.MainEitherWrapper
import ru.aleshin.studyassistant.domain.entities.MainFailures

/**
 * @author Stanislav Aleshin on 19.07.2025.
 */
interface SyncInteractor {

    suspend fun performSourceSync(): UnitDomainResult<MainFailures>

    class Base(
        private val sourceSyncFacade: SourceSyncFacade,
        private val syncWorkManager: SyncWorkManager,
        private val usersRepository: UsersRepository,
        private val eitherWrapper: MainEitherWrapper,
    ) : SyncInteractor {

        @OptIn(ExperimentalCoroutinesApi::class)
        override suspend fun performSourceSync() = eitherWrapper.wrap {
            if (usersRepository.fetchCurrentAuthUser() != null) {
                sourceSyncFacade.syncAllSource()
            } else {
                sourceSyncFacade.clearAllSyncedData()
            }
            syncWorkManager.startOrRetrySyncService()
        }
    }
}