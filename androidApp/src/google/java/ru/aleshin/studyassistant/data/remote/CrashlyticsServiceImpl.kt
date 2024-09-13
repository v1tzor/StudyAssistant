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

package ru.aleshin.studyassistant.data.remote

import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import ru.aleshin.studyassistant.core.common.inject.CrashlyticsService

/**
 * @author Stanislav Aleshin on 11.09.2024.
 */
class CrashlyticsServiceImpl : CrashlyticsService {

    private val firebaseCrashlytics: FirebaseCrashlytics by lazy { Firebase.crashlytics }

    override fun recordException(tag: String, message: String, exception: Throwable) {
        firebaseCrashlytics.run {
            setCustomKey(CRASHLYTICS_KEY_TAG, tag)
            setCustomKey(CRASHLYTICS_KEY_MESSAGE, message)
            recordException(exception)
        }
    }

    override fun initializeService() = Unit

    companion object {
        private const val CRASHLYTICS_KEY_TAG = "tag"
        private const val CRASHLYTICS_KEY_MESSAGE = "message"
    }
}