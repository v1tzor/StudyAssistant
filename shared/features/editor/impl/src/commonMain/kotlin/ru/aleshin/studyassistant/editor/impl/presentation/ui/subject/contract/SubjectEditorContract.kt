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

import architecture.screenmodel.contract.BaseAction
import architecture.screenmodel.contract.BaseEvent
import architecture.screenmodel.contract.BaseUiEffect
import architecture.screenmodel.contract.BaseViewState
import cafe.adriel.voyager.core.screen.Screen
import dev.icerock.moko.parcelize.Parcelize
import entities.subject.EventType
import functional.UID
import ru.aleshin.studyassistant.editor.impl.domain.entities.EditorFailures
import ru.aleshin.studyassistant.editor.impl.presentation.models.orgnizations.OrganizationUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.subjects.EditSubjectUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.ContactInfoUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.EmployeeUi

/**
 * @author Stanislav Aleshin on 04.06.2024
 */
@Parcelize
internal data class SubjectEditorViewState(
    val isLoading: Boolean = true,
    val editableSubject: EditSubjectUi? = null,
    val organization: OrganizationUi? = null,
) : BaseViewState

internal sealed class SubjectEditorEvent : BaseEvent {
    data class Init(val subjectId: UID?, val organizationId: UID) : SubjectEditorEvent()
    data class UpdateLocations(val locations: List<ContactInfoUi>) : SubjectEditorEvent()
    data class UpdateOffices(val offices: List<String>) : SubjectEditorEvent()
    data class EditName(val name: String) : SubjectEditorEvent()
    data class SelectEventType(val type: EventType?) : SubjectEditorEvent()
    data class PickColor(val color: Int?) : SubjectEditorEvent()
    data class SelectTeacher(val teacher: EmployeeUi?) : SubjectEditorEvent()
    data class SelectLocation(val location: ContactInfoUi?, val office: String?) : SubjectEditorEvent()
    data object NavigateToEmployeeEditor : SubjectEditorEvent()
    data object SaveSubject : SubjectEditorEvent()
    data object NavigateToBack : SubjectEditorEvent()
}

internal sealed class SubjectEditorEffect : BaseUiEffect {
    data class ShowError(val failures: EditorFailures) : SubjectEditorEffect()
    data class NavigateToLocal(val pushScreen: Screen) : SubjectEditorEffect()
    data object NavigateToBack : SubjectEditorEffect()
}

internal sealed class SubjectEditorAction : BaseAction {
    data class SetupEditModel(
        val editableSubject: EditSubjectUi,
        val organization: OrganizationUi,
    ) : SubjectEditorAction()

    data class UpdateLoading(val isLoading: Boolean) : SubjectEditorAction()
    data class UpdateEditModel(val editableSubject: EditSubjectUi?) : SubjectEditorAction()
}
