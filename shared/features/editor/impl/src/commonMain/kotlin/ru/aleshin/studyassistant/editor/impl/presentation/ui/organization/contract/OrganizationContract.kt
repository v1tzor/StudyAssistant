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

import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.architecture.component.BaseInput
import ru.aleshin.studyassistant.core.common.architecture.component.BaseOutput
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreAction
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEffect
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEvent
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreState
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.organizations.OrganizationType
import ru.aleshin.studyassistant.core.ui.models.ActionWithAvatar
import ru.aleshin.studyassistant.editor.impl.domain.entities.EditorFailures
import ru.aleshin.studyassistant.editor.impl.presentation.models.orgnizations.EditOrganizationUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.ContactInfoUi

/**
 * @author Stanislav Aleshin on 08.07.2024
 */
@Serializable
internal data class OrganizationState(
    val isLoading: Boolean = true,
    val editableOrganization: EditOrganizationUi? = null,
    val actionWithAvatar: ActionWithAvatar = ActionWithAvatar.None(null),
) : StoreState

internal sealed class OrganizationEvent : StoreEvent {
    data class Started(val inputData: OrganizationInput) : OrganizationEvent()
    data class UpdateAvatar(val image: PlatformFile) : OrganizationEvent()
    data object DeleteAvatar : OrganizationEvent()
    data class UpdateType(val organizationType: OrganizationType?) : OrganizationEvent()
    data class UpdateName(val shortName: String?, val fullName: String?) : OrganizationEvent()
    data class UpdateEmails(val emails: List<ContactInfoUi>) : OrganizationEvent()
    data class UpdatePhones(val phones: List<ContactInfoUi>) : OrganizationEvent()
    data class UpdateWebs(val webs: List<ContactInfoUi>) : OrganizationEvent()
    data class UpdateStatus(val isMain: Boolean) : OrganizationEvent()
    data class UpdateLocations(val locations: List<ContactInfoUi>) : OrganizationEvent()
    data object SaveOrganization : OrganizationEvent()
    data object HideOrganization : OrganizationEvent()
    data object NavigateToBack : OrganizationEvent()
}

internal sealed class OrganizationEffect : StoreEffect {
    data class ShowError(val failures: EditorFailures) : OrganizationEffect()
}

internal sealed class OrganizationAction : StoreAction {
    data class SetupEditModel(val editModel: EditOrganizationUi) : OrganizationAction()
    data class UpdateEditModel(val editModel: EditOrganizationUi?) : OrganizationAction()
    data class UpdateActionWithAvatar(val action: ActionWithAvatar) : OrganizationAction()
    data class UpdateLoading(val isLoading: Boolean) : OrganizationAction()
}

internal data class OrganizationInput(
    val organizationId: UID?
) : BaseInput

internal sealed class OrganizationOutput : BaseOutput {
    data object NavigateToBack : OrganizationOutput()
}