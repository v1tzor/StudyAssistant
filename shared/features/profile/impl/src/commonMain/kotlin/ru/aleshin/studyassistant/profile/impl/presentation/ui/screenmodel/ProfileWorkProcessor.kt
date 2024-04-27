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

import architecture.screenmodel.work.ActionResult
import architecture.screenmodel.work.EffectResult
import architecture.screenmodel.work.FlowWorkProcessor
import architecture.screenmodel.work.WorkCommand
import functional.handle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import ru.aleshin.studyassistant.auth.api.navigation.AuthScreen
import ru.aleshin.studyassistant.profile.impl.domain.interactors.AuthInteractor
import ru.aleshin.studyassistant.profile.impl.navigation.ProfileScreenProvider
import ru.aleshin.studyassistant.profile.impl.presentation.models.AppUserUi
import ru.aleshin.studyassistant.profile.impl.presentation.models.FriendRequestsUi
import ru.aleshin.studyassistant.profile.impl.presentation.ui.contract.ProfileAction
import ru.aleshin.studyassistant.profile.impl.presentation.ui.contract.ProfileEffect

/**
 * @author Stanislav Aleshin on 21.04.2024.
 */
internal interface ProfileWorkProcessor : FlowWorkProcessor<ProfileWorkCommand, ProfileAction, ProfileEffect> {

    class Base(
        private val screenProvider: ProfileScreenProvider,
        private val authInteractor: AuthInteractor,
    ) : ProfileWorkProcessor {

        override suspend fun work(command : ProfileWorkCommand) = when(command) {
            is ProfileWorkCommand.LoadProfileInfo -> loadProfileInfoWork()
            is ProfileWorkCommand.SignOut -> signOutWork()
        }

        private fun loadProfileInfoWork() = flow {
            // TODO Make load profile
            delay(2000L)
            val profile = AppUserUi("", "", "Stanislav Aleshin", "dev.aleshin@gmail.com", "110213")
            val requests = FriendRequestsUi(received = listOf(profile, profile))
            emit(ActionResult(ProfileAction.UpdateProfileInfo(profile, requests)))
        }

        private fun signOutWork() = flow {
            authInteractor.signOut().handle(
                onLeftAction = { emit(EffectResult(ProfileEffect.ShowError(it))) },
                onRightAction = {
                    val authScreen = screenProvider.provideAuthScreen(AuthScreen.Login)
                    emit(EffectResult(ProfileEffect.ReplaceGlobalScreen(authScreen)))
                },
            )
        }
    }
}

internal sealed class ProfileWorkCommand : WorkCommand {
    data object LoadProfileInfo : ProfileWorkCommand()
    data object SignOut : ProfileWorkCommand()
}

