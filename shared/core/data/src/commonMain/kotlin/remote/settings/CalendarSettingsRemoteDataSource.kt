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

package remote.settings

import dev.gitlive.firebase.firestore.FirebaseFirestore
import exceptions.FirebaseUserException
import functional.UID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.serializer
import mappers.organizations.mapToDetailsData
import mappers.organizations.mapToRemoteData
import mappers.settings.mapToDetailsData
import mappers.settings.mapToRemoteData
import models.settings.CalendarSettingsDetailsData
import models.settings.CalendarSettingsPojo
import remote.StudyAssistantFirestore.UserData

/**
 * @author Stanislav Aleshin on 24.04.2024.
 */
interface CalendarSettingsRemoteDataSource {

    fun fetchSettings(targetUser: UID): Flow<CalendarSettingsDetailsData>

    suspend fun addOrUpdateSettings(settings: CalendarSettingsDetailsData, targetUser: UID)

    class Base(
        private val database: FirebaseFirestore
    ) : CalendarSettingsRemoteDataSource {
        override fun fetchSettings(targetUser: UID): Flow<CalendarSettingsDetailsData> {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)
            val reference = userDataRoot.collection(UserData.SETTINGS).document(UserData.CALENDAR_SETTINGS)
            return reference.snapshots.map { snapshot ->
                val settings = snapshot.data(serializer<CalendarSettingsPojo?>()) ?: CalendarSettingsPojo.default()
                return@map settings.mapToDetailsData()
            }
        }

        override suspend fun addOrUpdateSettings(settings: CalendarSettingsDetailsData, targetUser: UID) {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)
            val reference = userDataRoot.collection(UserData.SETTINGS).document(UserData.CALENDAR_SETTINGS)
            reference.set(settings.mapToRemoteData(), merge = true)
        }
    }
}
