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

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import ru.aleshin.studyassistant.core.common.architecture.store.work.ActionResult
import ru.aleshin.studyassistant.core.common.architecture.store.work.EffectResult
import ru.aleshin.studyassistant.core.common.architecture.store.work.FlowWorkProcessor
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkCommand
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkResult
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.collectAndHandle
import ru.aleshin.studyassistant.core.common.functional.handle
import ru.aleshin.studyassistant.users.impl.domain.interactors.FriendRequestsInteractor
import ru.aleshin.studyassistant.users.impl.domain.interactors.UsersInteractor
import ru.aleshin.studyassistant.users.impl.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.users.impl.presentation.ui.friends.contract.FriendsAction
import ru.aleshin.studyassistant.users.impl.presentation.ui.friends.contract.FriendsEffect
import ru.aleshin.studyassistant.users.impl.presentation.ui.friends.contract.FriendsOutput

/**
 * @author Stanislav Aleshin on 12.07.2024.
 */
internal interface FriendsWorkProcessor :
    FlowWorkProcessor<FriendsWorkCommand, FriendsAction, FriendsEffect, FriendsOutput> {

    class Base(
        private val usersInteractor: UsersInteractor,
        private val requestsInteractor: FriendRequestsInteractor,
    ) : FriendsWorkProcessor {

        override suspend fun work(command: FriendsWorkCommand) = when (command) {
            is FriendsWorkCommand.LoadFriendsAndRequests -> loadFriendsAndRequestsWork()
            is FriendsWorkCommand.SearchUsersByCode -> searchUsersByCodeWork(command.code)
            is FriendsWorkCommand.AcceptFriendRequest -> acceptFriendRequestWork(command.userId)
            is FriendsWorkCommand.RejectFriendRequest -> rejectFriendRequestWork(command.userId)
            is FriendsWorkCommand.SendFriendRequest -> sendFriendRequestWork(command.userId)
            is FriendsWorkCommand.CancelSendFriendRequest -> cancelSendFriendRequestWork(command.userId)
            is FriendsWorkCommand.DeleteFriend -> deleteFriendWork(command.userId)
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        private fun loadFriendsAndRequestsWork() = flow {
            val friendsFlow = usersInteractor.fetchAllFriends()
            val friendRequestsFlow = requestsInteractor.fetchUserFriendRequests()

            friendsFlow.flatMapLatestWithResult(
                secondFlow = friendRequestsFlow,
                onError = { FriendsEffect.ShowError(it) },
                onData = { friendList, requests ->
                    val friends = friendList.map { it.mapToUi() }
                    val alphabeticFriends = friends.groupBy { it.username.first() }
                    val friendRequests = requests.mapToUi()

                    return@flatMapLatestWithResult FriendsAction.UpdateFriends(
                        friends = alphabeticFriends,
                        requests = friendRequests,
                    )
                }
            ).collect { workResult ->
                emit(workResult)
            }
        }

        private fun searchUsersByCodeWork(code: String) = flow<FriendsWorkResult> {
            if (code.isNotBlank()) {
                usersInteractor.findUsersByCode(code).collectAndHandle(
                    onLeftAction = { emit(EffectResult(FriendsEffect.ShowError(it))) },
                    onRightAction = { users ->
                        val usersResult = users.map { it.mapToUi() }
                        emit(ActionResult(FriendsAction.UpdateSearchedUsers(usersResult)))
                    },
                )
            } else {
                emit(ActionResult(FriendsAction.UpdateSearchedUsers(emptyList())))
            }
        }.onStart {
            emit(ActionResult(FriendsAction.UpdateSearchLoading(true)))
        }

        private fun acceptFriendRequestWork(userId: UID) = flow {
            usersInteractor.addUserToFriends(userId).handle(
                onLeftAction = { emit(EffectResult(FriendsEffect.ShowError(it))) },
                onRightAction = {
                    requestsInteractor.acceptRequest(userId).handle(
                        onLeftAction = { emit(EffectResult(FriendsEffect.ShowError(it))) },
                    )
                },
            )
        }

        private fun rejectFriendRequestWork(userId: UID) = flow {
            requestsInteractor.rejectRequest(userId).handle(
                onLeftAction = { emit(EffectResult(FriendsEffect.ShowError(it))) },
            )
        }

        private fun sendFriendRequestWork(userId: UID) = flow {
            requestsInteractor.sendRequest(userId).handle(
                onLeftAction = { emit(EffectResult(FriendsEffect.ShowError(it))) },
            )
        }

        private fun cancelSendFriendRequestWork(userId: UID) = flow {
            requestsInteractor.cancelSendRequest(userId).handle(
                onLeftAction = { emit(EffectResult(FriendsEffect.ShowError(it))) },
            )
        }

        private fun deleteFriendWork(userId: UID) = flow {
            usersInteractor.removeUserFromFriends(userId).handle(
                onLeftAction = { emit(EffectResult(FriendsEffect.ShowError(it))) },
            )
        }
    }
}

internal sealed class FriendsWorkCommand : WorkCommand {
    data object LoadFriendsAndRequests : FriendsWorkCommand()
    data class SearchUsersByCode(val code: String) : FriendsWorkCommand()
    data class AcceptFriendRequest(val userId: UID) : FriendsWorkCommand()
    data class RejectFriendRequest(val userId: UID) : FriendsWorkCommand()
    data class SendFriendRequest(val userId: UID) : FriendsWorkCommand()
    data class CancelSendFriendRequest(val userId: UID) : FriendsWorkCommand()
    data class DeleteFriend(val userId: UID) : FriendsWorkCommand()
}

internal typealias FriendsWorkResult = WorkResult<FriendsAction, FriendsEffect, FriendsOutput>