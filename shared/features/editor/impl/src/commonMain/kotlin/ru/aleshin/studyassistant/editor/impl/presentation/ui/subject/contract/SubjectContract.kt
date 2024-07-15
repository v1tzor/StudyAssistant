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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.contract

import androidx.compose.runtime.Immutable
import cafe.adriel.voyager.core.screen.Screen
import dev.icerock.moko.parcelize.Parcelize
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseAction
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseEvent
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseUiEffect
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseViewState
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.subject.EventType
import ru.aleshin.studyassistant.editor.impl.domain.entities.EditorFailures
import ru.aleshin.studyassistant.editor.impl.presentation.models.orgnizations.OrganizationShortUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.subjects.EditSubjectUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.ContactInfoUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.EmployeeDetailsUi

/**
 * @author Stanislav Aleshin on 04.06.2024
 */
@Immutable
@Parcelize
internal data class SubjectViewState(
    val isLoading: Boolean = true,
    val editableSubject: EditSubjectUi? = null,
    val organization: OrganizationShortUi? = null,
    val employees: List<EmployeeDetailsUi> = emptyList(),
) : BaseViewState

internal sealed class SubjectEvent : BaseEvent {
    data class Init(val subjectId: UID?, val organizationId: UID) : SubjectEvent()
    data class SelectEventType(val type: EventType?) : SubjectEvent()
    data class EditName(val name: String) : SubjectEvent()
    data class UpdateColor(val color: Int?) : SubjectEvent()
    data class UpdateTeacher(val teacher: EmployeeDetailsUi?) : SubjectEvent()
    data class UpdateLocation(val location: ContactInfoUi?, val office: String?) : SubjectEvent()
    data class UpdateOrganizationLocations(val locations: List<ContactInfoUi>) : SubjectEvent()
    data class UpdateOrganizationOffices(val offices: List<String>) : SubjectEvent()
    data object SaveSubject : SubjectEvent()
    data class NavigateToEmployeeEditor(val employeeId: UID?) : SubjectEvent()
    data object NavigateToBack : SubjectEvent()
}

internal sealed class SubjectEffect : BaseUiEffect {
    data class ShowError(val failures: EditorFailures) : SubjectEffect()
    data class NavigateToLocal(val pushScreen: Screen) : SubjectEffect()
    data object NavigateToBack : SubjectEffect()
}

internal sealed class SubjectAction : BaseAction {
    data class SetupEditModel(val editModel: EditSubjectUi, val organization: OrganizationShortUi) : SubjectAction()
    data class UpdateEditModel(val editModel: EditSubjectUi?) : SubjectAction()
    data class UpdateEmployees(val employees: List<EmployeeDetailsUi>) : SubjectAction()
    data class UpdateLoading(val isLoading: Boolean) : SubjectAction()
}