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

import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.serializer
import ru.aleshin.studyassistant.core.common.exceptions.FirebaseUserException
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.remote.datasources.StudyAssistantFirebase.UserData
import ru.aleshin.studyassistant.core.remote.models.settings.NotificationSettingsPojo

/**
 * @author Stanislav Aleshin on 24.04.2024.
 */
interface NotificationSettingsRemoteDataSource {

    suspend fun addOrUpdateSettings(settings: NotificationSettingsPojo, targetUser: UID)

    suspend fun fetchSettings(targetUser: UID): Flow<NotificationSettingsPojo>

    class Base(
        private val database: FirebaseFirestore
    ) : NotificationSettingsRemoteDataSource {
        override suspend fun addOrUpdateSettings(settings: NotificationSettingsPojo, targetUser: UID) {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.SETTINGS).document(UserData.NOTIFICATION_SETTINGS)

            return reference.set(settings)
        }

        override suspend fun fetchSettings(targetUser: UID): Flow<NotificationSettingsPojo> {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)
            val reference = userDataRoot.collection(UserData.SETTINGS).document(UserData.NOTIFICATION_SETTINGS)

            return reference.snapshots.map { snapshot ->
                val settings = snapshot.data(serializer<NotificationSettingsPojo?>()) ?: NotificationSettingsPojo.default()
                return@map settings
            }
        }
    }
}