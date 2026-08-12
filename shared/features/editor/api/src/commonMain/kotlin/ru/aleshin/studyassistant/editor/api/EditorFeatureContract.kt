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

package ru.aleshin.studyassistant.editor.api

import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.architecture.component.BaseOutput
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.common.NumberOfRepeatWeek

/**
 * @author Stanislav Aleshin on 05.08.2026.
 */
@Serializable
public sealed class EditorConfig {

    @Serializable
    public data class WeekSchedule(
        val week: NumberOfRepeatWeek = NumberOfRepeatWeek.ONE,
    ) : EditorConfig()

    @Serializable
    public data class DailySchedule(
        val date: Long,
        val customScheduleId: UID?,
        val baseScheduleId: UID?,
    ) : EditorConfig()

    @Serializable
    public data class Class(
        val classId: UID?,
        val scheduleId: UID?,
        val organizationId: UID?,
        val isCustomSchedule: Boolean,
        val weekDay: DayOfNumberedWeekUi,
    ) : EditorConfig()

    @Serializable
    public data class Subject(
        val subjectId: UID?,
        val organizationId: UID,
    ) : EditorConfig()

    @Serializable
    public data class Employee(
        val employeeId: UID?,
        val organizationId: UID,
    ) : EditorConfig()

    @Serializable
    public data class Homework(
        val homeworkId: UID?,
        val date: Long?,
        val subjectId: UID?,
        val organizationId: UID?
    ) : EditorConfig()

    @Serializable
    public data class Todo(
        val todoId: UID?,
    ) : EditorConfig()

    @Serializable
    public data class Organization(
        val organizationId: UID?,
    ) : EditorConfig()

    @Serializable
    public data object Profile : EditorConfig()
}

public sealed class EditorOutput : BaseOutput {
    public data object NavigateToBack : EditorOutput()
    public data class NavigateToImport(val rawText: String?) : EditorOutput()
}
