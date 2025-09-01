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

package ru.aleshin.studyassistant.tasks.impl.presentation.ui.share.contract

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.architecture.component.BaseOutput
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreAction
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEffect
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEvent
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreState
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.editor.api.EditorFeatureComponent.EditorConfig
import ru.aleshin.studyassistant.tasks.impl.domain.entities.TasksFailures
import ru.aleshin.studyassistant.tasks.impl.presentation.models.organization.OrganizationShortUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.schedules.ScheduleUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.share.ReceivedMediatedHomeworksDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.share.SentMediatedHomeworksDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.share.SharedHomeworksDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.subjects.SubjectUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.MediatedHomeworkLinkData
import ru.aleshin.studyassistant.users.api.UsersFeatureComponent.UsersConfig

/**
 * @author Stanislav Aleshin on 18.07.2024
 */
@Serializable
internal data class ShareState(
    val isLoading: Boolean = true,
    val isLoadingLink: Boolean = true,
    val isPaidUser: Boolean = false,
    val currentTime: Instant = Clock.System.now(),
    val sharedHomeworks: SharedHomeworksDetailsUi? = null,
    val organizations: List<OrganizationShortUi> = emptyList(),
    val linkDataList: List<MediatedHomeworkLinkData> = emptyList(),
    val linkSubjects: List<SubjectUi> = emptyList(),
    val linkSchedule: ScheduleUi? = null,
) : StoreState

internal sealed class ShareEvent : StoreEvent {
    data object Started : ShareEvent()
    data class LoadLinkData(val receivedHomeworks: ReceivedMediatedHomeworksDetailsUi?) : ShareEvent()
    data class UpdateLinkData(val linkData: MediatedHomeworkLinkData) : ShareEvent()
    data class LoadLinkSubjects(val organization: UID) : ShareEvent()
    data class AcceptHomework(
        val receivedHomeworks: ReceivedMediatedHomeworksDetailsUi,
        val linkDataList: List<MediatedHomeworkLinkData>,
    ) : ShareEvent()
    data class RejectHomework(val receivedHomeworks: ReceivedMediatedHomeworksDetailsUi) : ShareEvent()
    data class CancelSendHomework(val sentHomeworks: SentMediatedHomeworksDetailsUi) : ShareEvent()
    data class ClickEditSubject(val subjectId: UID?, val organization: UID) : ShareEvent()
    data class ClickUserProfile(val userId: UID) : ShareEvent()
    data object ClickPaidFunction : ShareEvent()
    data object BackClick : ShareEvent()
}

internal sealed class ShareEffect : StoreEffect {
    data class ShowError(val failures: TasksFailures) : ShareEffect()
}

internal sealed class ShareAction : StoreAction {
    data class UpdateSharedHomeworks(val sharedHomeworks: SharedHomeworksDetailsUi?) : ShareAction()
    data class SetupLinkData(
        val linkDataList: List<MediatedHomeworkLinkData>,
        val linkSchedule: ScheduleUi? = null,
    ) : ShareAction()
    data class UpdateLinkData(val linkDataList: List<MediatedHomeworkLinkData>) : ShareAction()
    data class UpdateSubjects(val subjects: List<SubjectUi>) : ShareAction()
    data class UpdateOrganizations(val organizations: List<OrganizationShortUi>) : ShareAction()
    data class UpdateLoading(val isLoading: Boolean) : ShareAction()
    data class UpdateUserPaidStatus(val isPaidUser: Boolean) : ShareAction()
    data class UpdateLinkLoading(val isLoading: Boolean) : ShareAction()
}

internal sealed class ShareOutput : BaseOutput {
    data object NavigateToBack : ShareOutput()
    data object NavigateToBilling : ShareOutput()
    data class NavigateToUserProfile(val config: UsersConfig.UserProfile) : ShareOutput()
    data class NavigateToSubjectEditor(val config: EditorConfig.Subject) : ShareOutput()
}