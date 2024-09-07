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

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.EffectResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.FlowWorkProcessor
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkCommand
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.handle
import ru.aleshin.studyassistant.users.impl.domain.interactors.FriendRequestsInteractor
import ru.aleshin.studyassistant.users.impl.domain.interactors.UsersInteractor
import ru.aleshin.studyassistant.users.impl.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.users.impl.presentation.ui.user.contract.UserProfileAction
import ru.aleshin.studyassistant.users.impl.presentation.ui.user.contract.UserProfileEffect

/**
 * @author Stanislav Aleshin on 15.07.2024.
 */
internal interface UserProfileWorkProcessor :
    FlowWorkProcessor<UserProfileWorkCommand, UserProfileAction, UserProfileEffect> {

    class Base(
        private val usersInteractor: UsersInteractor,
        private val requestsInteractor: FriendRequestsInteractor,
    ) : UserProfileWorkProcessor {

        override suspend fun work(command: UserProfileWorkCommand) = when (command) {
            is UserProfileWorkCommand.LoadUser -> loadUserWork(command.userId)
            is UserProfileWorkCommand.SendFriendRequest -> sendFriendRequestWork(command.userId)
            is UserProfileWorkCommand.AcceptFriendRequest -> acceptFriendRequestWork(command.userId)
            is UserProfileWorkCommand.CancelSendFriendRequest -> cancelSendFriendRequestWork(command.userId)
            is UserProfileWorkCommand.DeleteFriend -> deleteFriendWork(command.userId)
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        private fun loadUserWork(userId: UID) = flow {
            val userInfo = usersInteractor.fetchUserById(userId)
            val userFriendStatus = usersInteractor.fetchUserFriendStatus(userId)

            userInfo.flatMapLatestWithResult(
                secondFlow = userFriendStatus,
                onError = { UserProfileEffect.ShowError(it) },
                onData = { user, status ->
                    UserProfileAction.UpdateUser(user.mapToUi(), status)
                },
            ).collect { workResult ->
                emit(workResult)
            }
        }

        private fun sendFriendRequestWork(userId: UID) = flow {
            requestsInteractor.sendRequest(userId).handle(
                onLeftAction = { emit(EffectResult(UserProfileEffect.ShowError(it))) },
            )
        }

        private fun acceptFriendRequestWork(userId: UID) = flow {
            usersInteractor.addUserToFriends(userId).handle(
                onLeftAction = { emit(EffectResult(UserProfileEffect.ShowError(it))) },
                onRightAction = {
                    requestsInteractor.acceptRequest(userId).handle(
                        onLeftAction = { emit(EffectResult(UserProfileEffect.ShowError(it))) },
                    )
                },
            )
        }

        private fun cancelSendFriendRequestWork(userId: UID) = flow {
            requestsInteractor.cancelSendRequest(userId).handle(
                onLeftAction = { emit(EffectResult(UserProfileEffect.ShowError(it))) },
            )
        }
        private fun deleteFriendWork(userId: UID) = flow {
            usersInteractor.removeUserFromFriends(userId).handle(
                onLeftAction = { emit(EffectResult(UserProfileEffect.ShowError(it))) },
            )
        }
    }
}

internal sealed class UserProfileWorkCommand : WorkCommand {
    data class LoadUser(val userId: UID) : UserProfileWorkCommand()
    data class SendFriendRequest(val userId: UID) : UserProfileWorkCommand()
    data class AcceptFriendRequest(val userId: UID) : UserProfileWorkCommand()
    data class CancelSendFriendRequest(val userId: UID) : UserProfileWorkCommand()
    data class DeleteFriend(val userId: UID) : UserProfileWorkCommand()
}