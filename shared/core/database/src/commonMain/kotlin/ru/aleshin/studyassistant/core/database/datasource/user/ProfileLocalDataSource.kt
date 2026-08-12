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

package ru.aleshin.studyassistant.core.database.datasource.user

import kotlinx.coroutines.flow.Flow
import ru.aleshin.studyassistant.core.common.extensions.mapToOneOrNullFlow
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.database.mappers.user.mapToEntity
import ru.aleshin.studyassistant.core.database.mappers.user.mapToLocalData
import ru.aleshin.studyassistant.core.database.models.users.ProfileEntity
import ru.aleshin.studyassistant.sqldelight.user.ProfileQueries
import kotlin.coroutines.CoroutineContext

interface ProfileLocalDataSource {
    suspend fun addOrUpdateProfile(profile: ProfileEntity)
    suspend fun fetchProfile(): Flow<ProfileEntity?>

    class Base(
        private val profileQueries: ProfileQueries,
        private val coroutineManager: CoroutineManager,
    ) : ProfileLocalDataSource {

        private val coroutineContext: CoroutineContext
            get() = coroutineManager.ioDispatcher

        override suspend fun addOrUpdateProfile(profile: ProfileEntity) {
            profileQueries.addOrUpdateProfile(profile.mapToEntity()).await()
        }

        override suspend fun fetchProfile(): Flow<ProfileEntity?> {
            return profileQueries.fetchProfile().mapToOneOrNullFlow(coroutineContext) { profile ->
                profile.mapToLocalData()
            }
        }
    }
}
