/*
 * Copyright 2026 Stanislav Aleshin
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

package ru.aleshin.studyassistant.tasks.impl.presentation.ui.share.contract

import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.architecture.component.BaseInput
import ru.aleshin.studyassistant.core.common.architecture.component.BaseOutput
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreAction
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEffect
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEvent
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreState
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.presentation.models.organizations.OrganizationShortUi
import ru.aleshin.studyassistant.core.presentation.models.subjects.SubjectUi
import ru.aleshin.studyassistant.editor.api.EditorConfig
import ru.aleshin.studyassistant.tasks.impl.domain.entities.TasksFailures
import ru.aleshin.studyassistant.tasks.impl.presentation.models.schedules.ScheduleUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.share.HomeworkShareStatus
import ru.aleshin.studyassistant.tasks.impl.presentation.models.share.HomeworkShareUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.MediatedHomeworkLinkData

/**
 * @author Stanislav Aleshin on 08.08.2026.
 */
@Serializable
internal data class ShareState(
    val status: HomeworkShareStatus = HomeworkShareStatus.INPUT,
    val code: String = "",
    val share: HomeworkShareUi? = null,
    val organizations: List<OrganizationShortUi> = emptyList(),
    val linkDataList: List<MediatedHomeworkLinkData> = emptyList(),
    val linkSubjects: List<SubjectUi> = emptyList(),
    val linkSchedule: ScheduleUi? = null,
) : StoreState

internal sealed class ShareEvent : StoreEvent {
    data class Started(val input: ShareInput, val isRestore: Boolean) : ShareEvent()
    data class UpdatedCode(val code: String) : ShareEvent()
    data object FetchShare : ShareEvent()
    data class ScannedCode(val code: String) : ShareEvent()
    data class UpdateLinkData(val linkData: MediatedHomeworkLinkData) : ShareEvent()
    data class LoadLinkSubjects(val organization: UID) : ShareEvent()
    data object AcceptHomework : ShareEvent()
    data class ClickEditSubject(val subjectId: UID?, val organization: UID) : ShareEvent()
    data object Reset : ShareEvent()
    data object BackClick : ShareEvent()
}

internal sealed class ShareEffect : StoreEffect {
    data class ShowError(val failures: TasksFailures) : ShareEffect()
}

internal sealed class ShareAction : StoreAction {
    data class UpdateCode(val code: String) : ShareAction()
    data class UpdateStatus(val status: HomeworkShareStatus) : ShareAction()
    data class SetupShare(
        val share: HomeworkShareUi,
        val linkDataList: List<MediatedHomeworkLinkData>,
        val linkSchedule: ScheduleUi?,
    ) : ShareAction()

    data class UpdateLinkData(val linkDataList: List<MediatedHomeworkLinkData>) : ShareAction()
    data class UpdateSubjects(val subjects: List<SubjectUi>) : ShareAction()
    data class UpdateOrganizations(val organizations: List<OrganizationShortUi>) : ShareAction()
    data object Reset : ShareAction()
}

internal sealed class ShareOutput : BaseOutput {
    data object NavigateToBack : ShareOutput()
    data class NavigateToSubjectEditor(val config: EditorConfig.Subject) : ShareOutput()
}

internal data class ShareInput(val code: String?) : BaseInput
