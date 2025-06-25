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

package ru.aleshin.studyassistant.chat.impl.presentation.mappers

import ru.aleshin.studyassistant.chat.impl.presentation.models.classes.ClassUi
import ru.aleshin.studyassistant.core.domain.entities.classes.Class

/**
 * @author Stanislav Aleshin on 30.05.2024.
 */
internal fun Class.mapToUi(number: Int) = ClassUi(
    uid = uid,
    scheduleId = scheduleId,
    organization = organization.mapToUi(),
    eventType = eventType,
    subject = subject?.mapToUi(),
    customData = customData,
    teacher = teacher?.mapToUi(),
    office = office,
    location = location?.mapToUi(),
    timeRange = timeRange,
    number = number,
)

internal fun ClassUi.mapToDomain() = Class(
    uid = uid,
    scheduleId = scheduleId,
    organization = organization.mapToDomain(),
    eventType = eventType,
    subject = subject?.mapToDomain(),
    customData = customData,
    teacher = teacher?.mapToDomain(),
    office = office,
    location = location?.mapToDomain(),
    timeRange = timeRange,
)