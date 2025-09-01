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

package ru.aleshin.studyassistant.info.impl.presentation.ui.subjects.contract

import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.architecture.component.BaseInput
import ru.aleshin.studyassistant.core.common.architecture.component.BaseOutput
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreAction
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEffect
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEvent
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreState
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.editor.api.EditorFeatureComponent.EditorConfig
import ru.aleshin.studyassistant.info.impl.domain.entities.InfoFailures
import ru.aleshin.studyassistant.info.impl.presentation.models.orgnizations.OrganizationShortUi
import ru.aleshin.studyassistant.info.impl.presentation.models.subjects.SubjectSortedType
import ru.aleshin.studyassistant.info.impl.presentation.models.subjects.SubjectUi

/**
 * @author Stanislav Aleshin on 17.06.2024
 */
@Serializable
internal data class SubjectsState(
    val isLoading: Boolean = true,
    val organizations: List<OrganizationShortUi> = emptyList(),
    val selectedOrganization: UID? = null,
    val subjects: List<SubjectUi> = emptyList(),
    val sortedType: SubjectSortedType = SubjectSortedType.ALPHABETIC,
) : StoreState

internal sealed class SubjectsEvent : StoreEvent {
    data class Started(val inputData: SubjectsInput) : SubjectsEvent()
    data class SearchSubjects(val query: String) : SubjectsEvent()
    data class SelectedOrganization(val organization: UID) : SubjectsEvent()
    data class SelectedSortedType(val sortedType: SubjectSortedType) : SubjectsEvent()
    data class ClickDeleteSubject(val subjectId: UID) : SubjectsEvent()
    data class ClickEditSubject(val subjectId: UID?) : SubjectsEvent()
    data object ClickBack : SubjectsEvent()
}

internal sealed class SubjectsEffect : StoreEffect {
    data class ShowError(val failures: InfoFailures) : SubjectsEffect()
}

internal sealed class SubjectsAction : StoreAction {
    data class UpdateSubjects(
        val subjects: List<SubjectUi>,
        val sortedType: SubjectSortedType
    ) : SubjectsAction()

    data class UpdateOrganizations(
        val selectedOrganization: UID?,
        val organizations: List<OrganizationShortUi>
    ) : SubjectsAction()

    data class UpdateSelectedOrganization(val organization: UID) : SubjectsAction()
    data class UpdateLoading(val isLoading: Boolean) : SubjectsAction()
}

internal data class SubjectsInput(
    val organizationId: UID
) : BaseInput

internal sealed class SubjectsOutput : BaseOutput {
    data object NavigateToBack : SubjectsOutput()
    data class NavigateToSubjectEditor(val config: EditorConfig.Subject) : SubjectsOutput()
}