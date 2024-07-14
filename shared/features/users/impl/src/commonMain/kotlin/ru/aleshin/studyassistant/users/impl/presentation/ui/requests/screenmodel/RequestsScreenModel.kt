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
import ru.aleshin.studyassistant.users.impl.presentation.ui.requests.contract.RequestsAction
import ru.aleshin.studyassistant.users.impl.presentation.ui.requests.contract.RequestsEffect
import ru.aleshin.studyassistant.users.impl.presentation.ui.requests.contract.RequestsEvent
import ru.aleshin.studyassistant.users.impl.presentation.ui.requests.contract.RequestsViewState

/**
 * @author Stanislav Aleshin on 13.07.2024
 */
internal class RequestsScreenModel(
    private val workProcessor: RequestsWorkProcessor,
    private val screenProvider: UsersScreenProvider,
    private val dateManager: DateManager,
    stateCommunicator: RequestsStateCommunicator,
    effectCommunicator: RequestsEffectCommunicator,
    coroutineManager: CoroutineManager,
) : BaseScreenModel<RequestsViewState, RequestsEvent, RequestsAction, RequestsEffect, EmptyDeps>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun init(deps: EmptyDeps) {
        if (!isInitialize) {
            super.init(deps)
            dispatchEvent(RequestsEvent.Init)
        }
    }

    override suspend fun WorkScope<RequestsViewState, RequestsAction, RequestsEffect>.handleEvent(
        event: RequestsEvent,
    ) {
        when (event) {
            is RequestsEvent.Init -> {
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
            is RequestsEvent.DeleteHistoryRequest -> {
                launchBackgroundWork(BackgroundKey.REQUEST_ACTION) {
                    val command = RequestsWorkCommand.DeleteHistoryRequest(event.userId)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is RequestsEvent.NavigateToFriendProfile -> {
                val featureScreen = UsersScreen.UserProfile(event.userId)
                val screen = screenProvider.provideFeatureScreen(featureScreen)
                sendEffect(RequestsEffect.NavigateToLocal(screen))
            }
            is RequestsEvent.NavigateToBack -> {
                sendEffect(RequestsEffect.NavigateToBack)
            }
        }
    }

    override suspend fun reduce(
        action: RequestsAction,
        currentState: RequestsViewState,
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
}

@Composable
internal fun Screen.rememberRequestsScreenModel(): RequestsScreenModel {
    val di = UsersFeatureDIHolder.fetchDI()
    return rememberScreenModel { di.instance<RequestsScreenModel>() }
}