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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.store

import ru.aleshin.studyassistant.core.common.architecture.store.BaseComposeStore
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.EffectCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.StateCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkScope
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleFailures
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.contract.ImportAction
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.contract.ImportEffect
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.contract.ImportEvent
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.contract.ImportInput
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.contract.ImportOutput
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.contract.ImportState

/**
 * @author Stanislav Aleshin on 16.08.2026.
 */
internal class ImportComposeStore(
    private val workProcessor: ImportWorkProcessor,
    stateCommunicator: StateCommunicator<ImportState>,
    effectCommunicator: EffectCommunicator<ImportEffect>,
    coroutineManager: CoroutineManager,
) : BaseComposeStore<ImportState, ImportEvent, ImportAction, ImportEffect, ImportInput, ImportOutput>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun initialize(input: ImportInput, isRestore: Boolean) {
        dispatchEvent(ImportEvent.Started)
    }

    override suspend fun WorkScope<ImportState, ImportAction, ImportEffect, ImportOutput>.handleEvent(
        event: ImportEvent,
    ) {
        when (event) {
            is ImportEvent.Started -> {
                launchBackgroundWork(BackgroundKey.LOAD_DATA) {
                    workProcessor.work(ImportWorkCommand.LoadOrganizations).collectAndHandleWork()
                }
            }
            is ImportEvent.SelectedPhoto -> {
                launchBackgroundWork(BackgroundKey.PROCESS) {
                    val command = ImportWorkCommand.PrepareImage(event.imageBytes)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ImportEvent.ImageSelectionFailed -> {
                sendEffect(ImportEffect.ShowError(ScheduleFailures.InvalidImage))
            }
            is ImportEvent.UpdateNote -> {
                sendAction(ImportAction.UpdateNote(event.note))
            }
            is ImportEvent.SelectOrganization -> {
                sendAction(ImportAction.UpdateOrganization(event.organization))
                val organizationId = event.organization?.uid
                if (organizationId != null) {
                    launchBackgroundWork(BackgroundKey.LOAD_CATALOG) {
                        val command = ImportWorkCommand.LoadCatalog(organizationId)
                        workProcessor.work(command).collectAndHandleWork()
                    }
                } else {
                    sendAction(ImportAction.SetupCatalog(emptyList(), emptyList()))
                }
            }
            is ImportEvent.ExtractDraft -> with(state) {
                val organizationId = organization?.uid ?: return
                launchBackgroundWork(BackgroundKey.PROCESS) {
                    val command = ImportWorkCommand.ExtractDraft(
                        requestId = randomUUID(),
                        note = note,
                        organizationId = organizationId,
                    )
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ImportEvent.ToggleEntry -> with(state) {
                val updatedDraft = draft?.copy(
                    entries = draft.entries.map { entry ->
                        if (entry.id == event.id) entry.copy(included = !entry.included) else entry
                    },
                )
                sendAction(ImportAction.SetupDraft(updatedDraft, requestId))
            }
            is ImportEvent.UpdateEntry -> with(state) {
                val updatedDraft = draft?.copy(
                    entries = draft.entries.map { entry ->
                        if (entry.id == event.entry.id) event.entry else entry
                    },
                )
                sendAction(ImportAction.SetupDraft(updatedDraft, requestId))
            }
            is ImportEvent.MoveClass -> with(state) {
                val updatedDraft = draft?.copy(
                    entries = draft.entries.map { entry ->
                        if (entry.id == event.id) entry.copy(dayOfWeek = event.dayOfWeek) else entry
                    },
                )
                sendAction(ImportAction.SetupDraft(updatedDraft, requestId))
            }
            is ImportEvent.SwapClasses -> with(state) {
                val first = draft?.entries?.firstOrNull { entry -> entry.id == event.firstId }
                val second = draft?.entries?.firstOrNull { entry -> entry.id == event.secondId }
                val updatedDraft = if (first != null && second != null) {
                    draft.copy(
                        entries = draft.entries.map { entry ->
                            when (entry.id) {
                                first.id -> entry.copy(
                                    dayOfWeek = second.dayOfWeek,
                                    classNumber = second.classNumber,
                                    startTime = second.startTime,
                                    endTime = second.endTime,
                                )
                                second.id -> entry.copy(
                                    dayOfWeek = first.dayOfWeek,
                                    classNumber = first.classNumber,
                                    startTime = first.startTime,
                                    endTime = first.endTime,
                                )
                                else -> entry
                            }
                        },
                    )
                } else {
                    draft
                }
                sendAction(ImportAction.SetupDraft(updatedDraft, requestId))
            }
            is ImportEvent.ApplyDraft -> with(state) {
                if (draft != null && organization != null && requestId != null) {
                    launchBackgroundWork(BackgroundKey.REWARD) {
                        val command = ImportWorkCommand.PrepareImportReward(requestId)
                        workProcessor.work(command).collectAndHandleWork()
                    }
                }
            }
            is ImportEvent.RewardedAdGranted -> with(state) {
                val currentDraft = draft
                val organizationId = organization?.uid
                if (currentDraft != null && organizationId != null) {
                    launchBackgroundWork(BackgroundKey.REWARD) {
                        val command = ImportWorkCommand.ApplyDraft(
                            draft = currentDraft,
                            organizationId = organizationId,
                            rewardChallengeId = event.challengeId,
                        )
                        workProcessor.work(command).collectAndHandleWork()
                    }
                }
            }
            is ImportEvent.RewardedAdUnavailable -> {
                sendAction(ImportAction.UpdateRewardChallenge(null, false))
                sendEffect(ImportEffect.ShowError(ScheduleFailures.RewardUnavailable))
            }
            is ImportEvent.EditSource -> {
                sendAction(ImportAction.SetupDraft(null, null))
            }
            is ImportEvent.ClickBack -> with(state) {
                if (draft != null && !isApplied) {
                    sendAction(ImportAction.SetupDraft(null, requestId))
                } else {
                    consumeOutput(ImportOutput.NavigateToBack)
                }
            }
            is ImportEvent.ClickAddOrganization -> {
                consumeOutput(ImportOutput.NavigateToOrganizationEditor(null))
            }
        }
    }

    override suspend fun reduce(action: ImportAction, currentState: ImportState) = when (action) {
        is ImportAction.UpdateHasPhoto -> currentState.copy(hasPhoto = action.hasPhoto)
        is ImportAction.UpdateNote -> currentState.copy(note = action.note)
        is ImportAction.UpdateOrganization -> currentState.copy(organization = action.organization)
        is ImportAction.SetupOrganizations -> currentState.copy(organizations = action.organizations)
        is ImportAction.SetupCatalog -> currentState.copy(
            subjects = action.subjects,
            employees = action.employees,
        )
        is ImportAction.SetupDraft -> currentState.copy(
            draft = action.draft,
            requestId = action.requestId,
        )
        is ImportAction.UpdateLoading -> currentState.copy(isLoading = action.isLoading)
        is ImportAction.UpdateApplied -> currentState.copy(isApplied = action.isApplied)
        is ImportAction.UpdateRewardChallenge -> currentState.copy(
            rewardChallengeId = action.challengeId,
            isRewardInProgress = action.isInProgress,
        )
    }

    private enum class BackgroundKey : BackgroundWorkKey {
        LOAD_DATA,
        LOAD_CATALOG,
        PROCESS,
        REWARD,
    }

    class Factory(
        private val workProcessor: ImportWorkProcessor,
        private val coroutineManager: CoroutineManager,
    ) : BaseComposeStore.Factory<ImportComposeStore, ImportState> {
        override fun create(savedState: ImportState) = ImportComposeStore(
            workProcessor = workProcessor,
            stateCommunicator = StateCommunicator.Default(savedState),
            effectCommunicator = EffectCommunicator.Default(),
            coroutineManager = coroutineManager,
        )
    }
}
