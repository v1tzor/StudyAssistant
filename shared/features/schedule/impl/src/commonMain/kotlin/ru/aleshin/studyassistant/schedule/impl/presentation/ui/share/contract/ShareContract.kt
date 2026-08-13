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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.contract

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.architecture.component.BaseInput
import ru.aleshin.studyassistant.core.common.architecture.component.BaseOutput
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreAction
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEffect
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEvent
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreState
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.presentation.models.organizations.OrganizationShortUi
import ru.aleshin.studyassistant.core.presentation.models.schedules.BaseScheduleUi
import ru.aleshin.studyassistant.core.presentation.models.subjects.SubjectUi
import ru.aleshin.studyassistant.core.presentation.models.users.EmployeeUi
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleFailures
import ru.aleshin.studyassistant.schedule.impl.presentation.models.share.OrganizationLinkData
import ru.aleshin.studyassistant.schedule.impl.presentation.models.share.ScheduleShareClaimUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.share.ScheduleShareLinkUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.share.ShareStatus
import kotlin.time.Clock

/**
 * @author Stanislav Aleshin on 08.08.2026.
 */
@Serializable
internal data class ShareState(
    val status: ShareStatus = ShareStatus.INPUT,
    val code: String = "",
    val link: ScheduleShareLinkUi? = null,
    val claim: ScheduleShareClaimUi? = null,
    val rewardChallengeId: String? = null,
    val isRewardInProgress: Boolean = false,
    val isLoadingLinkedOrganization: Boolean = false,
    val currentTime: Instant = Clock.System.now(),
    val allOrganizations: List<OrganizationShortUi> = emptyList(),
    val organizationsLinkData: List<OrganizationLinkData> = emptyList(),
    val linkedSchedules: List<BaseScheduleUi> = emptyList(),
    val maxNumberOfWeek: Int = 1,
) : StoreState

internal sealed class ShareEvent : StoreEvent {
    data class Started(val inputData: ShareInput, val isRestore: Boolean) : ShareEvent()
    data class UpdatedCode(val code: String) : ShareEvent()
    data object CreateShare : ShareEvent()
    data object ClaimShare : ShareEvent()
    data class ScannedCode(val code: String) : ShareEvent()
    data class ClickLinkOrganization(val sharedOrganization: UID, val linkedOrganization: UID?) : ShareEvent()
    data class UpdatedLinkedSubjects(val sharedOrganization: UID, val subjects: Map<UID, SubjectUi>) : ShareEvent()
    data class UpdatedLinkedTeachers(val sharedOrganization: UID, val teachers: Map<UID, EmployeeUi>) : ShareEvent()
    data object AcceptedSharedSchedule : ShareEvent()
    data class RewardedAdGranted(val challengeId: String) : ShareEvent()
    data object RewardedAdUnavailable : ShareEvent()
    data object RejectedSharedSchedule : ShareEvent()
    data object Reset : ShareEvent()
    data object ClickBack : ShareEvent()
}

internal sealed class ShareEffect : StoreEffect {
    data class ShowError(val failures: ScheduleFailures) : ShareEffect()
}

internal sealed class ShareAction : StoreAction {
    data class UpdateCode(val code: String) : ShareAction()
    data class UpdateStatus(val status: ShareStatus) : ShareAction()
    data class SetupLink(val link: ScheduleShareLinkUi) : ShareAction()
    data class SetupClaim(
        val claim: ScheduleShareClaimUi,
        val organizationsLinkData: List<OrganizationLinkData>,
        val linkedSchedules: List<BaseScheduleUi>,
        val maxNumberOfWeek: Int,
    ) : ShareAction()
    data class UpdateLinkData(
        val linkData: List<OrganizationLinkData>,
        val linkedSchedules: List<BaseScheduleUi>,
    ) : ShareAction()
    data class UpdateOrganizations(val organizations: List<OrganizationShortUi>) : ShareAction()
    data class UpdateCurrentTime(val time: Instant) : ShareAction()
    data class UpdateLoadingLinkedOrganization(val isLoading: Boolean) : ShareAction()
    data class UpdateRewardChallenge(
        val challengeId: String?,
        val isInProgress: Boolean,
    ) : ShareAction()
    data object Reset : ShareAction()
}

internal sealed class ShareOutput : BaseOutput {
    data object NavigateToBack : ShareOutput()
}

internal data class ShareInput(
    val code: String?,
) : BaseInput
