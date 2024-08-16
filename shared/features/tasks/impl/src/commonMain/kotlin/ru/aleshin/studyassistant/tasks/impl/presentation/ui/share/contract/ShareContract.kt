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

import cafe.adriel.voyager.core.screen.Screen
import dev.icerock.moko.parcelize.Parcelize
import dev.icerock.moko.parcelize.TypeParceler
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseAction
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseEvent
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseUiEffect
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseViewState
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.platform.InstantParceler
import ru.aleshin.studyassistant.tasks.impl.domain.entities.TasksFailures
import ru.aleshin.studyassistant.tasks.impl.presentation.models.organization.OrganizationShortUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.schedules.ScheduleUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.share.ReceivedMediatedHomeworksDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.share.SentMediatedHomeworksDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.share.SharedHomeworksDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.subjects.SubjectUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.MediatedHomeworkLinkData

/**
 * @author Stanislav Aleshin on 18.07.2024
 */
@Parcelize
internal data class ShareViewState(
    val isLoading: Boolean = true,
    val isLoadingLink: Boolean = true,
    @TypeParceler<Instant, InstantParceler>
    val currentTime: Instant = Clock.System.now(),
    val sharedHomeworks: SharedHomeworksDetailsUi? = null,
    val organizations: List<OrganizationShortUi> = emptyList(),
    val linkDataList: List<MediatedHomeworkLinkData> = emptyList(),
    val linkSubjects: List<SubjectUi> = emptyList(),
    val linkSchedule: ScheduleUi? = null,
) : BaseViewState

internal sealed class ShareEvent : BaseEvent {
    data object Init : ShareEvent()
    data class LoadLinkData(val receivedHomeworks: ReceivedMediatedHomeworksDetailsUi?) : ShareEvent()
    data class UpdateLinkData(val linkData: MediatedHomeworkLinkData) : ShareEvent()
    data class LoadLinkSubjects(val organization: UID) : ShareEvent()
    data class AcceptHomework(
        val receivedHomeworks: ReceivedMediatedHomeworksDetailsUi,
        val linkDataList: List<MediatedHomeworkLinkData>,
    ) : ShareEvent()
    data class RejectHomework(val receivedHomeworks: ReceivedMediatedHomeworksDetailsUi) : ShareEvent()
    data class CancelSendHomework(val sentHomeworks: SentMediatedHomeworksDetailsUi) : ShareEvent()
    data class NavigateToSubjectEditor(val subjectId: UID?, val organization: UID) : ShareEvent()
    data class NavigateToUserProfile(val userId: UID) : ShareEvent()
    data object NavigateToBack : ShareEvent()
}

internal sealed class ShareEffect : BaseUiEffect {
    data class ShowError(val failures: TasksFailures) : ShareEffect()
    data class NavigateToGlobal(val screen: Screen) : ShareEffect()
    data object NavigateToBack : ShareEffect()
}

internal sealed class ShareAction : BaseAction {
    data class UpdateSharedHomeworks(val sharedHomeworks: SharedHomeworksDetailsUi?) : ShareAction()
    data class SetupLinkData(
        val linkDataList: List<MediatedHomeworkLinkData>,
        val linkSchedule: ScheduleUi? = null,
    ) : ShareAction()
    data class UpdateLinkData(val linkDataList: List<MediatedHomeworkLinkData>) : ShareAction()
    data class UpdateSubjects(val subjects: List<SubjectUi>) : ShareAction()
    data class UpdateOrganizations(val organizations: List<OrganizationShortUi>) : ShareAction()
    data class UpdateLoading(val isLoading: Boolean) : ShareAction()
    data class UpdateLinkLoading(val isLoading: Boolean) : ShareAction()
}