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

package ru.aleshin.studyassistant.settings.impl.presentation.models.settings

import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize
import ru.aleshin.studyassistant.core.domain.entities.common.NumberOfRepeatWeek
import ru.aleshin.studyassistant.core.domain.entities.settings.WeekScheduleViewType

/**
 * @author Stanislav Aleshin on 29.04.2024.
 */
@Parcelize
internal data class CalendarSettingsUi(
    val numberOfWeek: NumberOfRepeatWeek = NumberOfRepeatWeek.ONE,
    val weekScheduleViewType: WeekScheduleViewType = WeekScheduleViewType.COMMON,
    val holidays: List<HolidaysUi> = emptyList(),
    val updatedAt: Long,
) : Parcelable