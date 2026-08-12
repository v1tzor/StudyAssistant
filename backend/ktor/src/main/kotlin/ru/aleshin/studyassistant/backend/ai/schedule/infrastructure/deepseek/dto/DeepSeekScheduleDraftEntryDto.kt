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

package ru.aleshin.studyassistant.backend.ai.schedule.infrastructure.deepseek.dto

import kotlinx.serialization.Serializable

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
@Serializable
data class DeepSeekScheduleDraftEntryDto(
    val repeatWeek: Int,
    val dayOfWeek: Int,
    val classNumber: Int? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val subject: String? = null,
    val eventType: String? = null,
    val teacher: String? = null,
    val office: String? = null,
    val location: String? = null,
    val organization: String? = null,
    val notes: String? = null,
)
