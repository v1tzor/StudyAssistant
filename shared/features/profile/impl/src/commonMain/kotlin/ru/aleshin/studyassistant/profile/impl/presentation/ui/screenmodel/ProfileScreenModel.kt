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
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import co.touchlab.kermit.Logger
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.BaseScreenModel
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.EmptyDeps
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.profile.impl.di.holder.ProfileFeatureDIHolder
import ru.aleshin.studyassistant.profile.impl.navigation.ProfileScreenProvider
import ru.aleshin.studyassistant.profile.impl.presentation.ui.contract.ProfileAction
import ru.aleshin.studyassistant.profile.impl.presentation.ui.contract.ProfileEffect
import ru.aleshin.studyassistant.profile.impl.presentation.ui.contract.ProfileEvent
import ru.aleshin.studyassistant.profile.impl.presentation.ui.contract.ProfileViewState
import ru.aleshin.studyassistant.settings.api.navigation.SettingsScreen
import ru.aleshin.studyassistant.users.api.navigation.UsersScreen

/**
 * @author Stanislav Aleshin on 21.04.2024
 */
internal class ProfileScreenModel(
    private val workProcessor: ProfileWorkProcessor,
    private val screenProvider: ProfileScreenProvider,
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
            is ProfileEvent.Init -> launchBackgroundWork(ProfileWorkCommand.LoadProfileInfo) {
                val command = ProfileWorkCommand.LoadProfileInfo
                workProcessor.work(command).collectAndHandleWork()
            }
            is ProfileEvent.SignOut -> launchBackgroundWork(ProfileWorkCommand.SignOut) {
                val command = ProfileWorkCommand.SignOut
                workProcessor.work(command).collectAndHandleWork()
            }
            is ProfileEvent.NavigateToPrivacySettings -> {
            }
            is ProfileEvent.NavigateToFriends -> {
                val screen = screenProvider.provideUsersScreen(UsersScreen.Friends)
                sendEffect(ProfileEffect.PushGlobalScreen(screen))
            }
            is ProfileEvent.NavigateToGeneralSettings -> {
                val screen = screenProvider.provideSettingsScreen(SettingsScreen.General)
                sendEffect(ProfileEffect.PushGlobalScreen(screen))
            }
            is ProfileEvent.NavigateToNotifySettings -> {
                val screen = screenProvider.provideSettingsScreen(SettingsScreen.Notification)
                sendEffect(ProfileEffect.PushGlobalScreen(screen))
            }
            is ProfileEvent.NavigateToCalendarSettings -> {
                val screen = screenProvider.provideSettingsScreen(SettingsScreen.Calendar)
                sendEffect(ProfileEffect.PushGlobalScreen(screen))
            }
            is ProfileEvent.NavigateToPaymentsSettings -> {
                val screen = screenProvider.provideSettingsScreen(SettingsScreen.Subscription)
                sendEffect(ProfileEffect.PushGlobalScreen(screen))
            }
            is ProfileEvent.EditProfile -> {}
        }
    }

    override suspend fun reduce(
        action: ProfileAction,
        currentState: ProfileViewState,
    ) = when (action) {
        is ProfileAction.UpdateProfileInfo -> currentState.copy(
            myProfile = action.profile,
            myFriendRequest = action.requests,
        )
    }

    override fun onDispose() {
        super.onDispose()
        Logger.i("test") { "onDispose -> profile" }
        ProfileFeatureDIHolder.clear()
    }
}

@Composable
internal fun Screen.rememberProfileScreenModel(): ProfileScreenModel {
    val di = ProfileFeatureDIHolder.fetchDI()
    return rememberScreenModel { di.instance<ProfileScreenModel>() }
}