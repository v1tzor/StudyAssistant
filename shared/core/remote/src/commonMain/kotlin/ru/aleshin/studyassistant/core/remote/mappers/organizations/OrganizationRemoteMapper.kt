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

package ru.aleshin.studyassistant.core.remote.mappers.organizations

import ru.aleshin.studyassistant.core.remote.models.organizations.OrganizationDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.organizations.OrganizationPojo
import ru.aleshin.studyassistant.core.remote.models.subjects.SubjectDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.users.EmployeePojo

/**
 * @author Stanislav Aleshin on 30.04.2024.
 */
fun OrganizationDetailsPojo.mapToBase() = OrganizationPojo(
    uid = uid,
    main = isMain,
    shortName = shortName,
    fullName = fullName,
    type = type,
    scheduleTimeIntervals = scheduleTimeIntervals,
    avatar = avatar,
    emails = emails,
    phones = phones,
    locations = locations,
    webs = webs,
    offices = offices,
    hide = isHide,
)

fun OrganizationPojo.mapToDetails(
    subjects: List<SubjectDetailsPojo>,
    employee: List<EmployeePojo>,
) = OrganizationDetailsPojo(
    uid = uid,
    isMain = main,
    shortName = shortName,
    fullName = fullName,
    type = type,
    avatar = avatar,
    scheduleTimeIntervals = scheduleTimeIntervals,
    subjects = subjects,
    employee = employee,
    emails = emails,
    phones = phones,
    locations = locations,
    webs = webs,
    offices = offices,
    isHide = hide,
)