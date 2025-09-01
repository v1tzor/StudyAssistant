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

package ru.aleshin.studyassistant.info.impl.presentation.ui.employee.contract

import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.architecture.component.BaseInput
import ru.aleshin.studyassistant.core.common.architecture.component.BaseOutput
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreAction
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEffect
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEvent
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreState
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.editor.api.EditorFeatureComponent.EditorConfig
import ru.aleshin.studyassistant.info.impl.domain.entities.InfoFailures
import ru.aleshin.studyassistant.info.impl.presentation.models.orgnizations.OrganizationShortUi
import ru.aleshin.studyassistant.info.impl.presentation.models.subjects.SubjectUi
import ru.aleshin.studyassistant.info.impl.presentation.models.users.EmployeeAndSubjectsUi
import ru.aleshin.studyassistant.users.api.UsersFeatureComponent.UsersConfig

/**
 * @author Stanislav Aleshin on 17.06.2024
 */
@Serializable
internal data class EmployeeState(
    val isLoading: Boolean = true,
    val organizations: List<OrganizationShortUi> = emptyList(),
    val selectedOrganization: UID? = null,
    val subjects: List<SubjectUi> = emptyList(),
    val employees: Map<Char, List<EmployeeAndSubjectsUi>> = mapOf(),
) : StoreState

internal sealed class EmployeeEvent : StoreEvent {
    data class Started(val inputData: EmployeeInput) : EmployeeEvent()
    data class SearchEmployee(val query: String) : EmployeeEvent()
    data class SelectedOrganization(val organization: UID) : EmployeeEvent()
    data class ClickDeleteEmployee(val employeeId: UID) : EmployeeEvent()
    data class ClickEmployeeProfile(val employeeId: UID) : EmployeeEvent()
    data class ClickEditEmployee(val employeeId: UID?) : EmployeeEvent()
    data object BackClick : EmployeeEvent()
}

internal sealed class EmployeeEffect : StoreEffect {
    data class ShowError(val failures: InfoFailures) : EmployeeEffect()
}

internal sealed class EmployeeAction : StoreAction {
    data class UpdateEmployees(val employees: Map<Char, List<EmployeeAndSubjectsUi>>) : EmployeeAction()

    data class UpdateOrganizations(
        val selectedOrganization: UID?,
        val organizations: List<OrganizationShortUi>
    ) : EmployeeAction()

    data class UpdateSelectedOrganization(val organization: UID) : EmployeeAction()
    data class UpdateLoading(val isLoading: Boolean) : EmployeeAction()
}

internal data class EmployeeInput(
    val organizationId: UID
) : BaseInput

internal sealed class EmployeeOutput : BaseOutput {
    data object NavigateToBack : EmployeeOutput()
    data class NavigateToEmployeeEditor(val config: EditorConfig.Employee) : EmployeeOutput()
    data class NavigateToEmployeeProfile(val config: UsersConfig.EmployeeProfile) : EmployeeOutput()
}