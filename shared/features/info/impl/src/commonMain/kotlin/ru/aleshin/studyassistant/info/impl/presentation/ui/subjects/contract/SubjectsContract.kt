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

import androidx.compose.runtime.Immutable
import cafe.adriel.voyager.core.screen.Screen
import dev.icerock.moko.parcelize.Parcelize
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseAction
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseEvent
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseUiEffect
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseViewState
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.info.impl.domain.entities.InfoFailures
import ru.aleshin.studyassistant.info.impl.presentation.models.orgnizations.OrganizationShortUi
import ru.aleshin.studyassistant.info.impl.presentation.models.subjects.SubjectSortedType
import ru.aleshin.studyassistant.info.impl.presentation.models.subjects.SubjectUi

/**
 * @author Stanislav Aleshin on 17.06.2024
 */
@Immutable
@Parcelize
internal data class SubjectsViewState(
    val isLoading: Boolean = true,
    val organizations: List<OrganizationShortUi> = emptyList(),
    val selectedOrganization: UID? = null,
    val subjects: List<SubjectUi> = emptyList(),
    val sortedType: SubjectSortedType = SubjectSortedType.ALPHABETIC,
) : BaseViewState

internal sealed class SubjectsEvent : BaseEvent {
    data class Init(val organizationId: UID) : SubjectsEvent()
    data class SearchSubjects(val query: String) : SubjectsEvent()
    data class SelectedOrganization(val organization: UID) : SubjectsEvent()
    data class SelectedSortedType(val sortedType: SubjectSortedType) : SubjectsEvent()
    data class DeleteSubject(val subjectId: UID) : SubjectsEvent()
    data class NavigateToEditor(val subjectId: UID?) : SubjectsEvent()
    data object NavigateToBack : SubjectsEvent()
}

internal sealed class SubjectsEffect : BaseUiEffect {
    data class ShowError(val failures: InfoFailures) : SubjectsEffect()
    data object NavigateToBack : SubjectsEffect()
    data class NavigateToGlobal(val pushScreen: Screen) : SubjectsEffect()
}

internal sealed class SubjectsAction : BaseAction {
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