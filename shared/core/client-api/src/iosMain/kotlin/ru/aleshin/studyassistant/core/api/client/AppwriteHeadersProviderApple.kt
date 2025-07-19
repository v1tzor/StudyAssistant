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

import platform.Foundation.NSBundle
import platform.Foundation.NSProcessInfo
import platform.UIKit.UIDevice

class AppwriteHeadersProviderApple : AppwriteHeadersProvider {

    override fun fetchHeaders(): MutableMap<String, String> {
        return mutableMapOf(
            "content-type" to "application/json",
            "user-agent" to getUserAgent(),
            "origin" to "appwrite-${UIDevice.currentDevice.systemName}://${NSBundle.mainBundle.bundleIdentifier ?: ""}",
            "x-sdk-name" to "Apple",
            "x-sdk-platform" to "client",
            "x-sdk-language" to "apple",
            "x-sdk-version" to "8.1.0",
            "x-appwrite-response-format" to "1.7.0",
        )
    }

    private fun getUserAgent(): String {
        val systemName = UIDevice.currentDevice.systemName
        val systemVersion = UIDevice.currentDevice.systemVersion
        val model = UIDevice.currentDevice.model
        val osVersion = NSProcessInfo.processInfo.operatingSystemVersionString
        val bundle = NSBundle.mainBundle
        val version = bundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: ""
        val device = "$model; $systemName/$systemVersion ($osVersion)"
        return "${bundle.bundleIdentifier ?: ""}/$version $device"
    }
}