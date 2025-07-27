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

package ru.aleshin.studyassistant.core.database.models.settings

import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.database.utils.BaseLocalEntity
import ru.aleshin.studyassistant.core.domain.entities.common.NumberOfRepeatWeek
import ru.aleshin.studyassistant.core.domain.entities.settings.WeekScheduleViewType

/**
 * @author Stanislav Aleshin on 21.07.2025.
 */
@Serializable
data class BaseCalendarSettingsEntity(
    override val uid: String = "2",
    val numberOfWeek: String = NumberOfRepeatWeek.ONE.name,
    val weekScheduleViewType: String = WeekScheduleViewType.COMMON.name,
    val holidays: List<String>? = emptyList(),
    override val updatedAt: Long = 0,
    val isCacheData: Long = 1L,
) : BaseLocalEntity() {
    companion object {
        fun default() = BaseCalendarSettingsEntity()
    }
}