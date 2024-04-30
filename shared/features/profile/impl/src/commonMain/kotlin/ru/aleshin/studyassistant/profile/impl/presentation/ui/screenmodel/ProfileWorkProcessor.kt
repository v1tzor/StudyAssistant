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
import functional.handleAndGet
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import ru.aleshin.studyassistant.auth.api.navigation.AuthScreen
import ru.aleshin.studyassistant.profile.impl.domain.interactors.AuthInteractor
import ru.aleshin.studyassistant.profile.impl.domain.interactors.FriendsInteractor
import ru.aleshin.studyassistant.profile.impl.domain.interactors.UserInteractor
import ru.aleshin.studyassistant.profile.impl.navigation.ProfileScreenProvider
import ru.aleshin.studyassistant.profile.impl.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.profile.impl.presentation.ui.contract.ProfileAction
import ru.aleshin.studyassistant.profile.impl.presentation.ui.contract.ProfileEffect

/**
 * @author Stanislav Aleshin on 21.04.2024.
 */
internal interface ProfileWorkProcessor : FlowWorkProcessor<ProfileWorkCommand, ProfileAction, ProfileEffect> {

    class Base(
        private val userInteractor: UserInteractor,
        private val friendsInteractor: FriendsInteractor,
        private val authInteractor: AuthInteractor,
        private val screenProvider: ProfileScreenProvider,
    ) : ProfileWorkProcessor {

        override suspend fun work(command: ProfileWorkCommand) = when (command) {
            is ProfileWorkCommand.LoadProfileInfo -> loadProfileInfoWork()
            is ProfileWorkCommand.SignOut -> signOutWork()
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        private fun loadProfileInfoWork() = flow {
            userInteractor.fetchCurrentAppUser().flatMapLatest { userEither ->
                userEither.handleAndGet(
                    onLeftAction = { flowOf(EffectResult(ProfileEffect.ShowError(it))) },
                    onRightAction = { userInfo ->
                        friendsInteractor.fetchAllFriendRequests().map { friendsEither ->
                            friendsEither.handleAndGet(
                                onLeftAction = { EffectResult(ProfileEffect.ShowError(it)) },
                                onRightAction = { friendRequest ->
                                    val profile = userInfo.mapToUi()
                                    val requests = friendRequest.mapToUi()
                                    ActionResult(ProfileAction.UpdateProfileInfo(profile, requests))
                                }
                            )
                        }
                    },
                )
            }.collect { workResult ->
                emit(workResult)
            }
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

