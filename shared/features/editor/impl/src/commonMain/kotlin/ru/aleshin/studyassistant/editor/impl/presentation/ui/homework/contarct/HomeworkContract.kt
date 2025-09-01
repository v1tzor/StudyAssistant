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

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.architecture.component.BaseInput
import ru.aleshin.studyassistant.core.common.architecture.component.BaseOutput
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreAction
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEffect
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEvent
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreState
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.tasks.TaskPriority
import ru.aleshin.studyassistant.editor.api.EditorFeatureComponent.EditorConfig
import ru.aleshin.studyassistant.editor.impl.domain.entities.EditorFailures
import ru.aleshin.studyassistant.editor.impl.presentation.models.classes.ClassesForLinkedMapUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.orgnizations.OrganizationShortUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.subjects.SubjectUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.tasks.EditHomeworkUi

/**
 * @author Stanislav Aleshin on 22.06.2024
 */
@Serializable
internal data class HomeworkState(
    val isLoading: Boolean = true,
    val isLoadingSave: Boolean = false,
    val isClassesLoading: Boolean = true,
    val editableHomework: EditHomeworkUi? = null,
    val showDeleteAction: Boolean = false,
    val currentDate: Instant = Clock.System.now().startThisDay(),
    val organizations: List<OrganizationShortUi> = emptyList(),
    val subjects: List<SubjectUi> = emptyList(),
    val classesForLinking: ClassesForLinkedMapUi = emptyMap(),
) : StoreState

internal sealed class HomeworkEvent : StoreEvent {
    data class Started(val inputData: HomeworkInput, val isRestore: Boolean) : HomeworkEvent()

    data class UpdateOrganization(val organization: OrganizationShortUi?) : HomeworkEvent()
    data class UpdateSubject(val subject: SubjectUi?) : HomeworkEvent()
    data class UpdateDate(val date: Instant?) : HomeworkEvent()
    data class UpdateLinkedClass(val classId: UID?, val date: Instant?) : HomeworkEvent()
    data class UpdateTask(val theory: String, val practice: String, val presentations: String) : HomeworkEvent()
    data class UpdateTestTopic(val isTest: Boolean, val topic: String) : HomeworkEvent()
    data class UpdatePriority(val priority: TaskPriority) : HomeworkEvent()
    data object DeleteHomework : HomeworkEvent()
    data object SaveHomework : HomeworkEvent()
    data class NavigateToOrganizationEditor(val organizationId: UID?) : HomeworkEvent()
    data class NavigateToSubjectEditor(val subjectId: UID?) : HomeworkEvent()
    data object NavigateToBack : HomeworkEvent()
}

internal sealed class HomeworkEffect : StoreEffect {
    data class ShowError(val failures: EditorFailures) : HomeworkEffect()
}

internal sealed class HomeworkAction : StoreAction {
    data class SetupEditModel(val editModel: EditHomeworkUi, val showDeleteAction: Boolean) : HomeworkAction()
    class UpdateEditModel(val editModel: EditHomeworkUi?) : HomeworkAction()
    data class UpdateOrganizations(val organizations: List<OrganizationShortUi>) : HomeworkAction()
    data class UpdateSubjects(val subjects: List<SubjectUi>) : HomeworkAction()
    data class UpdateClassesForLinked(val classes: ClassesForLinkedMapUi) : HomeworkAction()
    data class UpdateCurrentDate(val date: Instant) : HomeworkAction()
    data class UpdateLoading(val isLoading: Boolean) : HomeworkAction()
    data class UpdateLoadingSave(val isLoading: Boolean) : HomeworkAction()
    data class UpdateClassesLoading(val isLoading: Boolean) : HomeworkAction()
}

internal data class HomeworkInput(
    val homeworkId: UID?,
    val date: Long?,
    val subjectId: UID?,
    val organizationId: UID?,
) : BaseInput

internal sealed class HomeworkOutput : BaseOutput {
    data object NavigateToBack : HomeworkOutput()
    data class NavigateToSubjectEditor(val config: EditorConfig.Subject) : HomeworkOutput()
    data class NavigateToOrganizationEditor(val config: EditorConfig.Organization) : HomeworkOutput()
}