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
import kotlinx.datetime.format.DateTimeComponents.Formats.ISO_DATE_TIME_OFFSET
import kotlinx.serialization.json.JsonElement
import ru.aleshin.studyassistant.core.api.AppwriteApi.CalendarSettings
import ru.aleshin.studyassistant.core.api.AppwriteApi.Common.UPDATED_AT
import ru.aleshin.studyassistant.core.api.auth.UserSessionProvider
import ru.aleshin.studyassistant.core.api.databases.DatabaseService
import ru.aleshin.studyassistant.core.api.realtime.RealtimeService
import ru.aleshin.studyassistant.core.api.utils.Permission
import ru.aleshin.studyassistant.core.common.architecture.data.MetadataModel
import ru.aleshin.studyassistant.core.common.extensions.getStringOrNull
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.remote.models.settings.CalendarSettingsPojo
import ru.aleshin.studyassistant.core.remote.utils.RemoteDataSource

/**
 * @author Stanislav Aleshin on 24.04.2024.
 */
interface CalendarSettingsRemoteDataSource : RemoteDataSource.FullSynced.SingleDocument<CalendarSettingsPojo> {

    class Base(
        database: DatabaseService,
        realtime: RealtimeService,
        userSessionProvider: UserSessionProvider
    ) : CalendarSettingsRemoteDataSource, RemoteDataSource.FullSynced.SingleDocument.BaseAppwrite<CalendarSettingsPojo>(
        database = database,
        realtime = realtime,
        userSessionProvider = userSessionProvider,
    ) {

        override val databaseId = CalendarSettings.DATABASE_ID

        override val collectionId = CalendarSettings.COLLECTION_ID

        override val nestedType = CalendarSettingsPojo.serializer()

        override fun permissions(currentUser: UID) = Permission.onlyUserData(currentUser)

        override suspend fun fetchItem(): Flow<CalendarSettingsPojo?> {
            val currentUser = userSessionProvider.getCurrentUserId()

            return database.getDocumentFlow(
                databaseId = databaseId,
                collectionId = collectionId,
                documentId = currentUser,
                nestedType = nestedType,
            ).map { item ->
                item?.data ?: CalendarSettingsPojo.default(currentUser)
            }
        }

        override suspend fun fetchMetadata(): MetadataModel? {
            val currentUser = userSessionProvider.getCurrentUserId()

            val document = database.getDocumentOrNull(
                databaseId = databaseId,
                collectionId = collectionId,
                documentId = currentUser,
                nestedType = JsonElement.serializer(),
            )

            return if (document != null) {
                val documentUpdatedAt = ISO_DATE_TIME_OFFSET.parse(document.updatedAt)
                val updatedAt = document.data.getStringOrNull(UPDATED_AT).let {
                    it?.toLongOrNull() ?: documentUpdatedAt.toInstantUsingOffset().toEpochMilliseconds()
                }
                MetadataModel(id = currentUser, updatedAt = updatedAt)
            } else {
                MetadataModel(id = currentUser, updatedAt = 0L)
            }
        }
    }
}