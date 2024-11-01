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

package ru.aleshin.studyassistant.core.common.functional

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings.Secure
import ru.aleshin.studyassistant.core.common.extensions.fetchCurrentLanguage
import ru.aleshin.studyassistant.core.common.platform.Platform

/**
 * @author Stanislav Aleshin on 07.09.2024.
 */
actual class DeviceInfoProvider(
    private val applicationContext: Context,
) {

    actual fun fetchDevicePlatform(): Platform {
        return Platform.Android
    }

    actual fun fetchDeviceName(): String {
        return buildString {
            append(Build.MANUFACTURER.capitalize(), " ")
            append(Build.MODEL)
        }
    }

    @SuppressLint("HardwareIds")
    actual fun fetchDeviceId(): String {
        return Secure.getString(applicationContext.contentResolver, Secure.ANDROID_ID)
    }

    actual fun fetchDeviceLanguage(): String {
        return applicationContext.fetchCurrentLanguage()
    }
}