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

package ru.aleshin.studyassistant.chat.impl.presentation.models.orgnizations

import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize
import ru.aleshin.studyassistant.chat.impl.presentation.models.users.ContactInfoUi
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.organizations.OrganizationType

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
@Parcelize
internal data class OrganizationShortUi(
    val uid: UID,
    val isMain: Boolean,
    val shortName: String,
    val type: OrganizationType,
    val locations: List<ContactInfoUi>,
    val avatar: String? = null,
    val offices: List<String>,
    val scheduleTimeIntervals: ScheduleTimeIntervalsUi = ScheduleTimeIntervalsUi(),
) : Parcelable

internal fun OrganizationUi.convertToShort() = OrganizationShortUi(
    uid = uid,
    isMain = isMain,
    shortName = shortName,
    type = type,
    locations = locations,
    offices = offices,
    scheduleTimeIntervals = scheduleTimeIntervals,
    avatar = avatar
)