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

package ru.aleshin.studyassistant.core.remote.models.goals

import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalTime
import ru.aleshin.studyassistant.core.domain.entities.organizations.Millis

/**
 * @author Stanislav Aleshin on 18.04.2025.
 */
@Serializable
data class GoalPojo(
    val uid: UID = "",
    val userId: UID,
    val type: String = "",
    val number: Int = 0,
    val contentId: UID = "",
    val contentOrganizationId: UID? = null,
    val contentDeadline: Long? = null,
    val targetDate: Long = 0L,
    val desiredTime: Millis? = null,
    val goalTimeType: String = GoalTime.Type.NONE.toString(),
    val targetTime: Millis? = null,
    val pastStopTime: Millis? = null,
    val startTimePoint: Long? = null,
    val active: Boolean = false,
    val completeAfterTimeElapsed: Boolean = false,
    val done: Boolean = false,
    val completeDate: Long? = null,
)