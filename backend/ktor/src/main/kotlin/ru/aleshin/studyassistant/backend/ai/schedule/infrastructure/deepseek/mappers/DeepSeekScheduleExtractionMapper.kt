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
import ru.aleshin.studyassistant.backend.ai.schedule.infrastructure.deepseek.dto.DeepSeekScheduleDraftEntryDto
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
            DeepSeekJson.decodeFromString<DeepSeekScheduleDraftDto>(unwrapJson(content))
        }.getOrNull() ?: return null

        if (
            response.entries.size > config.maxScheduleEntries ||
            response.title.isOversized()
        ) {
            return null
        }

        val title = response.title.normalizedField()
        val entries = response.entries.mapNotNull { entry ->
            mapEntry(entry = entry, numberOfWeeks = numberOfWeeks)
        }

        return ScheduleDraft(
            title = title,
            entries = entries,
        )
    }

    private fun mapEntry(
        entry: DeepSeekScheduleDraftEntryDto,
        numberOfWeeks: Int,
    ): ScheduleDraftEntry? {
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
        if (entry.repeatWeek !in 1..numberOfWeeks || entry.dayOfWeek !in 1..DAYS_IN_WEEK) {
            return null
        }

        val clocks = parseClocks(startTime = entry.startTime, endTime = entry.endTime) ?: return null
        val classNumber = entry.classNumber?.takeIf { number -> number in 1..MAX_CLASS_NUMBER }

        return ScheduleDraftEntry(
            repeatWeek = entry.repeatWeek,
            dayOfWeek = entry.dayOfWeek,
            classNumber = classNumber,
            startTime = clocks.first,
            endTime = clocks.second,
            subject = entry.subject.normalizedField(),
            eventType = entry.eventType
                ?.trim()
                ?.replace('-', '_')
                ?.replace(' ', '_')
                ?.uppercase()
                ?.let { value -> ScheduleEventType.entries.find { type -> type.name == value } },
            teacher = entry.teacher.normalizedField(),
            office = entry.office.normalizedField(),
            location = entry.location.normalizedField(),
        )
    }

    private fun parseClocks(
        startTime: String?,
        endTime: String?,
    ): Pair<String?, String?>? {
        val rawStart = startTime.normalizedField()
        val rawEnd = endTime.normalizedField()
        val range = rawStart?.let(::parseTimeRange) ?: rawEnd?.let(::parseTimeRange)
        val startClock = rawStart?.let(::parseClock) ?: range?.first
        val endClock = rawEnd?.let(::parseClock) ?: range?.second
        if (rawStart != null && startClock == null) return null
        if (rawEnd != null && endClock == null) return null
        if (startClock != null && endClock != null && LocalTime.parse(endClock) <= LocalTime.parse(startClock)) {
            return null
        }
        return startClock to endClock
    }

    private fun parseTimeRange(value: String): Pair<String, String>? {
        val match = TIME_RANGE_PATTERN.matchEntire(value.trim()) ?: return null
        val start = parseClock(match.groupValues[1]) ?: return null
        val end = parseClock(match.groupValues[2]) ?: return null
        return start to end
    }

    private fun parseClock(value: String): String? {
        val parsed = parseLocalTime(value) ?: return null
        return String.format("%02d:%02d", parsed.hour, parsed.minute)
    }

    private fun unwrapJson(content: String): String {
        val trimmed = content.trim()
        if (!trimmed.startsWith("```")) return trimmed
        return trimmed
            .removePrefix("```json")
            .removePrefix("```JSON")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
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
        val TIME_RANGE_PATTERN = Regex("""^(\d{1,2}[:.]\d{2})\s*[-–—]\s*(\d{1,2}[:.]\d{2})$""")
    }
}
