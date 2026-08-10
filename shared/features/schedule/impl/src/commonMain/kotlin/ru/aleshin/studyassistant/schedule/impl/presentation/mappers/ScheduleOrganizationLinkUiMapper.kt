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

package ru.aleshin.studyassistant.schedule.impl.presentation.mappers

import ru.aleshin.studyassistant.core.presentation.mappers.organizations.mapToDomain as mapOrganizationToDomain
import ru.aleshin.studyassistant.core.presentation.mappers.organizations.mapToUi as mapOrganizationToUi
import ru.aleshin.studyassistant.core.presentation.mappers.subjects.mapToDomain as mapSubjectToDomain
import ru.aleshin.studyassistant.core.presentation.mappers.subjects.mapToUi as mapSubjectToUi
import ru.aleshin.studyassistant.core.presentation.mappers.users.mapToDomain as mapEmployeeToDomain
import ru.aleshin.studyassistant.core.presentation.mappers.users.mapToUi as mapEmployeeToUi
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleOrganizationLink
import ru.aleshin.studyassistant.schedule.impl.presentation.models.share.OrganizationLinkData

/**
 * @author Stanislav Aleshin on 08.08.2026.
 */
internal fun ScheduleOrganizationLink.mapToUi() = OrganizationLinkData(
    sharedOrganization = sharedOrganization.mapToUi(),
    linkedOrganization = linkedOrganization?.mapOrganizationToUi(),
    linkedSubjects = linkedSubjects.mapValues { (_, subject) -> subject.mapSubjectToUi() },
    linkedTeachers = linkedTeachers.mapValues { (_, teacher) -> teacher.mapEmployeeToUi() },
)

internal fun OrganizationLinkData.mapToDomain() = ScheduleOrganizationLink(
    sharedOrganization = sharedOrganization.mapToDomain(),
    linkedOrganization = linkedOrganization?.mapOrganizationToDomain(),
    linkedSubjects = linkedSubjects.mapValues { (_, subject) -> subject.mapSubjectToDomain() },
    linkedTeachers = linkedTeachers.mapValues { (_, teacher) -> teacher.mapEmployeeToDomain() },
)
