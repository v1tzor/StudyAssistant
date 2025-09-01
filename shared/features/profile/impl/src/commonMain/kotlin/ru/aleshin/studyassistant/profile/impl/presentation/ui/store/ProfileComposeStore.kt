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

package ru.aleshin.studyassistant.profile.impl.presentation.ui.store

import ru.aleshin.studyassistant.core.common.architecture.component.EmptyInput
import ru.aleshin.studyassistant.core.common.architecture.store.BaseOnlyOutComposeStore
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.EffectCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.StateCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.profile.api.ProfileFeatureComponent.ProfileOutput
import ru.aleshin.studyassistant.profile.impl.presentation.ui.contract.ProfileAction
import ru.aleshin.studyassistant.profile.impl.presentation.ui.contract.ProfileEffect
import ru.aleshin.studyassistant.profile.impl.presentation.ui.contract.ProfileEvent
import ru.aleshin.studyassistant.profile.impl.presentation.ui.contract.ProfileState

/**
 * @author Stanislav Aleshin on 21.04.2024
 */
internal class ProfileComposeStore(
    private val workProcessor: ProfileWorkProcessor,
    private val dateManager: DateManager,
    stateCommunicator: StateCommunicator<ProfileState>,
    effectCommunicator: EffectCommunicator<ProfileEffect>,
    coroutineManager: CoroutineManager,
) : BaseOnlyOutComposeStore<ProfileState, ProfileEvent, ProfileAction, ProfileEffect, ProfileOutput>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun initialize(input: EmptyInput, isRestore: Boolean) {
        dispatchEvent(ProfileEvent.Started)
    }

    override suspend fun WorkScope<ProfileState, ProfileAction, ProfileEffect, ProfileOutput>.handleEvent(
        event: ProfileEvent,
    ) {
        when (event) {
            is ProfileEvent.Started -> {
                sendAction(ProfileAction.UpdateCurrentTime(dateManager.fetchCurrentInstant()))
                launchBackgroundWork(BackgroundKey.LOAD_PROFILE_INFO) {
                    val command = ProfileWorkCommand.LoadProfileInfo
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_SHARED_SCHEDULES) {
                    val command = ProfileWorkCommand.LoadSharedSchedules
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_FRIENDS) {
                    val command = ProfileWorkCommand.LoadFriends
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ProfileEvent.SendSharedSchedule -> {
                launchBackgroundWork(BackgroundKey.SEND_SCHEDULE) {
                    val command = ProfileWorkCommand.SendSharedSchedule(event.sendData)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ProfileEvent.CancelSentSchedule -> {
                launchBackgroundWork(BackgroundKey.DATA_ACTION) {
                    val command = ProfileWorkCommand.CancelSentSharedSchedule(event.schedule)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ProfileEvent.ClickSignOut -> {
                launchBackgroundWork(BackgroundKey.DATA_ACTION) {
                    val command = ProfileWorkCommand.SignOut
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ProfileEvent.ClickSharedSchedule -> {
                consumeOutput(ProfileOutput.NavigateToSharedSchedule(event.shareId))
            }
            is ProfileEvent.ClickFriends -> {
                consumeOutput(ProfileOutput.NavigateToFriends)
            }
            is ProfileEvent.ClickGeneralSettings -> {
                consumeOutput(ProfileOutput.NavigateToSettings.General)
            }
            is ProfileEvent.ClickNotifySettings -> {
                consumeOutput(ProfileOutput.NavigateToSettings.Notification)
            }
            is ProfileEvent.ClickCalendarSettings -> {
                consumeOutput(ProfileOutput.NavigateToSettings.Calendar)
            }
            is ProfileEvent.ClickPaymentsSettings -> {
                consumeOutput(ProfileOutput.NavigateToSettings.Subscription)
            }
            is ProfileEvent.ClickAboutApp -> {
                consumeOutput(ProfileOutput.NavigateToSettings.AboutApp)
            }
            is ProfileEvent.ClickEditProfile -> {
                consumeOutput(ProfileOutput.NavigateToProfileEditor)
            }
        }
    }

    override suspend fun reduce(
        action: ProfileAction,
        currentState: ProfileState,
    ) = when (action) {
        is ProfileAction.UpdateProfileInfo -> currentState.copy(
            appUserProfile = action.profile,
            friendRequest = action.requests,
            isLoading = false,
        )
        is ProfileAction.UpdateSharedSchedules -> currentState.copy(
            sharedSchedules = action.schedules,
            allOrganizations = action.organizations,
            isLoadingShare = false,
        )
        is ProfileAction.UpdateFriends -> currentState.copy(
            allFriends = action.friends,
        )
        is ProfileAction.UpdateCurrentTime -> currentState.copy(
            currentTime = action.time,
        )
        is ProfileAction.UpdateLoading -> currentState.copy(
            isLoading = action.isLoading,
        )
        is ProfileAction.UpdateLoadingSend -> currentState.copy(
            isLoadingSend = action.isLoading,
        )
        is ProfileAction.UpdateLoadingShared -> currentState.copy(
            isLoadingShare = action.isLoading,
        )
    }

    enum class BackgroundKey : BackgroundWorkKey {
        LOAD_PROFILE_INFO, LOAD_SHARED_SCHEDULES, LOAD_FRIENDS, SEND_SCHEDULE, DATA_ACTION
    }

    class Factory(
        private val workProcessor: ProfileWorkProcessor,
        private val dateManager: DateManager,
        private val coroutineManager: CoroutineManager
    ) : BaseOnlyOutComposeStore.Factory<ProfileComposeStore, ProfileState> {

        override fun create(savedState: ProfileState): ProfileComposeStore {
            return ProfileComposeStore(
                workProcessor = workProcessor,
                dateManager = dateManager,
                coroutineManager = coroutineManager,
                stateCommunicator = StateCommunicator.Default(savedState),
                effectCommunicator = EffectCommunicator.Default(),
            )
        }
    }
}