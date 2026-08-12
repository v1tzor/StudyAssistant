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

package ru.aleshin.studyassistant.core.remote.datasources.installation

import ru.aleshin.studyassistant.core.remote.api.installation.InstallationRemoteApi

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
interface InstallationRemoteDataSource {

    suspend fun register(): String

    class Base(
        private val api: InstallationRemoteApi,
    ) : InstallationRemoteDataSource {

        override suspend fun register(): String {
            return api.register().credential
        }
    }
}
