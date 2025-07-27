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

package ru.aleshin.studyassistant.core.database.models.schedule

import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.database.utils.BaseLocalEntity

/**
 * @author Stanislav Aleshin on 25.07.2025.
 */
@Serializable
data class BaseScheduleEntity(
    override val uid: String,
    val dateVersionFrom: Long,
    val dateVersionTo: Long,
    val weekDayOfWeek: String,
    val week: String,
    val classes: List<String>,
    override val updatedAt: Long,
    val isCacheData: Long,
) : BaseLocalEntity()