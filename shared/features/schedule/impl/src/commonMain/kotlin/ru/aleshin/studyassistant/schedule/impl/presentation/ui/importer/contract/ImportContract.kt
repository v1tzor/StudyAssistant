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
import ru.aleshin.studyassistant.schedule.impl.presentation.models.importing.ScheduleImportDraftUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.importing.ScheduleImportEntryUi

/**
 * @author Stanislav Aleshin on 16.08.2026.
 */
@Serializable
internal data class ImportState(
    val hasPhoto: Boolean = false,
    val note: String = "",
    val organization: OrganizationShortUi? = null,
    val organizations: List<OrganizationShortUi> = emptyList(),
    val subjects: List<SubjectUi> = emptyList(),
    val employees: List<EmployeeUi> = emptyList(),
    val requestId: UID? = null,
    val draft: ScheduleImportDraftUi? = null,
    val isLoading: Boolean = false,
    val isApplied: Boolean = false,
    val rewardChallengeId: String? = null,
    val isRewardInProgress: Boolean = false,
) : StoreState

internal sealed class ImportEvent : StoreEvent {
    data object Started : ImportEvent()
    data class SelectedPhoto(val imageBytes: ByteArray) : ImportEvent()
    data object ImageSelectionFailed : ImportEvent()
    data class UpdateNote(val note: String) : ImportEvent()
    data class SelectOrganization(val organization: OrganizationShortUi?) : ImportEvent()
    data object ExtractDraft : ImportEvent()
    data class ToggleEntry(val id: Int) : ImportEvent()
    data class UpdateEntry(val entry: ScheduleImportEntryUi) : ImportEvent()
    data class MoveClass(val id: Int, val dayOfWeek: Int) : ImportEvent()
    data class SwapClasses(val firstId: Int, val secondId: Int) : ImportEvent()
    data object ApplyDraft : ImportEvent()
    data class RewardedAdGranted(val challengeId: String) : ImportEvent()
    data object RewardedAdUnavailable : ImportEvent()
    data object EditSource : ImportEvent()
    data object ClickBack : ImportEvent()
    data object ClickAddOrganization : ImportEvent()
}

internal sealed class ImportEffect : StoreEffect {
    data class ShowError(val failure: ScheduleFailures) : ImportEffect()
}

internal sealed class ImportAction : StoreAction {
    data class UpdateHasPhoto(val hasPhoto: Boolean) : ImportAction()
    data class UpdateNote(val note: String) : ImportAction()
    data class UpdateOrganization(val organization: OrganizationShortUi?) : ImportAction()
    data class SetupOrganizations(val organizations: List<OrganizationShortUi>) : ImportAction()
    data class SetupCatalog(
        val subjects: List<SubjectUi>,
        val employees: List<EmployeeUi>,
    ) : ImportAction()
    data class SetupDraft(
        val draft: ScheduleImportDraftUi?,
        val requestId: UID?,
    ) : ImportAction()
    data class UpdateLoading(val isLoading: Boolean) : ImportAction()
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
