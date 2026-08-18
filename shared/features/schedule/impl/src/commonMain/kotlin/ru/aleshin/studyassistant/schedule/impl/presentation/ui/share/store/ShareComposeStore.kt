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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.store

import ru.aleshin.studyassistant.core.common.architecture.store.BaseComposeStore
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.EffectCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.StateCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleFailures
import ru.aleshin.studyassistant.schedule.impl.presentation.models.share.ShareStatus
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.contract.ShareAction
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.contract.ShareEffect
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.contract.ShareEvent
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.contract.ShareInput
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.contract.ShareOutput
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.contract.ShareState

/**
 * @author Stanislav Aleshin on 08.08.2026.
 */
internal class ShareComposeStore(
    private val workProcessor: ShareWorkProcessor,
    private val dateManager: DateManager,
    stateCommunicator: StateCommunicator<ShareState>,
    effectCommunicator: EffectCommunicator<ShareEffect>,
    coroutineManager: CoroutineManager,
) : BaseComposeStore<ShareState, ShareEvent, ShareAction, ShareEffect, ShareInput, ShareOutput>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun initialize(input: ShareInput, isRestore: Boolean) {
        dispatchEvent(ShareEvent.Started(input, isRestore))
    }

    override suspend fun WorkScope<ShareState, ShareAction, ShareEffect, ShareOutput>.handleEvent(
        event: ShareEvent,
    ) {
        when (event) {
            is ShareEvent.Started -> with(event) {
                sendAction(ShareAction.UpdateCurrentTime(dateManager.fetchCurrentInstant()))
                if (state.shareableOrganizations.isEmpty()) {
                    launchBackgroundWork(BackgroundKey.LOAD_ORGANIZATIONS) {
                        val command = ShareWorkCommand.LoadShareableOrganizations
                        workProcessor.work(command).collectAndHandleWork()
                    }
                }
                if (!isRestore && !inputData.code.isNullOrBlank()) {
                    sendAction(ShareAction.UpdateCode(inputData.code))
                    launchBackgroundWork(BackgroundKey.SHARE_ACTION) {
                        val command = ShareWorkCommand.ClaimShare(inputData.code)
                        workProcessor.work(command).collectAndHandleWork()
                    }
                }
            }
            is ShareEvent.UpdatedCode -> {
                sendAction(ShareAction.UpdateCode(event.code))
            }
            is ShareEvent.ToggleShareOrganization -> {
                val selectedIds = state().selectedOrganizationIds
                val updatedIds = if (event.organizationId in selectedIds) {
                    selectedIds - event.organizationId
                } else {
                    selectedIds + event.organizationId
                }
                sendAction(ShareAction.UpdateSelectedOrganizations(updatedIds))
            }
            is ShareEvent.CreateShare -> {
                val selectedIds = state().selectedOrganizationIds
                if (selectedIds.isEmpty()) return
                launchBackgroundWork(BackgroundKey.SHARE_ACTION) {
                    val command = ShareWorkCommand.CreateShare(selectedIds.toList())
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ShareEvent.ClaimShare -> with(state) {
                launchBackgroundWork(BackgroundKey.SHARE_ACTION) {
                    val command = ShareWorkCommand.ClaimShare(code)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ShareEvent.ScannedCode -> with(event) {
                sendAction(ShareAction.UpdateCode(code))
                launchBackgroundWork(BackgroundKey.SHARE_ACTION) {
                    val command = ShareWorkCommand.ClaimShare(code)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ShareEvent.ClickLinkOrganization -> with(state()) {
                launchBackgroundWork(BackgroundKey.LINK_ORGANIZATION) {
                    val claim = checkNotNull(claim)
                    val command = ShareWorkCommand.LinkOrganization(
                        allLinkData = organizationsLinkData,
                        sharedSchedules = claim.schedules,
                        sharedOrganization = event.sharedOrganization,
                        targetOrganization = event.linkedOrganization,
                    )
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ShareEvent.UpdatedLinkedSubjects -> with(state()) {
                launchBackgroundWork(BackgroundKey.LINK_DATA) {
                    val claim = checkNotNull(claim)
                    val command = ShareWorkCommand.UpdateLinkedSubjects(
                        allLinkData = organizationsLinkData,
                        sharedSchedules = claim.schedules,
                        sharedOrganization = event.sharedOrganization,
                        subjects = event.subjects,
                    )
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ShareEvent.UpdatedLinkedTeachers -> with(state()) {
                launchBackgroundWork(BackgroundKey.LINK_DATA) {
                    val claim = checkNotNull(claim)
                    val command = ShareWorkCommand.UpdateLinkedEmployees(
                        allLinkData = organizationsLinkData,
                        sharedSchedules = claim.schedules,
                        sharedOrganization = event.sharedOrganization,
                        teachers = event.teachers,
                    )
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ShareEvent.AcceptedSharedSchedule -> with(state()) {
                launchBackgroundWork(BackgroundKey.SHARE_ACTION) {
                    val command = ShareWorkCommand.PrepareImportReward(checkNotNull(claim))
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ShareEvent.RewardedAdGranted -> with(state()) {
                launchBackgroundWork(BackgroundKey.SHARE_ACTION) {
                    val command = ShareWorkCommand.AcceptSharedSchedule(
                        rewardChallengeId = event.challengeId,
                        claim = checkNotNull(claim),
                        organizationsLinkData = organizationsLinkData,
                    )
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ShareEvent.RewardedAdUnavailable -> {
                sendAction(ShareAction.UpdateRewardChallenge(null, false))
                sendEffect(ShareEffect.ShowError(ScheduleFailures.RewardUnavailable))
            }
            is ShareEvent.RejectedSharedSchedule -> with(state()) {
                launchBackgroundWork(BackgroundKey.SHARE_ACTION) {
                    val command = ShareWorkCommand.ReleaseShare(
                        claim = checkNotNull(claim),
                        navigateBack = false,
                    )
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ShareEvent.Reset -> {
                sendAction(ShareAction.Reset)
            }
            is ShareEvent.ClickBack -> {
                if (state().status == ShareStatus.PREVIEW && state().claim != null) {
                    launchBackgroundWork(BackgroundKey.SHARE_ACTION) {
                        val command = ShareWorkCommand.ReleaseShare(
                            claim = checkNotNull(state().claim),
                            navigateBack = true,
                        )
                        workProcessor.work(command).collectAndHandleWork()
                    }
                } else {
                    consumeOutput(ShareOutput.NavigateToBack)
                }
            }
        }
    }

    override suspend fun reduce(action: ShareAction, currentState: ShareState) = when (action) {
        is ShareAction.UpdateCode -> currentState.copy(
            code = action.code
        )
        is ShareAction.UpdateStatus -> currentState.copy(
            status = action.status
        )
        is ShareAction.SetupLink -> currentState.copy(
            status = ShareStatus.READY,
            link = action.link
        )
        is ShareAction.SetupClaim -> currentState.copy(
            status = ShareStatus.PREVIEW,
            claim = action.claim,
            organizationsLinkData = action.organizationsLinkData,
            linkedSchedules = action.linkedSchedules,
            maxNumberOfWeek = action.maxNumberOfWeek,
        )
        is ShareAction.UpdateLinkData -> currentState.copy(
            organizationsLinkData = action.linkData,
            linkedSchedules = action.linkedSchedules,
        )
        is ShareAction.UpdateOrganizations -> currentState.copy(
            allOrganizations = action.organizations
        )
        is ShareAction.SetupShareableOrganizations -> currentState.copy(
            shareableOrganizations = action.organizations,
            selectedOrganizationIds = action.selectedOrganizationIds,
            isLoadingShareableOrganizations = false,
        )
        is ShareAction.UpdateSelectedOrganizations -> currentState.copy(
            selectedOrganizationIds = action.organizationIds,
        )
        is ShareAction.UpdateCurrentTime -> currentState.copy(
            currentTime = action.time
        )
        is ShareAction.UpdateLoadingLinkedOrganization -> currentState.copy(
            isLoadingLinkedOrganization = action.isLoading,
        )
        is ShareAction.UpdateRewardChallenge -> currentState.copy(
            rewardChallengeId = action.challengeId,
            isRewardInProgress = action.isInProgress,
        )
        is ShareAction.Reset -> ShareState(
            currentTime = currentState.currentTime,
            shareableOrganizations = currentState.shareableOrganizations,
            selectedOrganizationIds = currentState.selectedOrganizationIds,
            isLoadingShareableOrganizations = false,
        )
    }

    enum class BackgroundKey : BackgroundWorkKey {
        LOAD_ORGANIZATIONS,
        LINK_ORGANIZATION,
        LINK_DATA,
        SHARE_ACTION,
    }

    class Factory(
        private val workProcessor: ShareWorkProcessor,
        private val dateManager: DateManager,
        private val coroutineManager: CoroutineManager,
    ) : BaseComposeStore.Factory<ShareComposeStore, ShareState> {
        override fun create(savedState: ShareState) = ShareComposeStore(
            workProcessor = workProcessor,
            dateManager = dateManager,
            stateCommunicator = StateCommunicator.Default(savedState),
            effectCommunicator = EffectCommunicator.Default(),
            coroutineManager = coroutineManager,
        )
    }
}
