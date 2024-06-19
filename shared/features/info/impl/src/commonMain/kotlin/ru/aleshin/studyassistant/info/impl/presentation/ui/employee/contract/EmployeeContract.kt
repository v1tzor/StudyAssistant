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

import architecture.screenmodel.contract.BaseAction
import architecture.screenmodel.contract.BaseEvent
import architecture.screenmodel.contract.BaseUiEffect
import architecture.screenmodel.contract.BaseViewState
import cafe.adriel.voyager.core.screen.Screen
import dev.icerock.moko.parcelize.Parcelize
import functional.UID
import ru.aleshin.studyassistant.info.impl.domain.entities.InfoFailures
import ru.aleshin.studyassistant.info.impl.presentation.models.orgnizations.OrganizationShortUi
import ru.aleshin.studyassistant.info.impl.presentation.models.subjects.SubjectUi
import ru.aleshin.studyassistant.info.impl.presentation.models.users.EmployeeAndSubjectsUi

/**
 * @author Stanislav Aleshin on 17.06.2024
 */
@Parcelize
internal data class EmployeeViewState(
    val isLoading: Boolean = true,
    val organizations: List<OrganizationShortUi> = emptyList(),
    val selectedOrganization: UID? = null,
    val subjects: List<SubjectUi> = emptyList(),
    val employees: Map<Char, List<EmployeeAndSubjectsUi>> = mapOf(),
) : BaseViewState

internal sealed class EmployeeEvent : BaseEvent {
    data class Init(val organizationId: UID) : EmployeeEvent()
    data class SearchEmployee(val query: String) : EmployeeEvent()
    data class SelectedOrganization(val organization: UID) : EmployeeEvent()
    data class DeleteEmployee(val employeeId: UID) : EmployeeEvent()
    data class NavigateToEditor(val employeeId: UID?) : EmployeeEvent()
    data object NavigateToBack : EmployeeEvent()
}

internal sealed class EmployeeEffect : BaseUiEffect {
    data class ShowError(val failures: InfoFailures) : EmployeeEffect()
    data object NavigateToBack : EmployeeEffect()
    data class NavigateToGlobal(val pushScreen: Screen) : EmployeeEffect()
}

internal sealed class EmployeeAction : BaseAction {
    data class UpdateOrganizations(
        val selectedOrganization: UID?,
        val organizations: List<OrganizationShortUi>
    ) : EmployeeAction()

    data class UpdateEmployees(val employees: Map<Char, List<EmployeeAndSubjectsUi>>) : EmployeeAction()
    data class UpdateSelectedOrganization(val organization: UID) : EmployeeAction()
    data class UpdateLoading(val isLoading: Boolean) : EmployeeAction()
}