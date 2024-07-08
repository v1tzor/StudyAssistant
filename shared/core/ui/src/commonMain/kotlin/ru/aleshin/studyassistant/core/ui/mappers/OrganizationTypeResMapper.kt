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

package ru.aleshin.studyassistant.core.ui.mappers

import ru.aleshin.studyassistant.core.domain.entities.organizations.OrganizationType
import ru.aleshin.studyassistant.core.ui.theme.tokens.StudyAssistantIcons
import ru.aleshin.studyassistant.core.ui.theme.tokens.StudyAssistantStrings

/**
 * @author Stanislav Aleshin on 27.04.2024.
 */
fun OrganizationType.mapToSting(strings: StudyAssistantStrings) = when (this) {
    OrganizationType.SCHOOL -> strings.schoolOrganizationType
    OrganizationType.LYCEUM -> strings.lyceumOrganizationType
    OrganizationType.GYMNASIUM -> strings.gymnasiumOrganizationType
    OrganizationType.SEMINARY -> strings.seminaryOrganizationType
    OrganizationType.COLLEGE -> strings.collegeOrganizationType
    OrganizationType.UNIVERSITY -> strings.universityOrganizationType
    OrganizationType.ADDITIONAL_EDUCATION -> strings.additionalEducationOrganizationType
    OrganizationType.COURSES -> strings.coursesOrganizationType
}

fun OrganizationType.mapToIcon(icons: StudyAssistantIcons) = when (this) {
    OrganizationType.SCHOOL -> icons.classes
    OrganizationType.LYCEUM -> icons.classes
    OrganizationType.GYMNASIUM -> icons.classes
    OrganizationType.SEMINARY -> icons.seminary
    OrganizationType.COLLEGE -> icons.university
    OrganizationType.UNIVERSITY -> icons.university
    OrganizationType.ADDITIONAL_EDUCATION -> icons.additionalEducation
    OrganizationType.COURSES -> icons.additionalEducation
}