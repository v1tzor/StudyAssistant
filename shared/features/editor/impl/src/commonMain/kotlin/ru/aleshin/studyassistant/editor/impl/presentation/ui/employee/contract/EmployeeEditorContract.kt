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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.contract

import architecture.screenmodel.contract.BaseAction
import architecture.screenmodel.contract.BaseEvent
import architecture.screenmodel.contract.BaseUiEffect
import architecture.screenmodel.contract.BaseViewState
import dev.icerock.moko.parcelize.Parcelize
import entities.employee.EmployeePost
import functional.UID
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.editor.impl.domain.entities.EditorFailures
import ru.aleshin.studyassistant.editor.impl.presentation.models.orgnizations.OrganizationUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.ContactInfoUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.EditEmployeeUi

/**
 * @author Stanislav Aleshin on 06.06.2024
 */
@Parcelize
internal data class EmployeeEditorViewState(
    val isLoading: Boolean = true,
    val editableEmployee: EditEmployeeUi? = null,
    val organization: OrganizationUi? = null,
) : BaseViewState

internal sealed class EmployeeEditorEvent : BaseEvent {
    data class Init(val employeeId: UID?, val organizationId: UID) : EmployeeEditorEvent()
    data class UpdateAvatar(val avatarUrl: String?) : EmployeeEditorEvent()
    data class UpdateName(val first: String?, val second: String?, val patronymic: String?) : EmployeeEditorEvent()
    data class SelectPost(val post: EmployeePost?) : EmployeeEditorEvent()
    data class SelectWorkTime(val start: Instant?, val end: Instant?) : EmployeeEditorEvent()
    data class SelectBirthday(val date: String?) : EmployeeEditorEvent()
    data class UpdateEmails(val emails: List<ContactInfoUi>) : EmployeeEditorEvent()
    data class UpdatePhones(val phones: List<ContactInfoUi>) : EmployeeEditorEvent()
    data class UpdateLocations(val locations: List<ContactInfoUi>) : EmployeeEditorEvent()
    data class UpdateWebs(val webs: List<ContactInfoUi>) : EmployeeEditorEvent()
    data object SaveEmployee : EmployeeEditorEvent()
    data object NavigateToBack : EmployeeEditorEvent()
}

internal sealed class EmployeeEditorEffect : BaseUiEffect {
    data class ShowError(val failures: EditorFailures) : EmployeeEditorEffect()
    data object NavigateToBack : EmployeeEditorEffect()
}

internal sealed class EmployeeEditorAction : BaseAction {
    data class SetupEditModel(
        val editableEmployee: EditEmployeeUi,
        val organization: OrganizationUi,
    ) : EmployeeEditorAction()

    data class UpdateLoading(val isLoading: Boolean) : EmployeeEditorAction()
    data class UpdateEditModel(val editableEmployee: EditEmployeeUi?) : EmployeeEditorAction()
}