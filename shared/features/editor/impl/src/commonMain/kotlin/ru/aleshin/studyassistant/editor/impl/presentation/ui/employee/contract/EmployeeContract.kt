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
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseAction
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseEvent
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseUiEffect
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseViewState
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.employee.EmployeePost
import ru.aleshin.studyassistant.core.ui.models.ActionWithAvatar
import ru.aleshin.studyassistant.editor.impl.domain.entities.EditorFailures
import ru.aleshin.studyassistant.editor.impl.presentation.models.orgnizations.OrganizationShortUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.ContactInfoUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.EditEmployeeUi

/**
 * @author Stanislav Aleshin on 06.06.2024
 */
@Immutable
@Parcelize
internal data class EmployeeViewState(
    val isLoading: Boolean = true,
    val editableEmployee: EditEmployeeUi? = null,
    val organization: OrganizationShortUi? = null,
    val actionWithAvatar: ActionWithAvatar = ActionWithAvatar.None(null),
) : BaseViewState

internal sealed class EmployeeEvent : BaseEvent {
    data class Init(val employeeId: UID?, val organizationId: UID) : EmployeeEvent()
    data class UpdateAvatar(val image: PlatformFile) : EmployeeEvent()
    data object DeleteAvatar : EmployeeEvent()
    data class UpdateName(val first: String?, val second: String?, val patronymic: String?) : EmployeeEvent()
    data class UpdatePost(val post: EmployeePost?) : EmployeeEvent()
    data class UpdateWorkTime(val start: Instant?, val end: Instant?) : EmployeeEvent()
    data class UpdateBirthday(val date: String?) : EmployeeEvent()
    data class UpdateEmails(val emails: List<ContactInfoUi>) : EmployeeEvent()
    data class UpdatePhones(val phones: List<ContactInfoUi>) : EmployeeEvent()
    data class UpdateWebs(val webs: List<ContactInfoUi>) : EmployeeEvent()
    data class UpdateLocations(val locations: List<ContactInfoUi>) : EmployeeEvent()
    data object SaveEmployee : EmployeeEvent()
    data object NavigateToBack : EmployeeEvent()
}

internal sealed class EmployeeEffect : BaseUiEffect {
    data class ShowError(val failures: EditorFailures) : EmployeeEffect()
    data object NavigateToBack : EmployeeEffect()
}

internal sealed class EmployeeAction : BaseAction {
    data class SetupEditModel(val editModel: EditEmployeeUi) : EmployeeAction()
    data class UpdateEditModel(val editModel: EditEmployeeUi?) : EmployeeAction()
    data class UpdateOrganization(val organization: OrganizationShortUi) : EmployeeAction()
    data class UpdateActionWithAvatar(val actionWithAvatar: ActionWithAvatar) : EmployeeAction()
    data class UpdateLoading(val isLoading: Boolean) : EmployeeAction()
}