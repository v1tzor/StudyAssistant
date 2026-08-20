/*
 * Copyright 2026 Stanislav Aleshin
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

package ru.aleshin.studyassistant.chat.impl.presentation.ui.assistant.store

import ru.aleshin.studyassistant.chat.impl.domain.entities.ChatFailures
import ru.aleshin.studyassistant.chat.impl.presentation.models.ai.ResponseStatus
import ru.aleshin.studyassistant.chat.impl.presentation.ui.assistant.contract.AssistantAction
import ru.aleshin.studyassistant.chat.impl.presentation.ui.assistant.contract.AssistantEffect
import ru.aleshin.studyassistant.chat.impl.presentation.ui.assistant.contract.AssistantEvent
import ru.aleshin.studyassistant.chat.impl.presentation.ui.assistant.contract.AssistantOutput
import ru.aleshin.studyassistant.chat.impl.presentation.ui.assistant.contract.AssistantState
import ru.aleshin.studyassistant.core.common.architecture.component.EmptyInput
import ru.aleshin.studyassistant.core.common.architecture.store.BaseOnlyOutComposeStore
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.EffectCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.StateCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager

/**
 * @author Stanislav Aleshin on 20.06.2025
 */
internal class AssistantComposeStore(
    private val workProcessor: AssistantWorkProcessor,
    stateCommunicator: StateCommunicator<AssistantState>,
    effectCommunicator: EffectCommunicator<AssistantEffect>,
    coroutineManager: CoroutineManager,
) : BaseOnlyOutComposeStore<AssistantState, AssistantEvent, AssistantAction, AssistantEffect, AssistantOutput>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun initialize(input: EmptyInput, isRestore: Boolean) {
        dispatchEvent(AssistantEvent.Started)
    }

    override suspend fun WorkScope<AssistantState, AssistantAction, AssistantEffect, AssistantOutput>.handleEvent(
        event: AssistantEvent,
    ) {
        when (event) {
            is AssistantEvent.Started -> {
                launchBackgroundWork(BackgroundKey.LOAD_MESSAGES) {
                    val command = AssistantWorkCommand.LoadMessages
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_AI_SETTINGS) {
                    val command = AssistantWorkCommand.LoadAiSettings
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is AssistantEvent.SendMessage -> with(event) {
                val chatId = state().chatHistory?.uid
                if (chatId != null) {
                    launchBackgroundWork(BackgroundKey.SEND_MESSAGE) {
                        val command = AssistantWorkCommand.SendMessage(chatId, message)
                        workProcessor.work(command).collectAndHandleWork()
                    }
                }
            }
            is AssistantEvent.RetryAttempt -> {
                val chatId = state().chatHistory?.uid
                if (chatId != null) {
                    launchBackgroundWork(BackgroundKey.SEND_MESSAGE) {
                        val command = AssistantWorkCommand.RetryAttempt(chatId)
                        workProcessor.work(command).collectAndHandleWork()
                    }
                }
            }
            is AssistantEvent.ClearUnsendMessage -> {
                val chatId = state().chatHistory?.uid
                if (chatId != null) {
                    launchBackgroundWork(BackgroundKey.MESSAGE_ACTION) {
                        val command = AssistantWorkCommand.ClearUnsendMessage(chatId)
                        workProcessor.work(command).collectAndHandleWork()
                    }
                }
            }
            is AssistantEvent.ClearHistory -> {
                val chatId = state().chatHistory?.uid
                if (chatId != null) {
                    sendAction(AssistantAction.UpdateResponseStatus(ResponseStatus.SUCCESS))
                    launchBackgroundWork(BackgroundKey.SEND_MESSAGE) {
                        sendAction(AssistantAction.UpdateResponseStatus(ResponseStatus.SUCCESS))
                    }
                    launchBackgroundWork(BackgroundKey.MESSAGE_ACTION) {
                        val command = AssistantWorkCommand.ClearChatHistory(chatId)
                        workProcessor.work(command).collectAndHandleWork()
                    }
                }
            }
            is AssistantEvent.UpdateUserQuery -> with(event) {
                val query = state().userQuery.copy(query)
                sendAction(AssistantAction.UpdateUserQuery(query))
            }
            is AssistantEvent.OpenAiSettings -> {
                consumeOutput(AssistantOutput.NavigateToAiSettings)
            }
            is AssistantEvent.OpenScheduleImport -> {
                consumeOutput(AssistantOutput.NavigateToScheduleImport)
            }
            is AssistantEvent.RequestQuotaReward -> with(state()) {
                if (isQuotaExpired && rewardedResetsRemaining > 0 && !isRewardInProgress) {
                    launchBackgroundWork(BackgroundKey.REWARD_ACTION) {
                        val command = AssistantWorkCommand.PrepareQuotaReward
                        workProcessor.work(command).collectAndHandleWork()
                    }
                }
            }
            is AssistantEvent.RewardedAdGranted -> with(state()) {
                if (event.challengeId == rewardChallengeId) {
                    launchBackgroundWork(BackgroundKey.REWARD_ACTION) {
                        val command = AssistantWorkCommand.CompleteQuotaReward(event.challengeId)
                        workProcessor.work(command).collectAndHandleWork()
                    }
                }
            }
            is AssistantEvent.RewardedAdUnavailable -> {
                sendAction(AssistantAction.UpdateRewardChallenge(null, false))
                sendEffect(AssistantEffect.ShowError(ChatFailures.RewardUnavailable))
            }
            is AssistantEvent.ResolveToolCall -> with(event) {
                val chatId = state().chatHistory?.uid
                if (chatId != null && state().responseStatus != ResponseStatus.LOADING) {
                    launchBackgroundWork(ToolConfirmationKey(toolCallId)) {
                        val command = AssistantWorkCommand.ResolveToolCall(
                            chatId = chatId,
                            toolCallId = toolCallId,
                            approved = approved,
                        )
                        workProcessor.work(command).collectAndHandleWork()
                    }
                }
            }
            is AssistantEvent.StopResponseLoading -> {
                sendAction(AssistantAction.UpdateResponseStatus(ResponseStatus.FAILURE))
            }
        }
    }

    override suspend fun reduce(
        action: AssistantAction,
        currentState: AssistantState,
    ) = when (action) {
        is AssistantAction.UpdateLoadingChat -> currentState.copy(
            isLoadingChat = action.isLoading,
        )

        is AssistantAction.UpdateUserQuery -> currentState.copy(
            userQuery = action.query,
        )

        is AssistantAction.UpdateResponseStatus -> currentState.copy(
            responseStatus = action.responseStatus,
        )

        is AssistantAction.UpdateChatHistory -> currentState.copy(
            chatHistory = action.chatHistory,
            isLoadingChat = false,
        )

        is AssistantAction.SetupQuota -> currentState.copy(
            quotaRemaining = action.remaining,
            quotaLimit = action.limit,
            rewardedResetsRemaining = action.rewardedResetsRemaining,
        )

        is AssistantAction.UpdateRewardChallenge -> currentState.copy(
            rewardChallengeId = action.challengeId,
            isRewardInProgress = action.isInProgress,
        )
    }

    enum class BackgroundKey : BackgroundWorkKey {
        LOAD_MESSAGES, LOAD_AI_SETTINGS, SEND_MESSAGE, MESSAGE_ACTION, REWARD_ACTION
    }

    data class ToolConfirmationKey(val toolCallId: String) : BackgroundWorkKey

    class Factory(
        private val workProcessor: AssistantWorkProcessor,
        private val coroutineManager: CoroutineManager,
    ) : BaseOnlyOutComposeStore.Factory<AssistantComposeStore, AssistantState> {

        override fun create(savedState: AssistantState): AssistantComposeStore {
            return AssistantComposeStore(
                workProcessor = workProcessor,
                stateCommunicator = StateCommunicator.Default(savedState),
                effectCommunicator = EffectCommunicator.Default(),
                coroutineManager = coroutineManager,
            )
        }
    }
}
