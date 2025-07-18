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

package ru.aleshin.studyassistant.core.remote.datasources.settings

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.aleshin.studyassistant.core.common.exceptions.AppwriteUserException
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.remote.appwrite.databases.DatabaseService
import ru.aleshin.studyassistant.core.remote.appwrite.utils.Permission
import ru.aleshin.studyassistant.core.remote.datasources.StudyAssistantAppwrite.CalendarSettings
import ru.aleshin.studyassistant.core.remote.models.settings.CalendarSettingsPojo

/**
 * @author Stanislav Aleshin on 24.04.2024.
 */
interface CalendarSettingsRemoteDataSource {

    suspend fun addOrUpdateSettings(settings: CalendarSettingsPojo, targetUser: UID)
    suspend fun fetchSettings(targetUser: UID): Flow<CalendarSettingsPojo>
    suspend fun deleteSettings(targetUser: UID)

    class Base(
        private val database: DatabaseService,
    ) : CalendarSettingsRemoteDataSource {
        override suspend fun addOrUpdateSettings(settings: CalendarSettingsPojo, targetUser: UID) {
            if (targetUser.isEmpty()) throw AppwriteUserException()

            database.upsertDocument(
                databaseId = CalendarSettings.DATABASE_ID,
                collectionId = CalendarSettings.COLLECTION_ID,
                documentId = targetUser,
                data = settings,
                permissions = Permission.onlyUserData(targetUser),
                nestedType = CalendarSettingsPojo.serializer(),
            )
        }

        override suspend fun fetchSettings(targetUser: UID): Flow<CalendarSettingsPojo> {
            if (targetUser.isEmpty()) throw AppwriteUserException()

            val settingsFlow = database.getDocumentFlow(
                databaseId = CalendarSettings.DATABASE_ID,
                collectionId = CalendarSettings.COLLECTION_ID,
                documentId = targetUser,
                nestedType = CalendarSettingsPojo.serializer(),
            )

            return settingsFlow.map { it ?: CalendarSettingsPojo.default() }
        }

        override suspend fun deleteSettings(targetUser: UID) {
            if (targetUser.isEmpty()) throw AppwriteUserException()

            database.deleteDocument(
                databaseId = CalendarSettings.DATABASE_ID,
                collectionId = CalendarSettings.COLLECTION_ID,
                documentId = targetUser
            )
        }
    }
}