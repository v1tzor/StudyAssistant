/*
 * Copyright 2023 Stanislav Aleshin
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
 * @author Stanislav Aleshin on 13.04.2025.
 */
interface AnalyticsService {

    fun trackEvent(name: String, eventParams: Map<String, String>)

    fun initializeService()

    fun setupUserId(id: UID) {}

    class Empty : AnalyticsService {
        override fun trackEvent(name: String, eventParams: Map<String, String>) = Unit
        override fun initializeService() = Unit
    }
}