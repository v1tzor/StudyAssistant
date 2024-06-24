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

package ru.aleshin.studyassistant.editor.impl.presentation.models.tasks

import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize
import dev.icerock.moko.parcelize.TypeParceler
import entities.tasks.TaskPriority
import functional.UID
import kotlinx.datetime.Instant
import platform.InstantParceler
import platform.NullInstantParceler
import ru.aleshin.studyassistant.editor.impl.presentation.models.orgnizations.OrganizationShortUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.subjects.SubjectUi

/**
 * @author Stanislav Aleshin on 22.06.2024.
 */
@Parcelize
internal data class HomeworkUi(
    val uid: UID,
    val classId: UID? = null,
    @TypeParceler<Instant, InstantParceler>
    val date: Instant,
    val subject: SubjectUi? = null,
    val organization: OrganizationShortUi,
    val theoreticalTasks: String = "",
    val practicalTasks: String = "",
    val presentationTasks: String = "",
    val test: String? = null,
    val priority: TaskPriority = TaskPriority.STANDARD,
    val isDone: Boolean = false,
    @TypeParceler<Instant?, NullInstantParceler>
    val completeDate: Instant?,
) : Parcelable