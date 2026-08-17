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

package ru.aleshin.studyassistant.core.ui.ads

/**
 * @author Stanislav Aleshin on 17.08.2026.
 */
object RewardedAdSession {

    private val lock = Any()
    private var presentedKey: String? = null
    private var rewardedKey: String? = null

    fun markPresented(key: String) = synchronized(lock) {
        presentedKey = key
    }

    fun markRewarded(key: String) = synchronized(lock) {
        presentedKey = key
        rewardedKey = key
    }

    fun hasRewarded(key: String) = synchronized(lock) {
        rewardedKey == key
    }

    fun isPresented(key: String) = synchronized(lock) {
        presentedKey == key
    }

    fun clear(key: String) = synchronized(lock) {
        if (presentedKey == key) presentedKey = null
        if (rewardedKey == key) rewardedKey = null
    }
}
