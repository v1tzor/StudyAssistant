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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.profile.store

import ru.aleshin.studyassistant.core.common.architecture.component.EmptyInput
import ru.aleshin.studyassistant.core.common.architecture.store.BaseOnlyOutComposeStore
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.EffectCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.StateCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.ui.mappers.convertToInputFile
import ru.aleshin.studyassistant.editor.impl.presentation.ui.profile.contract.ProfileAction
import ru.aleshin.studyassistant.editor.impl.presentation.ui.profile.contract.ProfileEffect
import ru.aleshin.studyassistant.editor.impl.presentation.ui.profile.contract.ProfileEvent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.profile.contract.ProfileOutput
import ru.aleshin.studyassistant.editor.impl.presentation.ui.profile.contract.ProfileState

/**
 * @author Stanislav Aleshin on 28.07.2024
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
                launchBackgroundWork(BackgroundKey.LOAD_PROFILE) {
                    val command = ProfileWorkCommand.LoadProfile
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ProfileEvent.UpdateAvatar -> with(state()) {
                launchBackgroundWork(BackgroundKey.USER_ACTION) {
                    val user = checkNotNull(profile)
                    val inputFile = event.file.convertToInputFile()
                    val command = ProfileWorkCommand.UpdateAvatar(user, inputFile)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ProfileEvent.DeleteAvatar -> with(state()) {
                launchBackgroundWork(BackgroundKey.USER_ACTION) {
                    val user = checkNotNull(profile)
                    val command = ProfileWorkCommand.DeleteAvatar(user)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ProfileEvent.UpdateUsername -> with(state()) {
                launchBackgroundWork(BackgroundKey.USER_ACTION) {
                    val updatedUser = checkNotNull(profile).copy(username = event.name)
                    val command = ProfileWorkCommand.UpdateProfile(updatedUser)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ProfileEvent.UpdateDescription -> with(state()) {
                launchBackgroundWork(BackgroundKey.USER_ACTION) {
                    val updatedUser = checkNotNull(profile).copy(description = event.text)
                    val command = ProfileWorkCommand.UpdateProfile(updatedUser)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ProfileEvent.UpdateBirthday -> with(state()) {
                launchBackgroundWork(BackgroundKey.USER_ACTION) {
                    val updatedUser = checkNotNull(profile).copy(birthday = event.text)
                    val command = ProfileWorkCommand.UpdateProfile(updatedUser)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ProfileEvent.UpdateGender -> with(state()) {
                launchBackgroundWork(BackgroundKey.USER_ACTION) {
                    val updatedUser = checkNotNull(profile).copy(gender = event.gender)
                    val command = ProfileWorkCommand.UpdateProfile(updatedUser)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ProfileEvent.UpdateCity -> with(state()) {
                launchBackgroundWork(BackgroundKey.USER_ACTION) {
                    val updatedUser = checkNotNull(profile).copy(city = event.city)
                    val command = ProfileWorkCommand.UpdateProfile(updatedUser)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ProfileEvent.NavigateToBack -> {
                consumeOutput(ProfileOutput.NavigateToBack)
            }
        }
    }

    override suspend fun reduce(
        action: ProfileAction,
        currentState: ProfileState,
    ) = when (action) {
        is ProfileAction.SetupProfile -> currentState.copy(
            profile = action.profile,
            isLoading = false,
        )

        is ProfileAction.UpdateLoading -> currentState.copy(
            isLoading = action.isLoading,
        )
    }

    enum class BackgroundKey : BackgroundWorkKey {
        LOAD_PROFILE, USER_ACTION
    }

    class Factory(
        private val workProcessor: ProfileWorkProcessor,
        private val coroutineManager: CoroutineManager,
    ) : BaseOnlyOutComposeStore.Factory<ProfileComposeStore, ProfileState> {

        override fun create(savedState: ProfileState): ProfileComposeStore {
            return ProfileComposeStore(
                workProcessor = workProcessor,
                stateCommunicator = StateCommunicator.Default(savedState),
                effectCommunicator = EffectCommunicator.Default(),
                coroutineManager = coroutineManager,
            )
        }
    }
}
