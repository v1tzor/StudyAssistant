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

import androidx.compose.runtime.Immutable
import cafe.adriel.voyager.core.screen.Screen
import dev.icerock.moko.parcelize.Parcelize
import dev.icerock.moko.parcelize.TypeParceler
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseAction
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseEvent
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseUiEffect
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseViewState
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.platform.InstantParceler
import ru.aleshin.studyassistant.core.domain.entities.tasks.TaskPriority
import ru.aleshin.studyassistant.editor.impl.domain.entities.EditorFailures
import ru.aleshin.studyassistant.editor.impl.presentation.models.classes.ClassesForLinkedMapUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.orgnizations.OrganizationShortUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.subjects.SubjectUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.tasks.EditHomeworkUi

/**
 * @author Stanislav Aleshin on 22.06.2024
 */
@Immutable
@Parcelize
internal data class HomeworkViewState(
    val isLoading: Boolean = true,
    val isClassesLoading: Boolean = true,
    val editableHomework: EditHomeworkUi? = null,
    val organizations: List<OrganizationShortUi> = emptyList(),
    val subjects: List<SubjectUi> = emptyList(),
    @TypeParceler<Instant, InstantParceler>
    val classesForLinking: ClassesForLinkedMapUi = emptyMap(),
) : BaseViewState

internal sealed class HomeworkEvent : BaseEvent {
    data class Init(
        val homeworkId: UID?,
        val date: Long?,
        val subjectId: UID?,
        val organizationId: UID?,
    ) : HomeworkEvent()

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

internal sealed class HomeworkEffect : BaseUiEffect {
    data class ShowError(val failures: EditorFailures) : HomeworkEffect()
    data class NavigateToLocal(val pushScreen: Screen) : HomeworkEffect()
    data object NavigateToBack : HomeworkEffect()
}

internal sealed class HomeworkAction : BaseAction {
    data class SetupEditModel(val editModel: EditHomeworkUi) : HomeworkAction()
    class UpdateEditModel(val editModel: EditHomeworkUi?) : HomeworkAction()
    data class UpdateOrganizations(val organizations: List<OrganizationShortUi>) : HomeworkAction()
    data class UpdateSubjects(val subjects: List<SubjectUi>) : HomeworkAction()
    data class UpdateClassesForLinked(val classes: ClassesForLinkedMapUi) : HomeworkAction()
    data class UpdateLoading(val isLoading: Boolean) : HomeworkAction()
    data class UpdateClassesLoading(val isLoading: Boolean) : HomeworkAction()
}