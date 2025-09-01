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

package ru.aleshin.studyassistant.tasks.api

import com.arkivanov.decompose.ComponentContext
import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.architecture.component.BaseOutput
import ru.aleshin.studyassistant.core.common.architecture.component.FeatureComponent
import ru.aleshin.studyassistant.core.common.architecture.component.OutputConsumer
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.inject.StartFeatureConfig
import ru.aleshin.studyassistant.tasks.api.TasksFeatureComponent.TasksConfig
import ru.aleshin.studyassistant.tasks.api.TasksFeatureComponent.TasksOutput

/**
 * @author Stanislav Aleshin on 25.08.2025.
 */
public abstract class TasksFeatureComponent(
    componentContext: ComponentContext,
    startConfig: StartFeatureConfig<TasksConfig>,
    outputConsumer: OutputConsumer<TasksOutput>,
) : FeatureComponent<TasksConfig, TasksOutput>(
    componentContext = componentContext,
    startConfig = startConfig,
    outputConsumer = outputConsumer,
) {

    @Serializable
    public sealed class TasksConfig {

        @Serializable
        public data object Overview : TasksConfig()

        @Serializable
        public data class Homeworks(val targetDate: Long? = null) : TasksConfig()

        @Serializable
        public data object Todos : TasksConfig()

        @Serializable
        public data object Share : TasksConfig()
    }

    public sealed class TasksOutput : BaseOutput {
        public data object NavigateToBack : TasksOutput()
        public data object NavigateToBilling : TasksOutput()
        public data class NavigateToUserProfile(val userId: UID) : TasksOutput()
        public sealed class NavigateToEditor : TasksOutput() {

            public data class Homework(
                val homeworkId: UID?,
                val date: Long?,
                val subjectId: UID?,
                val organizationId: UID?
            ) : NavigateToEditor()

            public data class Todo(
                val todoId: UID?,
            ) : NavigateToEditor()

            public data class Subject(
                val subjectId: UID?,
                val organizationId: UID,
            ) : NavigateToEditor()
        }
    }
}