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

package ru.aleshin.studyassistant.info.impl.presentation.ui.organizations

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.presentation.models.users.ContactInfoUi
import ru.aleshin.studyassistant.info.impl.presentation.ui.InfoLayoutMode
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.contract.OrganizationsState
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.layouts.OrganizationsCompactLayout
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.layouts.OrganizationsExpandedLayout

/**
 * @author Stanislav Aleshin on 17.08.2026.
 */
@Composable
internal fun OrganizationsLayout(
    modifier: Modifier = Modifier,
    layoutMode: InfoLayoutMode,
    state: OrganizationsState,
    onRefresh: () -> Unit,
    onAddOrganization: () -> Unit,
    onEditOrganization: () -> Unit,
    onSelectOrganization: (UID?) -> Unit,
    onEditOrganizationId: (UID) -> Unit,
    onCopyContactInfo: (ContactInfoUi) -> Unit,
    onShowAllEmployee: () -> Unit,
    onShowEmployeeProfile: (UID) -> Unit,
    onShowAllSubjects: () -> Unit,
    onShowSubjectEditor: (UID) -> Unit,
) {
    when (layoutMode) {
        InfoLayoutMode.COMPACT -> OrganizationsCompactLayout(
            modifier = modifier,
            state = state,
            onRefresh = onRefresh,
            onAddOrganization = onAddOrganization,
            onEditOrganization = onEditOrganization,
            onCopyContactInfo = onCopyContactInfo,
            onShowAllEmployee = onShowAllEmployee,
            onShowEmployeeProfile = onShowEmployeeProfile,
            onShowAllSubjects = onShowAllSubjects,
            onShowSubjectEditor = onShowSubjectEditor,
        )
        InfoLayoutMode.EXPANDED -> OrganizationsExpandedLayout(
            modifier = modifier,
            state = state,
            onRefresh = onRefresh,
            onAddOrganization = onAddOrganization,
            onSelectOrganization = onSelectOrganization,
            onEditOrganizationId = onEditOrganizationId,
            onCopyContactInfo = onCopyContactInfo,
            onShowAllEmployee = onShowAllEmployee,
            onShowEmployeeProfile = onShowEmployeeProfile,
            onShowAllSubjects = onShowAllSubjects,
            onShowSubjectEditor = onShowSubjectEditor,
        )
    }
}
