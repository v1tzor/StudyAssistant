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

package entities.tasks

import entities.organizations.OrganizationShort
import entities.subject.Subject
import extensions.startThisDay
import functional.UID
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * @author Stanislav Aleshin on 01.05.2024.
 */
data class Homework(
    val uid: UID,
    val classId: UID? = null,
    val deadline: Instant = Clock.System.now().startThisDay(),
    val subject: Subject? = null,
    val organization: OrganizationShort,
    val theoreticalTasks: String = "",
    val practicalTasks: String = "",
    val presentationTasks: String = "",
    val test: String? = null,
    val priority: TaskPriority = TaskPriority.STANDARD,
    val isDone: Boolean = false,
    val completeDate: Instant?,
)

fun String.toHomeworkComponents() = buildList<HomeworkTaskComponent> {
    if (isBlank()) return@buildList
    val labelsAndText = split(":", ";", ".", "|")
    labelsAndText.forEachIndexed { index, value ->
        val isLabel = index % 2 == 0
        if (isLabel && labelsAndText.size > 1) {
            add(HomeworkTaskComponent.Label(value.trim()))
        } else {
            val tasks = value.split(",").map { it.trim() }
            add(HomeworkTaskComponent.Tasks(tasks))
        }
    }
}