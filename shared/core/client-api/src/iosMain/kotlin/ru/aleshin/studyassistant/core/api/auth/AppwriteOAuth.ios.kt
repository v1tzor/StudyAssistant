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

import platform.UIKit.UIApplication
import ru.aleshin.studyassistant.core.common.exceptions.AppwriteException
import ru.aleshin.studyassistant.core.common.platform.PlatformActivity
import kotlin.coroutines.suspendCoroutine

actual suspend fun launchOAuth2Url(
    platformActivity: PlatformActivity,
    authUrl: String,
    callbackScheme: String
): String = suspendCoroutine { cont ->
    val oauthVC = OAuthWebViewController(
        authUrl = authUrl,
        callbackScheme = callbackScheme,
        onResult = { callbackUrl, error ->
            when {
                error != null -> {
                    cont.resumeWith(Result.failure(AppwriteException(error)))
                }
                callbackUrl == null -> {
                    cont.resumeWith(Result.failure(AppwriteException("Invalid callback URL")))
                }
                else -> {
                    cont.resumeWith(Result.success(callbackUrl))
                }
            }
        }
    )
    val keyWindow = UIApplication.sharedApplication.keyWindow
    val rootVC = keyWindow?.rootViewController
    if (rootVC == null) {
        return@suspendCoroutine cont.resumeWith(Result.failure(AppwriteException("No root view controller found")))
    }
    rootVC.presentViewController(oauthVC, animated = true, completion = null)
}