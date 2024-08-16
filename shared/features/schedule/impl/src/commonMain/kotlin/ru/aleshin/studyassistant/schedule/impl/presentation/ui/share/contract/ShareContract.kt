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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.contract

import androidx.compose.runtime.Immutable
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
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleFailures
import ru.aleshin.studyassistant.schedule.impl.presentation.models.organization.OrganizationShortUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.schedule.BaseScheduleUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.share.OrganizationLinkData
import ru.aleshin.studyassistant.schedule.impl.presentation.models.share.ReceivedMediatedSchedulesUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.subjects.SubjectUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.users.AppUserUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.users.EmployeeUi

/**
 * @author Stanislav Aleshin on 16.08.2024
 */
@Immutable
@Parcelize
internal data class ShareViewState(
    val isLoading: Boolean = true,
    val isLoadingAccept: Boolean = false,
    val isLoadingLinkedOrganization: Boolean = false,
    @TypeParceler<Instant, InstantParceler>
    val currentTime: Instant = Clock.System.now(),
    val receivedMediatedSchedule: ReceivedMediatedSchedulesUi? = null,
    val allOrganizations: List<OrganizationShortUi> = emptyList(),
    val organizationsLinkData: List<OrganizationLinkData> = emptyList(),
    val linkedSchedules: List<BaseScheduleUi> = emptyList(),
) : BaseViewState

internal sealed class ShareEvent : BaseEvent {
    data class Init(val shareId: UID) : ShareEvent()
    data class LinkOrganization(val sharedOrganization: UID, val linkedOrganization: UID?) : ShareEvent()
    data class UpdateLinkedSubjects(val sharedOrganization: UID, val subjects: Map<UID, SubjectUi>) : ShareEvent()
    data class UpdateLinkedTeachers(val sharedOrganization: UID, val teachers: Map<UID, EmployeeUi>) : ShareEvent()
    data object AcceptSharedSchedule : ShareEvent()
    data object RejectSharedSchedule : ShareEvent()
    data class NavigateToUserProfile(val user: AppUserUi) : ShareEvent()
    data object NavigateToBack : ShareEvent()
}

internal sealed class ShareEffect : BaseUiEffect {
    data class ShowError(val failures: ScheduleFailures) : ShareEffect()
    data object NavigateToBack : ShareEffect()
    data class NavigateToGlobal(val pushScreen: Screen) : ShareEffect()
}

internal sealed class ShareAction : BaseAction {

    data class SetupSharedSchedules(
        val receivedMediatedSchedule: ReceivedMediatedSchedulesUi?,
        val organizationsLinkData: List<OrganizationLinkData>,
        val linkedSchedules: List<BaseScheduleUi>,
    ) : ShareAction()

    data class UpdateLinkData(
        val linkData: List<OrganizationLinkData>,
        val linkedSchedules: List<BaseScheduleUi>
    ) : ShareAction()

    data class UpdateOrganizations(val organizations: List<OrganizationShortUi>) : ShareAction()
    data class UpdateCurrentTime(val time: Instant) : ShareAction()
    data class UpdateLoading(val isLoading: Boolean) : ShareAction()
    data class UpdateLoadingAccept(val isLoading: Boolean) : ShareAction()
    data class UpdateLoadingLinkedOrganization(val isLoading: Boolean) : ShareAction()
}