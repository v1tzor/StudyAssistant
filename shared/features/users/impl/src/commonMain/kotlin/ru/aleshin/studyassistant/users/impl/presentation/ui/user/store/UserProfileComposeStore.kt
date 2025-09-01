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

package ru.aleshin.studyassistant.users.impl.presentation.ui.user.store

import ru.aleshin.studyassistant.core.common.architecture.store.BaseComposeStore
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.EffectCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.StateCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.users.impl.presentation.ui.user.contract.UserProfileAction
import ru.aleshin.studyassistant.users.impl.presentation.ui.user.contract.UserProfileEffect
import ru.aleshin.studyassistant.users.impl.presentation.ui.user.contract.UserProfileEvent
import ru.aleshin.studyassistant.users.impl.presentation.ui.user.contract.UserProfileInput
import ru.aleshin.studyassistant.users.impl.presentation.ui.user.contract.UserProfileOutput
import ru.aleshin.studyassistant.users.impl.presentation.ui.user.contract.UserProfileState

/**
 * @author Stanislav Aleshin on 15.07.2024
 */
internal class UserProfileComposeStore(
    private val workProcessor: UserProfileWorkProcessor,
    stateCommunicator: StateCommunicator<UserProfileState>,
    effectCommunicator: EffectCommunicator<UserProfileEffect>,
    coroutineManager: CoroutineManager,
) : BaseComposeStore<UserProfileState, UserProfileEvent, UserProfileAction, UserProfileEffect, UserProfileInput, UserProfileOutput>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun initialize(input: UserProfileInput, isRestore: Boolean) {
        dispatchEvent(UserProfileEvent.Started(input.userId))
    }

    override suspend fun WorkScope<UserProfileState, UserProfileAction, UserProfileEffect, UserProfileOutput>.handleEvent(
        event: UserProfileEvent,
    ) {
        when (event) {
            is UserProfileEvent.Started -> {
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
            is UserProfileEvent.ClickBack -> {
                consumeOutput(UserProfileOutput.NavigateToBack)
            }
        }
    }

    override suspend fun reduce(
        action: UserProfileAction,
        currentState: UserProfileState,
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

    class Factory(
        private val workProcessor: UserProfileWorkProcessor,
        private val coroutineManager: CoroutineManager,
    ) : BaseComposeStore.Factory<UserProfileComposeStore, UserProfileState> {

        override fun create(savedState: UserProfileState): UserProfileComposeStore {
            return UserProfileComposeStore(
                workProcessor = workProcessor,
                stateCommunicator = StateCommunicator.Default(savedState),
                effectCommunicator = EffectCommunicator.Default(),
                coroutineManager = coroutineManager,
            )
        }
    }
}