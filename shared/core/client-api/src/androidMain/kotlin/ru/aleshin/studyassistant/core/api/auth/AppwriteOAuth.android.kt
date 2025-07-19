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

import androidx.core.net.toUri
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import ru.aleshin.studyassistant.core.common.exceptions.AppwriteException
import ru.aleshin.studyassistant.core.common.platform.PlatformActivity

actual suspend fun launchOAuth2Url(
    platformActivity: PlatformActivity,
    authUrl: String,
    callbackScheme: String
): String = callbackFlow {
    WebAuthComponent.authenticate(
        activity = platformActivity,
        url = authUrl.toUri(),
        callbackUrlScheme = callbackScheme,
    ) { result ->
        val error = result.exceptionOrNull()?.message
        val callbackUrl = result.getOrNull()

        when {
            error != null -> {
                AppwriteException(error).printStackTrace()
                trySend("")
            }
            callbackUrl == null -> {
                AppwriteException("Invalid callback URL").printStackTrace()
                trySend("")
            }
            else -> {
                trySend(callbackUrl)
            }
        }
    }
}.first()