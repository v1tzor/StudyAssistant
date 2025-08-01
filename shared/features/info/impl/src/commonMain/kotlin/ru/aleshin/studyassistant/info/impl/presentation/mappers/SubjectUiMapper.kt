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

package ru.aleshin.studyassistant.info.impl.presentation.mappers

import ru.aleshin.studyassistant.core.domain.entities.subject.Subject
import ru.aleshin.studyassistant.info.impl.presentation.models.subjects.SubjectSortedType
import ru.aleshin.studyassistant.info.impl.presentation.models.subjects.SubjectUi
import ru.aleshin.studyassistant.info.impl.presentation.ui.theme.tokens.InfoStrings

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
internal fun Subject.mapToUi() = SubjectUi(
    uid = uid,
    organizationId = organizationId,
    eventType = eventType,
    name = name,
    teacher = teacher?.mapToUi(),
    office = office,
    color = color,
    location = location?.mapToUi(),
    updatedAt = updatedAt,
)

internal fun SubjectUi.mapToDomain() = Subject(
    uid = uid,
    organizationId = organizationId,
    eventType = eventType,
    name = name,
    teacher = teacher?.mapToDomain(),
    office = office,
    color = color,
    location = location?.mapToDomain(),
    updatedAt = updatedAt,
)

internal fun SubjectSortedType.mapToString(strings: InfoStrings) = when (this) {
    SubjectSortedType.ALPHABETIC -> strings.alphabeticSortedType
    SubjectSortedType.TEACHER -> strings.teacherSortedType
    SubjectSortedType.EVENT_TYPE -> strings.eventTypeSortedType
    SubjectSortedType.OFFICE -> strings.officeSortedType
    SubjectSortedType.LOCATION -> strings.locationSortedType
}