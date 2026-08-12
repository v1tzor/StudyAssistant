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

package ru.aleshin.studyassistant.schedule.impl.domain.validation

import kotlinx.datetime.LocalTime
import ru.aleshin.studyassistant.core.domain.entities.schedules.importing.ScheduleImportDraft
import ru.aleshin.studyassistant.core.domain.entities.schedules.importing.ScheduleImportEntry

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
internal interface ScheduleImportValidator {

    fun isSourceTextValid(text: String): Boolean
    fun isDraftValid(draft: ScheduleImportDraft): Boolean
    fun parseTime(value: String?): LocalTime?

    class Base : ScheduleImportValidator {

        override fun isSourceTextValid(text: String): Boolean {
            return text.trim().length in MIN_TEXT_LENGTH..MAX_TEXT_LENGTH
        }

        override fun isDraftValid(draft: ScheduleImportDraft): Boolean {
            val entries = draft.entries.filter(ScheduleImportEntry::included)
            if (entries.isEmpty() || entries.size > MAX_ENTRIES) return false

            return entries.all { entry ->
                val start = parseTime(entry.startTime)
                val end = parseTime(entry.endTime)
                entry.repeatWeek in MIN_REPEAT_WEEK..MAX_REPEAT_WEEK &&
                    entry.dayOfWeek in MIN_DAY_OF_WEEK..MAX_DAY_OF_WEEK &&
                    !entry.subject.isNullOrBlank() &&
                    start != null &&
                    end != null &&
                    start < end
            } && entries.distinctBy { entry ->
                listOf(
                    entry.repeatWeek.toString(),
                    entry.dayOfWeek.toString(),
                    entry.startTime,
                    entry.endTime,
                    entry.subject?.trim()?.lowercase(),
                )
            }.size == entries.size
        }

        override fun parseTime(value: String?): LocalTime? {
            return value?.trim()?.takeIf(String::isNotEmpty)?.let { time ->
                runCatching { LocalTime.parse(time) }.getOrNull()
            }
        }

        private companion object {
            const val MIN_TEXT_LENGTH = 3
            const val MAX_TEXT_LENGTH = 30_000
            const val MAX_ENTRIES = 150
            const val MIN_REPEAT_WEEK = 1
            const val MAX_REPEAT_WEEK = 3
            const val MIN_DAY_OF_WEEK = 1
            const val MAX_DAY_OF_WEEK = 7
        }
    }
}
