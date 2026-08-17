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
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleImportClass
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleImportSession

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
internal interface ScheduleImportValidator {

    fun isNoteValid(note: String): Boolean
    fun isDraftValid(draft: ScheduleImportDraft): Boolean
    fun isSessionValid(session: ScheduleImportSession): Boolean
    fun parseTime(value: String?): LocalTime?

    class Base : ScheduleImportValidator {

        override fun isNoteValid(note: String): Boolean {
            return note.trim().length <= MAX_NOTE_LENGTH
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

        override fun isSessionValid(session: ScheduleImportSession): Boolean {
            val classes = session.classes.filter(ScheduleImportClass::included)
            if (classes.isEmpty() || classes.size > MAX_ENTRIES) return false
            val subjectIds = session.subjects.map { subject -> subject.uid }.toSet()

            return classes.all { classModel ->
                val start = parseTime(classModel.startTime)
                val end = parseTime(classModel.endTime)
                val subjectId = classModel.subjectId
                classModel.repeatWeek in MIN_REPEAT_WEEK..MAX_REPEAT_WEEK &&
                    classModel.dayOfWeek in MIN_DAY_OF_WEEK..MAX_DAY_OF_WEEK &&
                    subjectId != null &&
                    subjectId in subjectIds &&
                    start != null &&
                    end != null &&
                    start < end
            } && classes.distinctBy { classModel ->
                listOf(
                    classModel.repeatWeek.toString(),
                    classModel.dayOfWeek.toString(),
                    classModel.startTime,
                    classModel.endTime,
                    classModel.subjectId,
                )
            }.size == classes.size
        }

        override fun parseTime(value: String?): LocalTime? {
            val raw = value?.trim().orEmpty()
            if (raw.isEmpty()) return null
            val normalized = raw.replace('.', ':')
            runCatching { LocalTime.parse(normalized) }.getOrNull()?.let { parsed ->
                return parsed
            }
            val parts = normalized.split(':')
            if (parts.size !in 2..3) return null
            val hour = parts[0].toIntOrNull() ?: return null
            val minute = parts[1].toIntOrNull() ?: return null
            val second = parts.getOrNull(2)?.toIntOrNull() ?: 0
            return runCatching { LocalTime(hour, minute, second) }.getOrNull()
        }

        private companion object {
            const val MAX_NOTE_LENGTH = 120
            const val MAX_ENTRIES = 150
            const val MIN_REPEAT_WEEK = 1
            const val MAX_REPEAT_WEEK = 3
            const val MIN_DAY_OF_WEEK = 1
            const val MAX_DAY_OF_WEEK = 7
        }
    }
}
