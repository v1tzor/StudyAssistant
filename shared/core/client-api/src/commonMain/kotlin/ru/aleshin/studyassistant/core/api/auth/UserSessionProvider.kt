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

package ru.aleshin.studyassistant.core.api.auth

import ru.aleshin.studyassistant.core.common.functional.UID

/**
 * Allows you to get the current user's data from local storage
 * or from a remote source if the local data is not up to date.
 *
 * @author Stanislav Aleshin on 24.07.2025.
 */
interface UserSessionProvider {

    /**
     * Get current user id or null
     *
     * @return Current user [UID]
     */
    suspend fun getCurrentUserIdOrNull(): UID?

    /**
     * Get current user id
     *
     * @return Current user [UID]
     */
    suspend fun getCurrentUserId(): UID
}