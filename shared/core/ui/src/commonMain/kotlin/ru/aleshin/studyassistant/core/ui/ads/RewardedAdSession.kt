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

import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock

/**
 * @author Stanislav Aleshin on 17.08.2026.
 */
object RewardedAdSession {

    private val lock = reentrantLock()
    private var presentedKey: String? = null
    private var rewardedKey: String? = null

    fun markPresented(key: String) = lock.withLock {
        presentedKey = key
    }

    fun markRewarded(key: String) = lock.withLock {
        presentedKey = key
        rewardedKey = key
    }

    fun hasRewarded(key: String) = lock.withLock {
        rewardedKey == key
    }

    fun isPresented(key: String) = lock.withLock {
        presentedKey == key
    }

    fun clear(key: String) = lock.withLock {
        if (presentedKey == key) presentedKey = null
        if (rewardedKey == key) rewardedKey = null
    }
}
