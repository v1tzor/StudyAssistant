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

package ru.aleshin.studyassistant.tasks.api

import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.architecture.component.BaseOutput
import ru.aleshin.studyassistant.core.common.functional.UID

/**
 * @author Stanislav Aleshin on 05.08.2026.
 */
@Serializable
public sealed class TasksConfig {

    @Serializable
    public data object Overview : TasksConfig()

    @Serializable
    public data class Homeworks(val targetDate: Long? = null) : TasksConfig()

    @Serializable
    public data object Todos : TasksConfig()

    @Serializable
    public data class Share(val code: String? = null) : TasksConfig()
}

public sealed class TasksOutput : BaseOutput {
    public data object NavigateToBack : TasksOutput()
    public data object NavigateToAnalytics : TasksOutput()
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
