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

package ru.aleshin.studyassistant.profile.impl.presentation.ui.screenmodel

import androidx.compose.runtime.Composable
import architecture.screenmodel.BaseScreenModel
import architecture.screenmodel.EmptyDeps
import architecture.screenmodel.work.WorkScope
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import managers.CoroutineManager
import org.kodein.di.instance
import ru.aleshin.studyassistant.profile.impl.di.holder.ProfileFeatureDIHolder
import ru.aleshin.studyassistant.profile.impl.navigation.ProfileScreenProvider
import ru.aleshin.studyassistant.profile.impl.presentation.ui.contract.ProfileAction
import ru.aleshin.studyassistant.profile.impl.presentation.ui.contract.ProfileEffect
import ru.aleshin.studyassistant.profile.impl.presentation.ui.contract.ProfileEvent
import ru.aleshin.studyassistant.profile.impl.presentation.ui.contract.ProfileViewState

/**
 * @author Stanislav Aleshin on 21.04.2024
 */
internal class ProfileScreenModel(
    private val screenProvider: ProfileScreenProvider,
    private val workProcessor: ProfileWorkProcessor,
    stateCommunicator: ProfileStateCommunicator,
    effectCommunicator: ProfileEffectCommunicator,
    coroutineManager: CoroutineManager,
) : BaseScreenModel<ProfileViewState, ProfileEvent, ProfileAction, ProfileEffect, EmptyDeps>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun init(deps: EmptyDeps) {
        if (!isInitialize) {
            super.init(deps)
            dispatchEvent(ProfileEvent.Init)
        }
    }

    override suspend fun WorkScope<ProfileViewState, ProfileAction, ProfileEffect>.handleEvent(
        event: ProfileEvent,
    ) {
        when (event) {
            ProfileEvent.Init -> launchBackgroundWork(ProfileWorkCommand.LoadProfileInfo) {
                val command = ProfileWorkCommand.LoadProfileInfo
                workProcessor.work(command).collectAndHandleWork()
            }
            ProfileEvent.SignOut -> launchBackgroundWork(ProfileWorkCommand.SignOut) {
                val command = ProfileWorkCommand.SignOut
                workProcessor.work(command).collectAndHandleWork()
            }
            ProfileEvent.EditProfile -> {}
            ProfileEvent.NavigateToCalendarSettings -> {}
            ProfileEvent.NavigateToGeneralSettings -> {}
            ProfileEvent.NavigateToFriends -> {}
            ProfileEvent.NavigateToNotifySettings -> {}
            ProfileEvent.NavigateToPaymentsSettings -> {}
            ProfileEvent.NavigateToPrivacySettings -> {}
        }
    }

    override suspend fun reduce(
        action: ProfileAction,
        currentState: ProfileViewState,
    ) = when (action) {
        is ProfileAction.UpdateLoading -> currentState.copy(
            isLoading = action.isLoading,
        )
        is ProfileAction.UpdateProfileInfo -> currentState.copy(
            isLoading = false,
            myProfile = action.profile,
            myFriendRequest = action.requests,
        )
    }
}

@Composable
internal fun Screen.rememberProfileScreenModel(): ProfileScreenModel {
    val di = ProfileFeatureDIHolder.fetchDI()
    return rememberScreenModel { di.instance<ProfileScreenModel>() }
}