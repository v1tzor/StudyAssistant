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

package ru.aleshin.studyassistant.core.api.client

import android.content.Context
import android.content.pm.PackageManager
import co.touchlab.kermit.Logger
import ru.aleshin.studyassistant.core.common.functional.Constants.App.LOGGER_TAG

class AppwriteHeadersProviderAndroid(
    private val context: Context,
) : AppwriteHeadersProvider {

    private val appVersion: String?
        get() {
            return try {
                val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                pInfo.versionName
            } catch (e: PackageManager.NameNotFoundException) {
                Logger.e(LOGGER_TAG, e) { e.stackTraceToString() }
                ""
            }
        }

    override fun fetchHeaders(): MutableMap<String, String> {
        return mutableMapOf(
            "content-type" to "application/json",
            "origin" to "appwrite-android://${context.packageName}",
            "user-agent" to "${context.packageName}/$appVersion, ${System.getProperty("http.agent")}",
            "x-sdk-name" to "Android",
            "x-sdk-platform" to "client",
            "x-sdk-language" to "android",
            "x-sdk-version" to "8.1.0",
            "x-appwrite-response-format" to "1.7.0",
        )
    }
}