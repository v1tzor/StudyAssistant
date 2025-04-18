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

package ru.aleshin.studyassistant.users.impl.presentation.ui.user.screenmodel

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.BaseScreenModel
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.users.impl.di.holder.UsersFeatureDIHolder
import ru.aleshin.studyassistant.users.impl.presentation.ui.user.contract.UserProfileAction
import ru.aleshin.studyassistant.users.impl.presentation.ui.user.contract.UserProfileDeps
import ru.aleshin.studyassistant.users.impl.presentation.ui.user.contract.UserProfileEffect
import ru.aleshin.studyassistant.users.impl.presentation.ui.user.contract.UserProfileEvent
import ru.aleshin.studyassistant.users.impl.presentation.ui.user.contract.UserProfileViewState

/**
 * @author Stanislav Aleshin on 15.07.2024
 */
internal class UserProfileScreenModel(
    private val workProcessor: UserProfileWorkProcessor,
    stateCommunicator: UserProfileStateCommunicator,
    effectCommunicator: UserProfileEffectCommunicator,
    coroutineManager: CoroutineManager,
) : BaseScreenModel<UserProfileViewState, UserProfileEvent, UserProfileAction, UserProfileEffect, UserProfileDeps>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun init(deps: UserProfileDeps) {
        if (!isInitialize) {
            super.init(deps)
            dispatchEvent(UserProfileEvent.Init(deps.userId))
        }
    }

    override suspend fun WorkScope<UserProfileViewState, UserProfileAction, UserProfileEffect>.handleEvent(
        event: UserProfileEvent,
    ) {
        when (event) {
            is UserProfileEvent.Init -> {
                launchBackgroundWork(BackgroundKey.LOAD_USER) {
                    val command = UserProfileWorkCommand.LoadUser(event.userId)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is UserProfileEvent.SendFriendRequest -> with(state()) {
                launchBackgroundWork(BackgroundKey.USER_ACTION) {
                    val userId = checkNotNull(user?.uid)
                    val command = UserProfileWorkCommand.SendFriendRequest(userId)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is UserProfileEvent.AcceptFriendRequest -> with(state()) {
                launchBackgroundWork(BackgroundKey.USER_ACTION) {
                    val userId = checkNotNull(user?.uid)
                    val command = UserProfileWorkCommand.AcceptFriendRequest(userId)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is UserProfileEvent.CancelSendFriendRequest -> with(state()) {
                launchBackgroundWork(BackgroundKey.USER_ACTION) {
                    val userId = checkNotNull(user?.uid)
                    val command = UserProfileWorkCommand.CancelSendFriendRequest(userId)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is UserProfileEvent.DeleteFromFriends -> with(state()) {
                launchBackgroundWork(BackgroundKey.USER_ACTION) {
                    val userId = checkNotNull(user?.uid)
                    val command = UserProfileWorkCommand.DeleteFriend(userId)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is UserProfileEvent.NavigateToBack -> {
                sendEffect(UserProfileEffect.NavigateToBack)
            }
        }
    }

    override suspend fun reduce(
        action: UserProfileAction,
        currentState: UserProfileViewState,
    ) = when (action) {
        is UserProfileAction.UpdateUser -> currentState.copy(
            user = action.user,
            friendStatus = action.status,
            isLoading = false,
        )
        is UserProfileAction.UpdateLoading -> currentState.copy(
            isLoading = action.isLoading,
        )
    }

    enum class BackgroundKey : BackgroundWorkKey {
        LOAD_USER, USER_ACTION,
    }
}

@Composable
internal fun Screen.rememberUserProfileScreenModel(): UserProfileScreenModel {
    val di = UsersFeatureDIHolder.fetchDI()
    return rememberScreenModel { di.instance<UserProfileScreenModel>() }
}