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

package ru.aleshin.studyassistant.users.impl.presentation.ui.requests.screenmodel

import kotlinx.coroutines.flow.flow
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.ActionResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.EffectResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.FlowWorkProcessor
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkCommand
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.collectAndHandle
import ru.aleshin.studyassistant.core.common.functional.handle
import ru.aleshin.studyassistant.users.impl.domain.interactors.FriendRequestsInteractor
import ru.aleshin.studyassistant.users.impl.domain.interactors.UsersInteractor
import ru.aleshin.studyassistant.users.impl.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.users.impl.presentation.ui.requests.contract.RequestsAction
import ru.aleshin.studyassistant.users.impl.presentation.ui.requests.contract.RequestsEffect

/**
 * @author Stanislav Aleshin on 12.07.2024.
 */
internal interface RequestsWorkProcessor :
    FlowWorkProcessor<RequestsWorkCommand, RequestsAction, RequestsEffect> {

    class Base(
        private val usersInteractor: UsersInteractor,
        private val requestsInteractor: FriendRequestsInteractor,
    ) : RequestsWorkProcessor {

        override suspend fun work(command: RequestsWorkCommand) = when (command) {
            is RequestsWorkCommand.LoadRequests -> loadRequestsWork()
            is RequestsWorkCommand.AcceptFriendRequest -> acceptFriendRequestWork(command.userId)
            is RequestsWorkCommand.RejectFriendRequest -> rejectFriendRequestWork(command.userId)
            is RequestsWorkCommand.CancelFriendRequest -> cancelFriendRequestWork(command.userId)
            is RequestsWorkCommand.DeleteHistoryRequest -> deleteHistoryRequestWork(command.userId)
        }

        private fun loadRequestsWork() = flow {
            requestsInteractor.fetchUserFriendRequests().collectAndHandle(
                onLeftAction = { emit(EffectResult(RequestsEffect.ShowError(it))) },
                onRightAction = { requests ->
                    emit(ActionResult(RequestsAction.UpdateRequests(requests.mapToUi())))
                }
            )
        }

        private fun acceptFriendRequestWork(userId: UID) = flow {
            usersInteractor.addUserToFriends(userId).handle(
                onLeftAction = { emit(EffectResult(RequestsEffect.ShowError(it))) },
                onRightAction = {
                    requestsInteractor.acceptRequest(userId).handle(
                        onLeftAction = { emit(EffectResult(RequestsEffect.ShowError(it))) },
                    )
                },
            )
        }

        private fun rejectFriendRequestWork(userId: UID) = flow {
            requestsInteractor.rejectRequest(userId).handle(
                onLeftAction = { emit(EffectResult(RequestsEffect.ShowError(it))) },
            )
        }

        private fun cancelFriendRequestWork(userId: UID) = flow {
            requestsInteractor.cancelSendRequest(userId).handle(
                onLeftAction = { emit(EffectResult(RequestsEffect.ShowError(it))) },
            )
        }

        private fun deleteHistoryRequestWork(userId: UID) = flow {
            requestsInteractor.deleteHistoryRequestByUser(userId).handle(
                onLeftAction = { emit(EffectResult(RequestsEffect.ShowError(it))) },
            )
        }
    }
}

internal sealed class RequestsWorkCommand : WorkCommand {
    data object LoadRequests : RequestsWorkCommand()
    data class AcceptFriendRequest(val userId: UID) : RequestsWorkCommand()
    data class RejectFriendRequest(val userId: UID) : RequestsWorkCommand()
    data class CancelFriendRequest(val userId: UID) : RequestsWorkCommand()
    data class DeleteHistoryRequest(val userId: UID) : RequestsWorkCommand()
}