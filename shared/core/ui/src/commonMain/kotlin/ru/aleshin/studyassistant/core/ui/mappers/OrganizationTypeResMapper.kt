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

package ru.aleshin.studyassistant.core.ui.mappers

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.domain.entities.organizations.OrganizationType
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.additional_education_organization_type as core_additional_education_organization_type
import ru.aleshin.studyassistant.core.ui.resources.college_organization_type as core_college_organization_type
import ru.aleshin.studyassistant.core.ui.resources.courses_organization_type as core_courses_organization_type
import ru.aleshin.studyassistant.core.ui.resources.gymnasium_organization_type as core_gymnasium_organization_type
import ru.aleshin.studyassistant.core.ui.resources.ic_additional_organization as core_ic_additional_organization
import ru.aleshin.studyassistant.core.ui.resources.ic_church as core_ic_church
import ru.aleshin.studyassistant.core.ui.resources.ic_class as core_ic_class
import ru.aleshin.studyassistant.core.ui.resources.ic_university as core_ic_university
import ru.aleshin.studyassistant.core.ui.resources.lyceum_organization_type as core_lyceum_organization_type
import ru.aleshin.studyassistant.core.ui.resources.school_organization_type as core_school_organization_type
import ru.aleshin.studyassistant.core.ui.resources.seminary_organization_type as core_seminary_organization_type
import ru.aleshin.studyassistant.core.ui.resources.university_organization_type as core_university_organization_type

/**
 * @author Stanislav Aleshin on 27.04.2024.
 */
@Composable
fun OrganizationType.mapToSting() = when (this) {
    OrganizationType.SCHOOL -> stringResource(CoreRes.string.core_school_organization_type)
    OrganizationType.LYCEUM -> stringResource(CoreRes.string.core_lyceum_organization_type)
    OrganizationType.GYMNASIUM -> stringResource(CoreRes.string.core_gymnasium_organization_type)
    OrganizationType.SEMINARY -> stringResource(CoreRes.string.core_seminary_organization_type)
    OrganizationType.COLLEGE -> stringResource(CoreRes.string.core_college_organization_type)
    OrganizationType.UNIVERSITY -> stringResource(CoreRes.string.core_university_organization_type)
    OrganizationType.ADDITIONAL_EDUCATION -> stringResource(CoreRes.string.core_additional_education_organization_type)
    OrganizationType.COURSES -> stringResource(CoreRes.string.core_courses_organization_type)
}

fun OrganizationType.mapToIcon() = when (this) {
    OrganizationType.SCHOOL -> CoreRes.drawable.core_ic_class
    OrganizationType.LYCEUM -> CoreRes.drawable.core_ic_class
    OrganizationType.GYMNASIUM -> CoreRes.drawable.core_ic_class
    OrganizationType.SEMINARY -> CoreRes.drawable.core_ic_church
    OrganizationType.COLLEGE -> CoreRes.drawable.core_ic_university
    OrganizationType.UNIVERSITY -> CoreRes.drawable.core_ic_university
    OrganizationType.ADDITIONAL_EDUCATION -> CoreRes.drawable.core_ic_additional_organization
    OrganizationType.COURSES -> CoreRes.drawable.core_ic_additional_organization
}