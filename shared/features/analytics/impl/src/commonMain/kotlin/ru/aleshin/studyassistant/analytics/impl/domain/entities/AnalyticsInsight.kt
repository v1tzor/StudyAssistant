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

package ru.aleshin.studyassistant.analytics.impl.domain.entities

import kotlinx.datetime.Instant

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
internal data class AnalyticsInsight(
    val type: Type,
    val value: Float,
    val date: Instant? = null,
    val name: String? = null,
) {

    enum class Type {
        PEAK_LOAD,
        OVERLOAD_DAYS,
        LATE_COMPLETION_SHARE,
        ORGANIZATION_CONCENTRATION,
        SUBJECT_CONCENTRATION,
    }
}
