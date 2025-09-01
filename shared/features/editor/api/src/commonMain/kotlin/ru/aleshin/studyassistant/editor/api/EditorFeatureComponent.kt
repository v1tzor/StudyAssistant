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

package ru.aleshin.studyassistant.editor.api

import com.arkivanov.decompose.ComponentContext
import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.architecture.component.BaseOutput
import ru.aleshin.studyassistant.core.common.architecture.component.FeatureComponent
import ru.aleshin.studyassistant.core.common.architecture.component.OutputConsumer
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.inject.StartFeatureConfig
import ru.aleshin.studyassistant.core.domain.entities.common.NumberOfRepeatWeek
import ru.aleshin.studyassistant.editor.api.EditorFeatureComponent.EditorConfig
import ru.aleshin.studyassistant.editor.api.EditorFeatureComponent.EditorOutput

/**
 * @author Stanislav Aleshin on 21.08.2025.
 */
public abstract class EditorFeatureComponent(
    componentContext: ComponentContext,
    startConfig: StartFeatureConfig<EditorConfig>,
    outputConsumer: OutputConsumer<EditorOutput>,
) : FeatureComponent<EditorConfig, EditorOutput>(
    componentContext = componentContext,
    startConfig = startConfig,
    outputConsumer = outputConsumer,
) {

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
        public data object NavigateToBilling : EditorOutput()
    }
}