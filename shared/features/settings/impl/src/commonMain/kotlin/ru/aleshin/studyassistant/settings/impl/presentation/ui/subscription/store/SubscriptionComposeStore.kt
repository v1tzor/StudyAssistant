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

package ru.aleshin.studyassistant.settings.impl.presentation.ui.subscription.store

import ru.aleshin.studyassistant.core.common.architecture.component.EmptyInput
import ru.aleshin.studyassistant.core.common.architecture.store.BaseOnlyOutComposeStore
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.EffectCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.StateCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.settings.impl.presentation.ui.subscription.contract.SubscriptionAction
import ru.aleshin.studyassistant.settings.impl.presentation.ui.subscription.contract.SubscriptionEffect
import ru.aleshin.studyassistant.settings.impl.presentation.ui.subscription.contract.SubscriptionEvent
import ru.aleshin.studyassistant.settings.impl.presentation.ui.subscription.contract.SubscriptionOutput
import ru.aleshin.studyassistant.settings.impl.presentation.ui.subscription.contract.SubscriptionState

/**
 * @author Stanislav Aleshin on 28.08.2024
 */
internal class SubscriptionComposeStore(
    private val workProcessor: SubscriptionWorkProcessor,
    stateCommunicator: StateCommunicator<SubscriptionState>,
    effectCommunicator: EffectCommunicator<SubscriptionEffect>,
    coroutineManager: CoroutineManager,
) : BaseOnlyOutComposeStore<SubscriptionState, SubscriptionEvent, SubscriptionAction, SubscriptionEffect, SubscriptionOutput>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun initialize(input: EmptyInput, isRestore: Boolean) {
        dispatchEvent(SubscriptionEvent.Init)
    }

    override suspend fun WorkScope<SubscriptionState, SubscriptionAction, SubscriptionEffect, SubscriptionOutput>.handleEvent(
        event: SubscriptionEvent,
    ) {
        when (event) {
            is SubscriptionEvent.Init -> {
                workProcessor.work(SubscriptionWorkCommand.LoadCurrentStore).collectAndHandleWork()
                launchBackgroundWork(BackgroundKey.LOAD_SUBSCRIPTIONS) {
                    val command = SubscriptionWorkCommand.LoadSubscriptions
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_PAID_STATUS) {
                    val command = SubscriptionWorkCommand.LoadUserPaidStatus
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_REMOTE_STATUS) {
                    val command = SubscriptionWorkCommand.LoadRemoteDataStatus
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is SubscriptionEvent.TransferRemoteData -> with(event) {
                launchBackgroundWork(BackgroundKey.TRANSFER_DATA) {
                    val command = SubscriptionWorkCommand.TransferRemoteData(mergeData)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is SubscriptionEvent.TransferLocalData -> with(event) {
                launchBackgroundWork(BackgroundKey.TRANSFER_DATA) {
                    val command = SubscriptionWorkCommand.TransferLocalData(mergeData)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is SubscriptionEvent.ControlSubscription -> {
                state().currentStore?.getSubscriptionsUri()?.let {
                    sendEffect(SubscriptionEffect.OpenUri(it))
                }
            }
            is SubscriptionEvent.RestoreSubscription -> {
                launchBackgroundWork(BackgroundKey.SUBSCRIPTION_ACTION) {
                    val command = SubscriptionWorkCommand.RestoreSubscription
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is SubscriptionEvent.NavigateToBilling -> {
                consumeOutput(SubscriptionOutput.NavigateToBilling)
            }
        }
    }

    override suspend fun reduce(
        action: SubscriptionAction,
        currentState: SubscriptionState,
    ) = when (action) {
        is SubscriptionAction.UpdateUserPaidStatus -> currentState.copy(
            isPaidUser = action.isPaidUser,
        )
        is SubscriptionAction.UpdateRemoteDataStatus -> currentState.copy(
            haveRemoteData = action.haveRemoteData,
        )
        is SubscriptionAction.UpdateLoadingSync -> currentState.copy(
            isLoadingSync = action.isLoading,
        )
        is SubscriptionAction.UpdateCurrentStore -> currentState.copy(
            currentStore = action.store,
        )
        is SubscriptionAction.UpdateSubscriptions -> currentState.copy(
            subscriptions = action.subscriptions,
            isLoadingSubscriptions = false,
        )
        is SubscriptionAction.UpdateLoadingSubscriptions -> currentState.copy(
            isLoadingSubscriptions = action.isLoading,
        )
    }

    enum class BackgroundKey : BackgroundWorkKey {
        LOAD_PAID_STATUS, LOAD_REMOTE_STATUS, LOAD_SUBSCRIPTIONS, SUBSCRIPTION_ACTION, TRANSFER_DATA
    }

    class Factory(
        private val workProcessor: SubscriptionWorkProcessor,
        private val coroutineManager: CoroutineManager,
    ) : BaseOnlyOutComposeStore.Factory<SubscriptionComposeStore, SubscriptionState> {

        override fun create(savedState: SubscriptionState): SubscriptionComposeStore {
            return SubscriptionComposeStore(
                workProcessor = workProcessor,
                stateCommunicator = StateCommunicator.Default(savedState),
                effectCommunicator = EffectCommunicator.Default(),
                coroutineManager = coroutineManager,
            )
        }
    }
}