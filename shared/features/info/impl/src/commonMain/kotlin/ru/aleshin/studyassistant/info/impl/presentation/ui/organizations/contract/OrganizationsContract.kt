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

package ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.contract

import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.architecture.component.BaseOutput
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreAction
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEffect
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEvent
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreState
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.editor.api.EditorFeatureComponent.EditorConfig
import ru.aleshin.studyassistant.info.api.InfoFeatureComponent.InfoConfig
import ru.aleshin.studyassistant.info.impl.domain.entities.InfoFailures
import ru.aleshin.studyassistant.info.impl.presentation.models.orgnizations.OrganizationClassesInfoUi
import ru.aleshin.studyassistant.info.impl.presentation.models.orgnizations.OrganizationShortUi
import ru.aleshin.studyassistant.info.impl.presentation.models.orgnizations.OrganizationUi
import ru.aleshin.studyassistant.users.api.UsersFeatureComponent.UsersConfig

/**
 * @author Stanislav Aleshin on 16.06.2024
 */
@Serializable
internal data class OrganizationsState(
    val isLoading: Boolean = true,
    val isPaidUser: Boolean = false,
    val shortOrganizations: List<OrganizationShortUi>? = null,
    val organizationData: OrganizationUi? = null,
    val classesInfo: OrganizationClassesInfoUi? = null,
) : StoreState

internal sealed class OrganizationsEvent : StoreEvent {
    data object Started : OrganizationsEvent()
    data class Refresh(val organizationId: UID?) : OrganizationsEvent()
    data class ChangeOrganization(val organizationId: UID?) : OrganizationsEvent()
    data class ClickEmployee(val employeeId: UID) : OrganizationsEvent()
    data class ClickShowAllEmployees(val organizationId: UID) : OrganizationsEvent()
    data class ClickShowAllSubjects(val organizationId: UID) : OrganizationsEvent()
    data class ClickEditOrganization(val organizationId: UID?) : OrganizationsEvent()
    data class ClickEditSubject(val subjectId: UID, val organizationId: UID) : OrganizationsEvent()
    data object ClickPaidFunction : OrganizationsEvent()
}

internal sealed class OrganizationsEffect : StoreEffect {
    data class ShowError(val failures: InfoFailures) : OrganizationsEffect()
}

internal sealed class OrganizationsAction : StoreAction {
    data class UpdateShortOrganizations(val organizations: List<OrganizationShortUi>) : OrganizationsAction()
    data class UpdatePaidUserStatus(val isPaidUser: Boolean) : OrganizationsAction()
    data class UpdateOrganizationData(
        val data: OrganizationUi?,
        val classesInfo: OrganizationClassesInfoUi?,
    ) : OrganizationsAction()
    data class UpdateLoading(val isLoading: Boolean) : OrganizationsAction()
}

internal sealed class OrganizationsOutput : BaseOutput {
    data object NavigateToBilling : OrganizationsOutput()
    data class NavigateToEmployeeProfile(val config: UsersConfig.EmployeeProfile) : OrganizationsOutput()
    data class NavigateToSubjects(val config: InfoConfig.Subjects) : OrganizationsOutput()
    data class NavigateToEmployees(val config: InfoConfig.Employee) : OrganizationsOutput()
    data class NavigateToSubjectEditor(val config: EditorConfig.Subject) : OrganizationsOutput()
    data class NavigateToOrganizationEditor(val config: EditorConfig.Organization) : OrganizationsOutput()
}