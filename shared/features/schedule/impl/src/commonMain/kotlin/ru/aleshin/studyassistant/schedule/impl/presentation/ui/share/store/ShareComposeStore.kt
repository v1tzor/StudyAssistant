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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.store

import ru.aleshin.studyassistant.core.common.architecture.store.BaseComposeStore
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.EffectCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.StateCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.contract.ShareAction
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.contract.ShareEffect
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.contract.ShareEvent
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.contract.ShareInput
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.contract.ShareOutput
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.contract.ShareState
import ru.aleshin.studyassistant.users.api.UsersFeatureComponent.UsersConfig

/**
 * @author Stanislav Aleshin on 16.08.2024
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
                if (!isRestore) {
                    launchBackgroundWork(BackgroundKey.LOAD_SHARED_SCHEDULES) {
                        val command = ShareWorkCommand.LoadSharedSchedules(inputData.receivedShareId)
                        workProcessor.work(command).collectAndHandleWork()
                    }
                }
                launchBackgroundWork(BackgroundKey.LOAD_ORGANIZATIONS) {
                    val command = ShareWorkCommand.LoadAllOrganizations
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ShareEvent.ClickLinkOrganization -> with(state()) {
                launchBackgroundWork(BackgroundKey.LINK_ORGANIZATION) {
                    val receivedMediatedSchedule = checkNotNull(receivedMediatedSchedule)
                    val command = ShareWorkCommand.LinkOrganization(
                        allLinkData = organizationsLinkData,
                        sharedSchedules = receivedMediatedSchedule.schedules,
                        sharedOrganization = event.sharedOrganization,
                        targetOrganization = event.linkedOrganization,
                    )
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ShareEvent.UpdatedLinkedSubjects -> with(state()) {
                launchBackgroundWork(BackgroundKey.LINK_DATA) {
                    val receivedMediatedSchedule = checkNotNull(receivedMediatedSchedule)
                    val command = ShareWorkCommand.UpdateLinkedSubjects(
                        allLinkData = organizationsLinkData,
                        sharedSchedules = receivedMediatedSchedule.schedules,
                        sharedOrganization = event.sharedOrganization,
                        subjects = event.subjects,
                    )
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ShareEvent.UpdatedLinkedTeachers -> with(state()) {
                launchBackgroundWork(BackgroundKey.LINK_DATA) {
                    val receivedMediatedSchedule = checkNotNull(receivedMediatedSchedule)
                    val command = ShareWorkCommand.UpdateLinkedEmployees(
                        allLinkData = organizationsLinkData,
                        sharedSchedules = receivedMediatedSchedule.schedules,
                        sharedOrganization = event.sharedOrganization,
                        teachers = event.teachers,
                    )
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ShareEvent.AcceptedSharedSchedule -> with(state()) {
                launchBackgroundWork(BackgroundKey.SHARE_ACTION) {
                    val sharedSchedules = checkNotNull(receivedMediatedSchedule)
                    val command = ShareWorkCommand.AcceptSharedSchedule(
                        sharedSchedules = sharedSchedules,
                        organizationsLinkData = organizationsLinkData,
                        linkedSchedules = linkedSchedules,
                    )
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ShareEvent.RejectedSharedSchedule -> with(state()) {
                launchBackgroundWork(BackgroundKey.SHARE_ACTION) {
                    val sharedSchedules = checkNotNull(receivedMediatedSchedule)
                    val command = ShareWorkCommand.RejectSharedSchedule(sharedSchedules)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ShareEvent.ClickUserProfile -> with(event) {
                val config = UsersConfig.UserProfile(user.uid)
                consumeOutput(ShareOutput.NavigateToUserProfile(config))
            }
            is ShareEvent.ClickBack -> {
                consumeOutput(ShareOutput.NavigateToBack)
            }
        }
    }

    override suspend fun reduce(
        action: ShareAction,
        currentState: ShareState,
    ) = when (action) {
        is ShareAction.SetupSharedSchedules -> currentState.copy(
            receivedMediatedSchedule = action.receivedMediatedSchedule,
            organizationsLinkData = action.organizationsLinkData,
            linkedSchedules = action.linkedSchedules,
            isLoading = false,
        )
        is ShareAction.UpdateLinkData -> currentState.copy(
            organizationsLinkData = action.linkData,
            linkedSchedules = action.linkedSchedules,
        )
        is ShareAction.UpdateOrganizations -> currentState.copy(
            allOrganizations = action.organizations,
        )
        is ShareAction.UpdateCurrentTime -> currentState.copy(
            currentTime = action.time,
        )
        is ShareAction.UpdateLoading -> currentState.copy(
            isLoading = action.isLoading,
        )
        is ShareAction.UpdateLoadingAccept -> currentState.copy(
            isLoadingAccept = action.isLoading,
        )
        is ShareAction.UpdateLoadingLinkedOrganization -> currentState.copy(
            isLoadingLinkedOrganization = action.isLoading,
        )
    }

    enum class BackgroundKey : BackgroundWorkKey {
        LOAD_SHARED_SCHEDULES, LOAD_ORGANIZATIONS, LINK_ORGANIZATION, LINK_DATA, SHARE_ACTION
    }

    class Factory(
        private val workProcessor: ShareWorkProcessor,
        private val dateManager: DateManager,
        private val coroutineManager: CoroutineManager,
    ) : BaseComposeStore.Factory<ShareComposeStore, ShareState> {

        override fun create(savedState: ShareState): ShareComposeStore {
            return ShareComposeStore(
                workProcessor = workProcessor,
                dateManager = dateManager,
                stateCommunicator = StateCommunicator.Default(savedState),
                effectCommunicator = EffectCommunicator.Default(),
                coroutineManager = coroutineManager,
            )
        }
    }
}