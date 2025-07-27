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

package ru.aleshin.studyassistant.core.remote.api.message

import ru.aleshin.studyassistant.core.remote.BuildKonfig

/**
 * @author Stanislav Aleshin on 07.08.2024.
 */
interface PushServiceAuthTokenProvider {

    suspend fun fetchAuthToken(): String

    suspend fun fetchProjectId(): String

    object None : PushServiceAuthTokenProvider {
        override suspend fun fetchAuthToken() = ""
        override suspend fun fetchProjectId() = ""
    }

    class Firebase(
        private val googleAuthTokenProvider: GoogleAuthTokenProvider,
    ) : PushServiceAuthTokenProvider {

        override suspend fun fetchAuthToken(): String {
            return checkNotNull(googleAuthTokenProvider.fetchAccessToken(MESSAGING_SCOPE)) {
                "Google auth token was null."
            }
        }

        override suspend fun fetchProjectId(): String {
            return BuildKonfig.FIREBASE_PROJECT_ID
        }

        companion object {
            const val MESSAGING_SCOPE = "https://www.googleapis.com/auth/firebase.messaging"
        }
    }

    class RuStore : PushServiceAuthTokenProvider {
        override suspend fun fetchAuthToken(): String {
            return BuildKonfig.RUSTORE_SERVICE_AUTH_TOKEN
        }

        override suspend fun fetchProjectId(): String {
            return BuildKonfig.RUSTORE_PROJECT_ID
        }
    }

    class Huawei(
        private val hmsAuthTokenProvider: HmsAuthTokenProvider,
    ) : PushServiceAuthTokenProvider {

        override suspend fun fetchAuthToken(): String {
            return checkNotNull(hmsAuthTokenProvider.fetchAccessToken()) {
                "Hms auth token was null."
            }
        }

        override suspend fun fetchProjectId(): String {
            return BuildKonfig.HMS_PROJECT_ID
        }
    }
}