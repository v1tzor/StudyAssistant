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
import dev.icerock.moko.parcelize.TypeParceler
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.platform.NullInstantParceler

/**
 * @author Stanislav Aleshin on 28.08.2024.
 */
@Parcelize
internal data class EditHolidaysUi(
    val organizations: List<UID> = emptyList(),
    @TypeParceler<Instant?, NullInstantParceler> val start: Instant? = null,
    @TypeParceler<Instant?, NullInstantParceler> val end: Instant? = null,
) : Parcelable {
    fun isValid() = organizations.isNotEmpty() && start != null && end != null
}

internal fun HolidaysUi.convertToEdit() = EditHolidaysUi(
    organizations = organizations,
    start = start,
    end = end,
)

internal fun EditHolidaysUi.convertToBase() = HolidaysUi(
    organizations = organizations,
    start = checkNotNull(start),
    end = checkNotNull(end),
)