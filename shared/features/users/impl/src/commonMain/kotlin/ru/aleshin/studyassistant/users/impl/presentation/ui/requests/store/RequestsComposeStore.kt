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

package ru.aleshin.studyassistant.users.impl.presentation.ui.requests.store

import ru.aleshin.studyassistant.core.common.architecture.component.EmptyInput
import ru.aleshin.studyassistant.core.common.architecture.store.BaseOnlyOutComposeStore
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.EffectCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.StateCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.users.api.UsersFeatureComponent.UsersConfig
import ru.aleshin.studyassistant.users.impl.presentation.ui.requests.contract.RequestsAction
import ru.aleshin.studyassistant.users.impl.presentation.ui.requests.contract.RequestsEffect
import ru.aleshin.studyassistant.users.impl.presentation.ui.requests.contract.RequestsEvent
import ru.aleshin.studyassistant.users.impl.presentation.ui.requests.contract.RequestsOutput
import ru.aleshin.studyassistant.users.impl.presentation.ui.requests.contract.RequestsState

/**
 * @author Stanislav Aleshin on 13.07.2024
 */
internal class RequestsComposeStore(
    private val workProcessor: RequestsWorkProcessor,
    private val dateManager: DateManager,
    stateCommunicator: StateCommunicator<RequestsState>,
    effectCommunicator: EffectCommunicator<RequestsEffect>,
    coroutineManager: CoroutineManager,
) : BaseOnlyOutComposeStore<RequestsState, RequestsEvent, RequestsAction, RequestsEffect, RequestsOutput>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun initialize(input: EmptyInput, isRestore: Boolean) {
        dispatchEvent(RequestsEvent.Started)
    }

    override suspend fun WorkScope<RequestsState, RequestsAction, RequestsEffect, RequestsOutput>.handleEvent(
        event: RequestsEvent,
    ) {
        when (event) {
            is RequestsEvent.Started -> {
                sendAction(RequestsAction.UpdateCurrentTime(dateManager.fetchCurrentInstant()))
                launchBackgroundWork(BackgroundKey.LOAD_REQUESTS) {
                    val command = RequestsWorkCommand.LoadRequests
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is RequestsEvent.AcceptFriendRequest -> {
                launchBackgroundWork(BackgroundKey.REQUEST_ACTION) {
                    val command = RequestsWorkCommand.AcceptFriendRequest(event.userId)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is RequestsEvent.RejectFriendRequest -> {
                launchBackgroundWork(BackgroundKey.REQUEST_ACTION) {
                    val command = RequestsWorkCommand.RejectFriendRequest(event.userId)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is RequestsEvent.CancelSendFriendRequest -> {
                launchBackgroundWork(BackgroundKey.REQUEST_ACTION) {
                    val command = RequestsWorkCommand.CancelFriendRequest(event.userId)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is RequestsEvent.ClickDeleteHistoryRequest -> {
                launchBackgroundWork(BackgroundKey.REQUEST_ACTION) {
                    val command = RequestsWorkCommand.DeleteHistoryRequest(event.userId)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is RequestsEvent.ClickUserProfile -> {
                val config = UsersConfig.UserProfile(event.userId)
                consumeOutput(RequestsOutput.NavigateToUserProfile(config))
            }
            is RequestsEvent.ClickBack -> {
                consumeOutput(RequestsOutput.NavigateToBack)
            }
        }
    }

    override suspend fun reduce(
        action: RequestsAction,
        currentState: RequestsState,
    ) = when (action) {
        is RequestsAction.UpdateRequests -> currentState.copy(
            requests = action.requests,
            isLoading = false,
        )
        is RequestsAction.UpdateCurrentTime -> currentState.copy(
            currentTime = action.time,
        )
        is RequestsAction.UpdateLoading -> currentState.copy(
            isLoading = action.isLoading,
        )
    }

    enum class BackgroundKey : BackgroundWorkKey {
        LOAD_REQUESTS, REQUEST_ACTION
    }

    class Factory(
        private val workProcessor: RequestsWorkProcessor,
        private val dateManager: DateManager,
        private val coroutineManager: CoroutineManager,
    ) : BaseOnlyOutComposeStore.Factory<RequestsComposeStore, RequestsState> {

        override fun create(savedState: RequestsState): RequestsComposeStore {
            return RequestsComposeStore(
                workProcessor = workProcessor,
                dateManager = dateManager,
                stateCommunicator = StateCommunicator.Default(savedState),
                effectCommunicator = EffectCommunicator.Default(),
                coroutineManager = coroutineManager,
            )
        }
    }
}