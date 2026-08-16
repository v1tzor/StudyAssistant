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

package ru.aleshin.studyassistant.chat.impl.domain.tools.validation

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.format.DateTimeComponents.Formats
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonPrimitive
import ru.aleshin.studyassistant.core.common.extensions.endThisDay
import ru.aleshin.studyassistant.core.common.extensions.parseUsingOffset
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.domain.entities.tasks.TaskPriority
import ru.aleshin.studyassistant.core.ui.views.iso8601

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
internal interface AiToolArgumentsValidator {

    fun required(args: Map<String, String>, name: String): String?
    fun optional(args: Map<String, String>, name: String): String?
    fun instant(value: String?): Instant?
    fun date(value: String?): Instant?
    fun time(value: String?): LocalTime?
    fun priority(value: String?): TaskPriority?
    fun boolean(value: String?): Boolean?
    fun list(value: String?): List<String>
    fun range(from: String?, to: String?): TimeRange?

    class Base : AiToolArgumentsValidator {

        override fun required(args: Map<String, String>, name: String): String? {
            return args[name]?.trim()?.takeIf(String::isNotEmpty)
        }

        override fun optional(args: Map<String, String>, name: String): String? {
            return args[name]?.trim()?.takeIf(String::isNotEmpty)
        }

        override fun instant(value: String?): Instant? {
            val source = value?.trim()?.takeIf(String::isNotEmpty) ?: return null
            return runCatching {
                Instant.parseUsingOffset(source, Formats.iso8601())
            }.getOrNull()
        }

        override fun date(value: String?): Instant? {
            val source = value?.trim()?.takeIf(String::isNotEmpty) ?: return null
            return runCatching {
                LocalDate.parse(source).atStartOfDayIn(TimeZone.currentSystemDefault())
            }.getOrNull()
        }

        override fun time(value: String?): LocalTime? {
            val source = value?.trim()?.takeIf(String::isNotEmpty) ?: return null
            return runCatching { LocalTime.parse(source) }.getOrNull()
        }

        override fun priority(value: String?): TaskPriority? {
            val source = value?.trim()?.takeIf(String::isNotEmpty) ?: return TaskPriority.STANDARD
            return TaskPriority.entries.find { it.name.equals(source, ignoreCase = true) }
        }

        override fun boolean(value: String?): Boolean? = when (value?.trim()?.lowercase()) {
            "true" -> true
            "false" -> false
            else -> null
        }

        override fun list(value: String?): List<String> {
            val source = value?.trim()?.takeIf(String::isNotEmpty) ?: return emptyList()
            return runCatching {
                val json = Json.parseToJsonElement(source) as? JsonArray
                json?.map { it.jsonPrimitive.content }.orEmpty()
            }.getOrElse {
                source.split(',').map(String::trim).filter(String::isNotEmpty)
            }
        }

        override fun range(from: String?, to: String?): TimeRange? {
            val start = date(from) ?: return null
            val end = date(to) ?: return null
            if (end < start || end.toEpochMilliseconds() - start.toEpochMilliseconds() > MAX_PERIOD_MILLIS) {
                return null
            }
            return TimeRange(start.startThisDay(), end.endThisDay())
        }
    }

    companion object {
        private const val MILLIS_IN_DAY = 86_400_000L
        private const val MAX_PERIOD_MILLIS = 14L * MILLIS_IN_DAY
    }
}
