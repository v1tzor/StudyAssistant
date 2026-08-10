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

package ru.aleshin.studyassistant.core.data.datasources

/**
 * @author Stanislav Aleshin on 08.08.2026.
 */
internal class IosAiSecureDataSource(
    private val storage: IosSecureStorage,
) : AiSecureDataSource {

    override suspend fun fetchPersonalKey(): String? = storage.read(PERSONAL_KEY)

    override suspend fun savePersonalKey(apiKey: String) {
        storage.write(PERSONAL_KEY, apiKey.trim())
    }

    override suspend fun deletePersonalKey() {
        storage.delete(PERSONAL_KEY)
    }

    private companion object {
        const val PERSONAL_KEY = "personal_key"
    }
}
