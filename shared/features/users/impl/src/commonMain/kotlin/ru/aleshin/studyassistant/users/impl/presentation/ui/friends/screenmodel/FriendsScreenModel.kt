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

package ru.aleshin.studyassistant.users.impl.presentation.ui.friends.screenmodel

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.BaseScreenModel
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.EmptyDeps
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.users.api.navigation.UsersScreen
import ru.aleshin.studyassistant.users.impl.di.holder.UsersFeatureDIHolder
import ru.aleshin.studyassistant.users.impl.navigation.UsersScreenProvider
import ru.aleshin.studyassistant.users.impl.presentation.ui.friends.contract.FriendsAction
import ru.aleshin.studyassistant.users.impl.presentation.ui.friends.contract.FriendsEffect
import ru.aleshin.studyassistant.users.impl.presentation.ui.friends.contract.FriendsEvent
import ru.aleshin.studyassistant.users.impl.presentation.ui.friends.contract.FriendsViewState

/**
 * @author Stanislav Aleshin on 12.07.2024
 */
internal class FriendsScreenModel(
    private val workProcessor: FriendsWorkProcessor,
    private val screenProvider: UsersScreenProvider,
    private val dateManager: DateManager,
    stateCommunicator: FriendsStateCommunicator,
    effectCommunicator: FriendsEffectCommunicator,
    coroutineManager: CoroutineManager,
) : BaseScreenModel<FriendsViewState, FriendsEvent, FriendsAction, FriendsEffect, EmptyDeps>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun init(deps: EmptyDeps) {
        if (!isInitialize) {
            super.init(deps)
            dispatchEvent(FriendsEvent.Init)
        }
    }

    override suspend fun WorkScope<FriendsViewState, FriendsAction, FriendsEffect>.handleEvent(
        event: FriendsEvent,
    ) {
        when (event) {
            is FriendsEvent.Init -> {
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
            is FriendsEvent.NavigateToFriendProfile -> {
                val featureScreen = UsersScreen.UserProfile(event.userId)
                val screen = screenProvider.provideFeatureScreen(featureScreen)
                sendEffect(FriendsEffect.NavigateToLocal(screen))
            }
            is FriendsEvent.NavigateToRequests -> {
                val featureScreen = UsersScreen.Requests
                val screen = screenProvider.provideFeatureScreen(featureScreen)
                sendEffect(FriendsEffect.NavigateToLocal(screen))
            }
            is FriendsEvent.NavigateToBack -> {
                sendEffect(FriendsEffect.NavigateToBack)
            }
        }
    }

    override suspend fun reduce(
        action: FriendsAction,
        currentState: FriendsViewState,
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
}

@Composable
internal fun Screen.rememberFriendsScreenModel(): FriendsScreenModel {
    val di = UsersFeatureDIHolder.fetchDI()
    return rememberScreenModel { di.instance<FriendsScreenModel>() }
}