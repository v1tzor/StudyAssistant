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
import kotlinx.coroutines.flow.Flow
import models.settings.CalendarSettingsPojo

/**
 * @author Stanislav Aleshin on 24.04.2024.
 */
interface CalendarSettingsRemoteDataSource {

    fun fetchSettings(): Flow<CalendarSettingsPojo>

    suspend fun updateSettings(settings: CalendarSettingsPojo)

    class Base(
        private val database: FirebaseFirestore
    ) : CalendarSettingsRemoteDataSource {

        override fun fetchSettings(): Flow<CalendarSettingsPojo> {
            TODO()
        }

        override suspend fun updateSettings(settings: CalendarSettingsPojo) {
            TODO()
        }
    }
}
