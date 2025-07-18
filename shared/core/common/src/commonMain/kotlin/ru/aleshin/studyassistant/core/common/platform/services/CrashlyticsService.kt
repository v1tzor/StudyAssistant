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

package ru.aleshin.studyassistant.core.common.platform.services

import ru.aleshin.studyassistant.core.common.functional.UID

/**
 * @author Stanislav Aleshin on 11.09.2024.
 */
interface CrashlyticsService {

    fun sendLog(message: String)

    fun recordException(tag: String, message: String, exception: Throwable)

    fun initializeService()

    fun setupUser(id: UID?)

    class Empty : CrashlyticsService {
        override fun sendLog(message: String) = Unit
        override fun recordException(tag: String, message: String, exception: Throwable) = Unit
        override fun initializeService() = Unit
        override fun setupUser(id: UID?) = Unit
    }
}