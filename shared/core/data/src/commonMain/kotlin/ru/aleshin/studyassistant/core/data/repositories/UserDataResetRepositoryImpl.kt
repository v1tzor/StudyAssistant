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

package ru.aleshin.studyassistant.core.data.repositories

import ru.aleshin.studyassistant.core.database.datasource.settings.UserDataResetLocalDataSource
import ru.aleshin.studyassistant.core.domain.repositories.UserDataResetRepository

/**
 * @author Stanislav Aleshin on 17.08.2026.
 */
class UserDataResetRepositoryImpl(
    private val localDataSource: UserDataResetLocalDataSource,
) : UserDataResetRepository {

    override suspend fun deleteAllSchedules() {
        localDataSource.deleteAllSchedules()
    }

    override suspend fun deleteAllUserData() {
        localDataSource.deleteAllUserData()
    }
}
