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

package ru.aleshin.studyassistant.backend.ai.schedule.infrastructure.deepseek.mappers

import ru.aleshin.studyassistant.backend.ai.AiConfig
import ru.aleshin.studyassistant.backend.ai.infrastructure.deepseek.DeepSeekJson
import ru.aleshin.studyassistant.backend.ai.schedule.domain.model.ScheduleDraft
import ru.aleshin.studyassistant.backend.ai.schedule.domain.model.ScheduleDraftEntry
import ru.aleshin.studyassistant.backend.ai.schedule.domain.model.ScheduleEventType
import ru.aleshin.studyassistant.backend.ai.schedule.infrastructure.deepseek.dto.DeepSeekScheduleDraftDto
import java.time.LocalTime

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
class DeepSeekScheduleExtractionMapper(
    private val config: AiConfig,
) {

    fun mapResponse(
        content: String,
        numberOfWeeks: Int,
    ): ScheduleDraft? {
        val response = runCatching {
            DeepSeekJson.decodeFromString<DeepSeekScheduleDraftDto>(content)
        }.getOrNull() ?: return null

        if (
            response.entries.size > config.maxScheduleEntries ||
            response.title.isOversized()
        ) {
            return null
        }

        val title = response.title.normalizedField()
        val entries = response.entries.map { entry ->
            val fields = listOf(
                entry.startTime,
                entry.endTime,
                entry.subject,
                entry.eventType,
                entry.teacher,
                entry.office,
                entry.location,
            )
            if (fields.any { field -> field.isOversized() }) {
                return null
            }

            val startTime = entry.startTime.normalizedField()?.let { value ->
                parseClock(value) ?: return null
            }
            val endTime = entry.endTime.normalizedField()?.let { value ->
                parseClock(value) ?: return null
            }
            if (
                entry.repeatWeek !in 1..numberOfWeeks ||
                entry.dayOfWeek !in 1..DAYS_IN_WEEK ||
                entry.classNumber != null && entry.classNumber !in 1..MAX_CLASS_NUMBER ||
                startTime != null && endTime != null && LocalTime.parse(endTime) <= LocalTime.parse(startTime)
            ) {
                return null
            }

            ScheduleDraftEntry(
                repeatWeek = entry.repeatWeek,
                dayOfWeek = entry.dayOfWeek,
                classNumber = entry.classNumber,
                startTime = startTime,
                endTime = endTime,
                subject = entry.subject.normalizedField(),
                eventType = entry.eventType
                    ?.trim()
                    ?.replace('-', '_')
                    ?.replace(' ', '_')
                    ?.uppercase()
                    ?.let { value -> ScheduleEventType.entries.find { it.name == value } },
                teacher = entry.teacher.normalizedField(),
                office = entry.office.normalizedField(),
                location = entry.location.normalizedField(),
            )
        }

        return ScheduleDraft(
            title = title,
            entries = entries,
        )
    }

    private fun parseClock(value: String): String? {
        val parsed = parseLocalTime(value) ?: return null
        return String.format("%02d:%02d", parsed.hour, parsed.minute)
    }

    private fun parseLocalTime(value: String): LocalTime? {
        val normalized = value.trim().replace('.', ':')
        runCatching { LocalTime.parse(normalized) }.getOrNull()?.let { parsed ->
            return parsed
        }
        val parts = normalized.split(':')
        if (parts.size !in 2..3) return null
        val hour = parts[0].toIntOrNull() ?: return null
        val minute = parts[1].toIntOrNull() ?: return null
        val second = parts.getOrNull(2)?.toIntOrNull() ?: 0
        return runCatching { LocalTime.of(hour, minute, second) }.getOrNull()
    }

    private fun String?.normalizedField(): String? {
        return this?.trim()?.takeIf(String::isNotEmpty)
    }

    private fun String?.isOversized(): Boolean {
        return this != null && length > config.maxScheduleFieldCharacters
    }

    private companion object {

        const val DAYS_IN_WEEK = 7
        const val MAX_CLASS_NUMBER = 30
    }
}
