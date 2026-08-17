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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.contract

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
import ru.aleshin.studyassistant.core.presentation.models.users.EmployeeUi
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleFailures
import ru.aleshin.studyassistant.schedule.impl.platform.CompressedScheduleImage
import ru.aleshin.studyassistant.schedule.impl.presentation.models.importing.ScheduleImportClassUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.importing.ScheduleImportSessionUi

/**
 * @author Stanislav Aleshin on 16.08.2026.
 */
@Serializable
internal data class ImportState(
    val isLoadingPhoto: Boolean = false,
    val isAnalysisInProgress: Boolean = false,
    val isRewardInProgress: Boolean = false,
    val isApplied: Boolean = false,
    var preparedImage: CompressedScheduleImage? = null,
    val note: String = "",
    val selectedOrganization: OrganizationShortUi? = null,
    val organizations: List<OrganizationShortUi> = emptyList(),
    val requestId: UID? = null,
    val session: ScheduleImportSessionUi? = null,
    val rewardChallengeId: String? = null,
) : StoreState

internal sealed class ImportEvent : StoreEvent {
    data object Started : ImportEvent()
    data class SelectedPhoto(val imageBytes: ByteArray) : ImportEvent()
    data object ImageSelectionFailed : ImportEvent()
    data class UpdateNote(val note: String) : ImportEvent()
    data class SelectOrganization(val organization: OrganizationShortUi?) : ImportEvent()
    data object ExtractDraft : ImportEvent()
    data class UpdateClass(val classModel: ScheduleImportClassUi) : ImportEvent()
    data class UpdateSubject(val subject: SubjectUi) : ImportEvent()
    data class UpdateEmployee(val employee: EmployeeUi) : ImportEvent()
    data class AssignSubject(val classId: UID, val subjectId: UID?) : ImportEvent()
    data class AssignTeacher(val classId: UID, val teacherId: UID?) : ImportEvent()
    data class AddSubject(val name: String) : ImportEvent()
    data class AddEmployee(val firstName: String) : ImportEvent()
    data class DeleteClass(val classId: UID) : ImportEvent()
    data class DeleteSubject(val subjectId: UID) : ImportEvent()
    data class DeleteEmployee(val employeeId: UID) : ImportEvent()
    data class ReorderDayClasses(
        val dayOfWeek: Int,
        val repeatWeek: Int,
        val orderedIds: List<UID>,
    ) : ImportEvent()
    data object ApplySession : ImportEvent()
    data class RewardedAdGranted(val challengeId: String) : ImportEvent()
    data object RewardedAdUnavailable : ImportEvent()
    data object ReconcileReward : ImportEvent()
    data object EditSource : ImportEvent()
    data object ClickBack : ImportEvent()
    data object ClickAddOrganization : ImportEvent()
}

internal sealed class ImportEffect : StoreEffect {
    data class ShowError(val failure: ScheduleFailures) : ImportEffect()
}

internal sealed class ImportAction : StoreAction {
    data class UpdateLoadingPhoto(val isLoading: Boolean) : ImportAction()
    data class UpdateAnalysisProgress(val isLoading: Boolean) : ImportAction()
    data class UpdatePhoto(val preparedImage: CompressedScheduleImage?) : ImportAction()
    data class UpdateNote(val note: String) : ImportAction()
    data class UpdateSelectedOrganization(val organization: OrganizationShortUi?) : ImportAction()
    data class SetupOrganizations(val organizations: List<OrganizationShortUi>) : ImportAction()
    data class SetupSession(
        val session: ScheduleImportSessionUi?,
        val requestId: UID?,
    ) : ImportAction()
    data class UpdateApplied(val isApplied: Boolean) : ImportAction()
    data class UpdateRewardChallenge(
        val challengeId: String?,
        val isInProgress: Boolean,
    ) : ImportAction()
}

internal sealed class ImportOutput : BaseOutput {
    data object NavigateToBack : ImportOutput()
    data class NavigateToOrganizationEditor(val organizationId: UID?) : ImportOutput()
}

internal data object ImportInput : BaseInput
