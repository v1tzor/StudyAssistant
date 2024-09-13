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

package ru.aleshin.studyassistant.core.remote.datasources.message

import android.content.Context
import com.google.auth.oauth2.GoogleCredentials

/**
 * @author Stanislav Aleshin on 07.08.2024.
 */
class GoogleAuthTokenProviderImpl(
    private val applicationContext: Context,
) : GoogleAuthTokenProvider {

    override suspend fun fetchAccessToken(scope: String): String? {
        val stream = applicationContext.assets.open(SERVICE_ACCOUNT_FILE)
        val credentials = GoogleCredentials.fromStream(stream).createScoped(scope)

        credentials.refresh()
        return credentials.accessToken.tokenValue
    }

    companion object {
        const val SERVICE_ACCOUNT_FILE = "service-account-file.json"
    }
}