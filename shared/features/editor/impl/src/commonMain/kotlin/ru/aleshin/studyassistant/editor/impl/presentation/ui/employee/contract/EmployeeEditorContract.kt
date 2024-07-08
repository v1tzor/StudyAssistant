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

import androidx.compose.runtime.Immutable
import dev.icerock.moko.parcelize.Parcelize
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseAction
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseEvent
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseUiEffect
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseViewState
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.employee.EmployeePost
import ru.aleshin.studyassistant.editor.impl.domain.entities.EditorFailures
import ru.aleshin.studyassistant.editor.impl.presentation.models.orgnizations.OrganizationShortUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.ContactInfoUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.EditEmployeeUi

/**
 * @author Stanislav Aleshin on 06.06.2024
 */
@Immutable
@Parcelize
internal data class EmployeeEditorViewState(
    val isLoading: Boolean = true,
    val editableEmployee: EditEmployeeUi? = null,
    val organization: OrganizationShortUi? = null,
) : BaseViewState

internal sealed class EmployeeEditorEvent : BaseEvent {
    data class Init(val employeeId: UID?, val organizationId: UID) : EmployeeEditorEvent()
    data class UpdateAvatar(val avatarUrl: String?) : EmployeeEditorEvent()
    data class UpdateName(val first: String?, val second: String?, val patronymic: String?) : EmployeeEditorEvent()
    data class UpdatePost(val post: EmployeePost?) : EmployeeEditorEvent()
    data class UpdateWorkTime(val start: Instant?, val end: Instant?) : EmployeeEditorEvent()
    data class UpdateBirthday(val date: String?) : EmployeeEditorEvent()
    data class UpdateEmails(val emails: List<ContactInfoUi>) : EmployeeEditorEvent()
    data class UpdatePhones(val phones: List<ContactInfoUi>) : EmployeeEditorEvent()
    data class UpdateWebs(val webs: List<ContactInfoUi>) : EmployeeEditorEvent()
    data class UpdateLocations(val locations: List<ContactInfoUi>) : EmployeeEditorEvent()
    data object SaveEmployee : EmployeeEditorEvent()
    data object NavigateToBack : EmployeeEditorEvent()
}

internal sealed class EmployeeEditorEffect : BaseUiEffect {
    data class ShowError(val failures: EditorFailures) : EmployeeEditorEffect()
    data object NavigateToBack : EmployeeEditorEffect()
}

internal sealed class EmployeeEditorAction : BaseAction {
    data class SetupEditModel(
        val editModel: EditEmployeeUi,
        val organization: OrganizationShortUi,
    ) : EmployeeEditorAction()

    data class UpdateEditModel(val editModel: EditEmployeeUi?) : EmployeeEditorAction()
    data class UpdateLoading(val isLoading: Boolean) : EmployeeEditorAction()
}