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

package ru.aleshin.studyassistant.tasks.impl.presentation.models.goals

import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize
import dev.icerock.moko.parcelize.TypeParceler
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.platform.InstantParceler
import ru.aleshin.studyassistant.core.common.platform.NullInstantParceler
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalType
import ru.aleshin.studyassistant.core.domain.entities.organizations.Millis
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.TodoDetailsUi

/**
 * @author Stanislav Aleshin on 01.06.2025.
 */
@Parcelize
internal data class GoalDetailsUi(
    val uid: UID,
    val contentType: GoalType,
    val contentHomework: HomeworkUi? = null,
    val contentTodo: TodoDetailsUi? = null,
    val number: Int = 0,
    @TypeParceler<Instant, InstantParceler> val targetDate: Instant,
    val desiredTime: Millis?,
    val time: GoalTimeUi,
    val completeAfterTimeElapsed: Boolean = false,
    val isDone: Boolean = false,
    @TypeParceler<Instant?, NullInstantParceler> val completeDate: Instant?,
) : Parcelable