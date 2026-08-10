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

package ru.aleshin.studyassistant.profile.impl.presentation.ui.store

import ru.aleshin.studyassistant.core.common.architecture.component.EmptyInput
import ru.aleshin.studyassistant.core.common.architecture.store.BaseOnlyOutComposeStore
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.EffectCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.StateCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.profile.api.ProfileOutput
import ru.aleshin.studyassistant.profile.impl.presentation.ui.contract.ProfileAction
import ru.aleshin.studyassistant.profile.impl.presentation.ui.contract.ProfileEffect
import ru.aleshin.studyassistant.profile.impl.presentation.ui.contract.ProfileEvent
import ru.aleshin.studyassistant.profile.impl.presentation.ui.contract.ProfileState

/**
 * @author Stanislav Aleshin on 21.04.2024
 */
internal class ProfileComposeStore(
    private val workProcessor: ProfileWorkProcessor,
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
                launchBackgroundWork(BackgroundKey.PROFILE) {
                    val command = ProfileWorkCommand.FetchProfile
                    workProcessor.work(command).collectAndHandleWork()
                }
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
            is ProfileEvent.ClickAiSettings -> {
                consumeOutput(ProfileOutput.NavigateToSettings.Ai)
            }
            is ProfileEvent.ClickShareSchedule -> {
                consumeOutput(ProfileOutput.NavigateToScheduleSharing)
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
        is ProfileAction.UpdateProfile -> currentState.copy(
            profile = action.profile,
            isLoading = false,
        )
        is ProfileAction.UpdateLoading -> currentState.copy(
            isLoading = action.isLoading,
        )
    }

    enum class BackgroundKey : BackgroundWorkKey {
        PROFILE
    }

    class Factory(
        private val workProcessor: ProfileWorkProcessor,
        private val coroutineManager: CoroutineManager
    ) : BaseOnlyOutComposeStore.Factory<ProfileComposeStore, ProfileState> {

        override fun create(savedState: ProfileState): ProfileComposeStore {
            return ProfileComposeStore(
                workProcessor = workProcessor,
                coroutineManager = coroutineManager,
                stateCommunicator = StateCommunicator.Default(savedState),
                effectCommunicator = EffectCommunicator.Default(),
            )
        }
    }
}
