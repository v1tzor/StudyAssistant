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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.contarct

import architecture.screenmodel.contract.BaseAction
import architecture.screenmodel.contract.BaseEvent
import architecture.screenmodel.contract.BaseUiEffect
import architecture.screenmodel.contract.BaseViewState
import cafe.adriel.voyager.core.screen.Screen
import dev.icerock.moko.parcelize.Parcelize
import dev.icerock.moko.parcelize.TypeParceler
import entities.tasks.TaskPriority
import functional.UID
import kotlinx.datetime.Instant
import platform.InstantParceler
import ru.aleshin.studyassistant.editor.impl.domain.entities.EditorFailures
import ru.aleshin.studyassistant.editor.impl.presentation.models.classes.ClassesForLinkedUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.orgnizations.OrganizationShortUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.subjects.SubjectUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.tasks.EditHomeworkUi

/**
 * @author Stanislav Aleshin on 22.06.2024
 */
@Parcelize
internal data class HomeworkEditorViewState(
    val isLoading: Boolean = true,
    val isClassesLoading: Boolean = true,
    val editableHomework: EditHomeworkUi? = null,
    val organizations: List<OrganizationShortUi> = emptyList(),
    val subjects: List<SubjectUi> = emptyList(),
    @TypeParceler<Instant, InstantParceler>
    val classesForLinking: ClassesForLinkedUi = emptyMap(),
) : BaseViewState

internal sealed class HomeworkEditorEvent : BaseEvent {
    data class Init(
        val homeworkId: UID?,
        val date: Long?,
        val subjectId: UID?,
        val organizationId: UID?,
    ) : HomeworkEditorEvent()

    data class UpdateOrganization(val organization: OrganizationShortUi?) : HomeworkEditorEvent()
    data class UpdateSubject(val subject: SubjectUi?) : HomeworkEditorEvent()
    data class UpdateDate(val date: Instant?) : HomeworkEditorEvent()
    data class UpdateLinkedClass(val classId: UID?, val date: Instant?) : HomeworkEditorEvent()
    data class UpdateTask(val theory: String, val practice: String, val presentations: String) : HomeworkEditorEvent()
    data class UpdateTestTopic(val isTest: Boolean, val topic: String) : HomeworkEditorEvent()
    data class UpdatePriority(val priority: TaskPriority) : HomeworkEditorEvent()
    data object DeleteHomework : HomeworkEditorEvent()
    data object SaveHomework : HomeworkEditorEvent()
    data class NavigateToOrganizationEditor(val organizationId: UID?) : HomeworkEditorEvent()
    data class NavigateToSubjectEditor(val subjectId: UID?) : HomeworkEditorEvent()
    data object NavigateToBack : HomeworkEditorEvent()
}

internal sealed class HomeworkEditorEffect : BaseUiEffect {
    data class ShowError(val failures: EditorFailures) : HomeworkEditorEffect()
    data class NavigateToLocal(val pushScreen: Screen) : HomeworkEditorEffect()
    data object NavigateToBack : HomeworkEditorEffect()
}

internal sealed class HomeworkEditorAction : BaseAction {
    data class SetupEditModel(val editModel: EditHomeworkUi) : HomeworkEditorAction()
    class UpdateEditModel(val editModel: EditHomeworkUi?) : HomeworkEditorAction()
    data class UpdateOrganizations(val organizations: List<OrganizationShortUi>) : HomeworkEditorAction()
    data class UpdateSubjects(val subjects: List<SubjectUi>) : HomeworkEditorAction()
    data class UpdateClassesForLinked(val classes: ClassesForLinkedUi) : HomeworkEditorAction()
    data class UpdateLoading(val isLoading: Boolean) : HomeworkEditorAction()
    data class UpdateClassesLoading(val isLoading: Boolean) : HomeworkEditorAction()
}