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
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.platform.NullInstantParceler
import ru.aleshin.studyassistant.core.domain.entities.tasks.TaskPriority

/**
 * @author Stanislav Aleshin on 26.07.2024.
 */
@Parcelize
internal data class TodoUi(
    val uid: UID,
    @TypeParceler<Instant?, NullInstantParceler>
    val deadline: Instant?,
    val name: String,
    val priority: TaskPriority,
    val notification: Boolean = true,
    val isDone: Boolean = false,
    @TypeParceler<Instant?, NullInstantParceler>
    val completeDate: Instant? = null,
) : Parcelable