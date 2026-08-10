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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.aleshin.studyassistant.core.data.datasources.AvatarLocalDataSource
import ru.aleshin.studyassistant.core.data.datasources.AvatarType
import ru.aleshin.studyassistant.core.data.mappers.users.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.users.mapToLocalData
import ru.aleshin.studyassistant.core.database.datasource.user.ProfileLocalDataSource
import ru.aleshin.studyassistant.core.domain.entities.files.InputFile
import ru.aleshin.studyassistant.core.domain.entities.users.Profile
import ru.aleshin.studyassistant.core.domain.repositories.ProfileRepository

class ProfileRepositoryImpl(
    private val localDataSource: ProfileLocalDataSource,
    private val avatarLocalDataSource: AvatarLocalDataSource,
) : ProfileRepository {

    override suspend fun fetchProfile(): Flow<Profile?> {
        return localDataSource.fetchProfile().map { profile -> profile?.mapToDomain() }
    }

    override suspend fun updateProfile(profile: Profile) {
        localDataSource.addOrUpdateProfile(profile.mapToLocalData())
    }

    override suspend fun uploadAvatar(oldAvatar: String?, file: InputFile): String {
        val avatar = avatarLocalDataSource.saveAvatar(AvatarType.PROFILE, file)
        if (oldAvatar != null && oldAvatar != avatar) avatarLocalDataSource.deleteAvatar(oldAvatar)
        return avatar
    }

    override suspend fun deleteAvatar(avatar: String) {
        avatarLocalDataSource.deleteAvatar(avatar)
    }

}
