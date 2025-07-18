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

package ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks

import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize
import ru.aleshin.studyassistant.core.domain.entities.tasks.fetchAllTasks
import ru.aleshin.studyassistant.core.domain.entities.tasks.toHomeworkComponents
import ru.aleshin.studyassistant.tasks.impl.presentation.mappers.mapToDomain
import ru.aleshin.studyassistant.tasks.impl.presentation.mappers.mapToUi

/**
 * @author Stanislav Aleshin on 01.07.2024.
 */
internal sealed interface HomeworkTaskComponentUi : Parcelable {

    @Parcelize
    data class Label(val text: String) : HomeworkTaskComponentUi

    @Parcelize
    data class Tasks(val taskList: List<String>) : HomeworkTaskComponentUi
}

@Parcelize
internal data class HomeworkTasksDetailsUi(
    val origin: String,
    val components: List<HomeworkTaskComponentUi>,
) : Parcelable

internal fun String.mapToHomeworkTasks() = HomeworkTasksDetailsUi(
    origin = this,
    components = this.toHomeworkComponents().map { it.mapToUi() }
)

internal fun List<HomeworkTaskComponentUi>.fetchAllTasks(): List<String> {
    return this.map { it.mapToDomain() }.fetchAllTasks()
}