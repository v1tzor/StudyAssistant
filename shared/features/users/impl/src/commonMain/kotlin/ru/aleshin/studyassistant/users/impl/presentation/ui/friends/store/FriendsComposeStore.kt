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

package ru.aleshin.studyassistant.users.impl.presentation.ui.friends.store

import ru.aleshin.studyassistant.core.common.architecture.component.EmptyInput
import ru.aleshin.studyassistant.core.common.architecture.store.BaseOnlyOutComposeStore
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.EffectCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.StateCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.users.api.UsersFeatureComponent.UsersConfig
import ru.aleshin.studyassistant.users.impl.presentation.ui.friends.contract.FriendsAction
import ru.aleshin.studyassistant.users.impl.presentation.ui.friends.contract.FriendsEffect
import ru.aleshin.studyassistant.users.impl.presentation.ui.friends.contract.FriendsEvent
import ru.aleshin.studyassistant.users.impl.presentation.ui.friends.contract.FriendsOutput
import ru.aleshin.studyassistant.users.impl.presentation.ui.friends.contract.FriendsState

/**
 * @author Stanislav Aleshin on 12.07.2024
 */
internal class FriendsComposeStore(
    private val workProcessor: FriendsWorkProcessor,
    private val dateManager: DateManager,
    stateCommunicator: StateCommunicator<FriendsState>,
    effectCommunicator: EffectCommunicator<FriendsEffect>,
    coroutineManager: CoroutineManager,
) : BaseOnlyOutComposeStore<FriendsState, FriendsEvent, FriendsAction, FriendsEffect, FriendsOutput>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun initialize(input: EmptyInput, isRestore: Boolean) {
        dispatchEvent(FriendsEvent.Started)
    }

    override suspend fun WorkScope<FriendsState, FriendsAction, FriendsEffect, FriendsOutput>.handleEvent(
        event: FriendsEvent,
    ) {
        when (event) {
            is FriendsEvent.Started -> {
                sendAction(FriendsAction.UpdateCurrentTime(dateManager.fetchCurrentInstant()))
                launchBackgroundWork(BackgroundKey.LOAD_FRIENDS) {
                    val command = FriendsWorkCommand.LoadFriendsAndRequests
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is FriendsEvent.SearchUsers -> {
                launchBackgroundWork(BackgroundKey.SEARCH_USERS) {
                    val command = FriendsWorkCommand.SearchUsersByCode(event.code)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is FriendsEvent.AcceptFriendRequest -> {
                launchBackgroundWork(BackgroundKey.FRIEND_ACTION) {
                    val command = FriendsWorkCommand.AcceptFriendRequest(event.userId)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is FriendsEvent.RejectFriendRequest -> {
                launchBackgroundWork(BackgroundKey.FRIEND_ACTION) {
                    val command = FriendsWorkCommand.RejectFriendRequest(event.userId)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is FriendsEvent.SendFriendRequest -> {
                launchBackgroundWork(BackgroundKey.FRIEND_ACTION) {
                    val command = FriendsWorkCommand.SendFriendRequest(event.userId)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is FriendsEvent.CancelSendFriendRequest -> {
                launchBackgroundWork(BackgroundKey.FRIEND_ACTION) {
                    val command = FriendsWorkCommand.CancelSendFriendRequest(event.userId)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is FriendsEvent.DeleteFriend -> {
                launchBackgroundWork(BackgroundKey.FRIEND_ACTION) {
                    val command = FriendsWorkCommand.DeleteFriend(event.userId)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is FriendsEvent.ClickUserProfile -> {
                val config = UsersConfig.UserProfile(event.userId)
                consumeOutput(FriendsOutput.NavigateToUserProfile(config))
            }
            is FriendsEvent.ClickShowRequests -> {
                consumeOutput(FriendsOutput.NavigateToRequests)
            }
            is FriendsEvent.ClickBack -> {
                consumeOutput(FriendsOutput.NavigateToBack)
            }
        }
    }

    override suspend fun reduce(
        action: FriendsAction,
        currentState: FriendsState,
    ) = when (action) {
        is FriendsAction.UpdateFriends -> currentState.copy(
            friends = action.friends,
            requests = action.requests,
            isLoading = false,
        )
        is FriendsAction.UpdateSearchedUsers -> currentState.copy(
            searchedUsers = action.users,
            isLoadingSearch = false,
        )
        is FriendsAction.UpdateLoading -> currentState.copy(
            isLoading = action.isLoading
        )
        is FriendsAction.UpdateCurrentTime -> currentState.copy(
            currentTime = action.time,
        )
        is FriendsAction.UpdateSearchLoading -> currentState.copy(
            isLoadingSearch = action.isLoading,
        )
    }

    enum class BackgroundKey : BackgroundWorkKey {
        LOAD_FRIENDS, SEARCH_USERS, FRIEND_ACTION
    }

    class Factory(
        private val workProcessor: FriendsWorkProcessor,
        private val dateManager: DateManager,
        private val coroutineManager: CoroutineManager,
    ) : BaseOnlyOutComposeStore.Factory<FriendsComposeStore, FriendsState> {

        override fun create(savedState: FriendsState): FriendsComposeStore {
            return FriendsComposeStore(
                workProcessor = workProcessor,
                dateManager = dateManager,
                stateCommunicator = StateCommunicator.Default(savedState),
                effectCommunicator = EffectCommunicator.Default(),
                coroutineManager = coroutineManager,
            )
        }
    }
}