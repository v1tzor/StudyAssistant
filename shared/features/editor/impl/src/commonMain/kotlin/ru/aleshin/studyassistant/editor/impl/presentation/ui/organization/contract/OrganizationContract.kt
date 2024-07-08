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
internal data class OrganizationEditorViewState(
    val isLoading: Boolean = true,
    val editableOrganization: EditOrganizationUi? = null,
) : BaseViewState

internal sealed class OrganizationEditorEvent : BaseEvent {
    data class Init(val organizationId: UID?) : OrganizationEditorEvent()
    data class UpdateAvatar(val avatarUrl: String?) : OrganizationEditorEvent()
    data class UpdateType(val organizationType: OrganizationType?) : OrganizationEditorEvent()
    data class UpdateName(val shortName: String?, val fullName: String?) : OrganizationEditorEvent()
    data class UpdateEmails(val emails: List<ContactInfoUi>) : OrganizationEditorEvent()
    data class UpdatePhones(val phones: List<ContactInfoUi>) : OrganizationEditorEvent()
    data class UpdateWebs(val webs: List<ContactInfoUi>) : OrganizationEditorEvent()
    data class UpdateStatus(val isMain: Boolean) : OrganizationEditorEvent()
    data class UpdateLocations(val locations: List<ContactInfoUi>) : OrganizationEditorEvent()
    data object SaveOrganization : OrganizationEditorEvent()
    data object NavigateToBack : OrganizationEditorEvent()
}

internal sealed class OrganizationEditorEffect : BaseUiEffect {
    data class ShowError(val failures: EditorFailures) : OrganizationEditorEffect()
    data object NavigateToBack : OrganizationEditorEffect()
}

internal sealed class OrganizationEditorAction : BaseAction {
    data class SetupEditModel(val editModel: EditOrganizationUi) : OrganizationEditorAction()
    data class UpdateEditModel(val editModel: EditOrganizationUi?) : OrganizationEditorAction()
    data class UpdateLoading(val isLoading: Boolean) : OrganizationEditorAction()
}