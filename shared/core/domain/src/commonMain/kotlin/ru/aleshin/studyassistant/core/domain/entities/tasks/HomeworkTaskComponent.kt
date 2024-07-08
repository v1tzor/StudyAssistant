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

package ru.aleshin.studyassistant.core.domain.entities.tasks

import ru.aleshin.studyassistant.core.common.extensions.extractAllItem

/**
 * @author Stanislav Aleshin on 01.07.2024.
 */
sealed class HomeworkTaskComponent {
    data class Label(val text: String) : HomeworkTaskComponent()
    data class Tasks(val taskList: List<String>) : HomeworkTaskComponent()
}

typealias HomeworkTasks = List<HomeworkTaskComponent>

fun HomeworkTasks.fetchAllTasks(): List<String> {
    val tasks = map { taskComponent ->
        if (taskComponent is HomeworkTaskComponent.Tasks) taskComponent.taskList else emptyList()
    }
    return tasks.extractAllItem()
}