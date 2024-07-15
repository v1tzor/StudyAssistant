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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.contract

import androidx.compose.runtime.Immutable
import dev.icerock.moko.parcelize.Parcelize
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseAction
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseEvent
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseUiEffect
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseViewState
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.organizations.OrganizationType
import ru.aleshin.studyassistant.editor.impl.domain.entities.EditorFailures
import ru.aleshin.studyassistant.editor.impl.presentation.models.orgnizations.EditOrganizationUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.ContactInfoUi

/**
 * @author Stanislav Aleshin on 08.07.2024
 */
@Immutable
@Parcelize
internal data class OrganizationViewState(
    val isLoading: Boolean = true,
    val editableOrganization: EditOrganizationUi? = null,
) : BaseViewState

internal sealed class OrganizationEvent : BaseEvent {
    data class Init(val organizationId: UID?) : OrganizationEvent()
    data class UpdateAvatar(val avatarUrl: String?) : OrganizationEvent()
    data class UpdateType(val organizationType: OrganizationType?) : OrganizationEvent()
    data class UpdateName(val shortName: String?, val fullName: String?) : OrganizationEvent()
    data class UpdateEmails(val emails: List<ContactInfoUi>) : OrganizationEvent()
    data class UpdatePhones(val phones: List<ContactInfoUi>) : OrganizationEvent()
    data class UpdateWebs(val webs: List<ContactInfoUi>) : OrganizationEvent()
    data class UpdateStatus(val isMain: Boolean) : OrganizationEvent()
    data class UpdateLocations(val locations: List<ContactInfoUi>) : OrganizationEvent()
    data object SaveOrganization : OrganizationEvent()
    data object NavigateToBack : OrganizationEvent()
}

internal sealed class OrganizationEffect : BaseUiEffect {
    data class ShowError(val failures: EditorFailures) : OrganizationEffect()
    data object NavigateToBack : OrganizationEffect()
}

internal sealed class OrganizationAction : BaseAction {
    data class SetupEditModel(val editModel: EditOrganizationUi) : OrganizationAction()
    data class UpdateEditModel(val editModel: EditOrganizationUi?) : OrganizationAction()
    data class UpdateLoading(val isLoading: Boolean) : OrganizationAction()
}