/*
 * Copyright 2026 Stanislav Aleshin
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

package ru.aleshin.studyassistant.core.data.mappers.organizations

import ru.aleshin.studyassistant.core.database.models.organizations.NumberedDurationEntity
import ru.aleshin.studyassistant.core.domain.entities.organizations.NumberedDuration
import ru.aleshin.studyassistant.core.remote.models.organizations.NumberedDurationPojo

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
fun NumberedDurationPojo.mapToDomain() = NumberedDuration(
    number = number,
    duration = duration,
)

fun NumberedDurationEntity.mapToDomain() = NumberedDuration(
    number = number,
    duration = duration,
)

fun NumberedDuration.mapToRemoteDate() = NumberedDurationPojo(
    number = number,
    duration = duration,
)

fun NumberedDuration.mapToLocalDate() = NumberedDurationEntity(
    number = number,
    duration = duration,
)
