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

package ru.aleshin.studyassistant.core.domain.entities.schedules

import ru.aleshin.studyassistant.core.domain.entities.classes.Class
import ru.aleshin.studyassistant.core.domain.entities.classes.ClassDetails
import ru.aleshin.studyassistant.core.domain.entities.schedules.base.BaseScheduleDetails
import ru.aleshin.studyassistant.core.domain.entities.schedules.base.convertToDetails
import ru.aleshin.studyassistant.core.domain.entities.schedules.custom.CustomScheduleDetails
import ru.aleshin.studyassistant.core.domain.entities.schedules.custom.convertToDetails

/**
 * @author Stanislav Aleshin on 08.06.2024.
 */
sealed class ScheduleDetails {
    data class Base(val data: BaseScheduleDetails?) : ScheduleDetails()
    data class Custom(val data: CustomScheduleDetails?) : ScheduleDetails()
}

fun Schedule.convertToDetails(
    classesMapper: (Class) -> ClassDetails,
) = when (this) {
    is Schedule.Base -> ScheduleDetails.Base(data?.convertToDetails(classesMapper))
    is Schedule.Custom -> ScheduleDetails.Custom(data?.convertToDetails(classesMapper))
}