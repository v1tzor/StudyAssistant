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

import androidx.compose.runtime.Immutable
import cafe.adriel.voyager.core.screen.Screen
import dev.icerock.moko.parcelize.Parcelize
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseAction
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseEvent
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseUiEffect
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseViewState
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.info.impl.domain.entities.InfoFailures
import ru.aleshin.studyassistant.info.impl.presentation.models.orgnizations.OrganizationClassesInfoUi
import ru.aleshin.studyassistant.info.impl.presentation.models.orgnizations.OrganizationShortUi
import ru.aleshin.studyassistant.info.impl.presentation.models.orgnizations.OrganizationUi

/**
 * @author Stanislav Aleshin on 16.06.2024
 */
@Immutable
@Parcelize
internal data class OrganizationsViewState(
    val isLoading: Boolean = true,
    val isPaidUser: Boolean = false,
    val shortOrganizations: List<OrganizationShortUi>? = null,
    val organizationData: OrganizationUi? = null,
    val classesInfo: OrganizationClassesInfoUi? = null,
) : BaseViewState

internal sealed class OrganizationsEvent : BaseEvent {
    data object Init : OrganizationsEvent()
    data class Refresh(val organizationId: UID?) : OrganizationsEvent()
    data class ChangeOrganization(val organizationId: UID?) : OrganizationsEvent()
    data class OpenEmployeeProfile(val employeeId: UID) : OrganizationsEvent()
    data class NavigateToEmployees(val organizationId: UID) : OrganizationsEvent()
    data class NavigateToSubjects(val organizationId: UID) : OrganizationsEvent()
    data class NavigateToOrganizationEditor(val organizationId: UID?) : OrganizationsEvent()
    data class NavigateToSubjectEditor(val subjectId: UID, val organizationId: UID) : OrganizationsEvent()
    data object NavigateToBilling : OrganizationsEvent()
}

internal sealed class OrganizationsEffect : BaseUiEffect {
    data class ShowError(val failures: InfoFailures) : OrganizationsEffect()
    data class NavigateToLocal(val pushScreen: Screen) : OrganizationsEffect()
    data class NavigateToGlobal(val pushScreen: Screen) : OrganizationsEffect()
}

internal sealed class OrganizationsAction : BaseAction {
    data class UpdateShortOrganizations(val organizations: List<OrganizationShortUi>) : OrganizationsAction()
    data class UpdatePaidUserStatus(val isPaidUser: Boolean) : OrganizationsAction()
    data class UpdateOrganizationData(
        val data: OrganizationUi?,
        val classesInfo: OrganizationClassesInfoUi?,
    ) : OrganizationsAction()
    data class UpdateLoading(val isLoading: Boolean) : OrganizationsAction()
}