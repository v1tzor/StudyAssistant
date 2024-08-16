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

package ru.aleshin.studyassistant.schedule.impl.presentation.models.schedule

import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize
import kotlinx.datetime.DayOfWeek
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.common.NumberOfRepeatWeek
import ru.aleshin.studyassistant.core.domain.entities.schedules.DateVersion
import ru.aleshin.studyassistant.schedule.impl.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.classes.MediatedClassUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.classes.convertToBase
import ru.aleshin.studyassistant.schedule.impl.presentation.models.share.OrganizationLinkData

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
@Parcelize
internal data class MediatedBaseScheduleUi(
    val uid: UID,
    val dateVersion: DateVersion,
    val dayOfWeek: DayOfWeek,
    val week: NumberOfRepeatWeek = NumberOfRepeatWeek.ONE,
    val classes: List<MediatedClassUi>,
) : Parcelable

internal fun MediatedBaseScheduleUi.convertToBase(
    linkDataMapper: (UID) -> OrganizationLinkData
): BaseScheduleUi {
    val groupedClasses = classes.groupBy { it.organizationId }
    return BaseScheduleUi(
        uid = uid,
        dateVersion = dateVersion.mapToUi(),
        dayOfWeek = dayOfWeek,
        week = week,
        classes = classes.map { classModel ->
            val number = groupedClasses[classModel.organizationId]?.indexOf(classModel)?.inc() ?: 0
            val linkData = linkDataMapper(classModel.organizationId)
            classModel.convertToBase(linkData, number)
        }.sortedBy { classModel ->
            classModel.timeRange.from.dateTime().time
        },
    )
}