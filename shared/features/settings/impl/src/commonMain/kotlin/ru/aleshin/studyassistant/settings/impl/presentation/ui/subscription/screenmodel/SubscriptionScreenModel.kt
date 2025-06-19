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

package ru.aleshin.studyassistant.settings.impl.presentation.ui.subscription.screenmodel

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import org.kodein.di.instance
import ru.aleshin.studyassistant.billing.api.navigation.BillingScreen
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.BaseScreenModel
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.EmptyDeps
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.settings.impl.di.holder.SettingsFeatureDIHolder
import ru.aleshin.studyassistant.settings.impl.navigation.SettingsScreenProvider
import ru.aleshin.studyassistant.settings.impl.presentation.ui.subscription.contract.SubscriptionAction
import ru.aleshin.studyassistant.settings.impl.presentation.ui.subscription.contract.SubscriptionEffect
import ru.aleshin.studyassistant.settings.impl.presentation.ui.subscription.contract.SubscriptionEffect.NavigateToGlobal
import ru.aleshin.studyassistant.settings.impl.presentation.ui.subscription.contract.SubscriptionEvent
import ru.aleshin.studyassistant.settings.impl.presentation.ui.subscription.contract.SubscriptionViewState

/**
 * @author Stanislav Aleshin on 28.08.2024
 */
internal class SubscriptionScreenModel(
    private val workProcessor: SubscriptionWorkProcessor,
    private val screenProvider: SettingsScreenProvider,
    stateCommunicator: SubscriptionStateCommunicator,
    effectCommunicator: SubscriptionEffectCommunicator,
    coroutineManager: CoroutineManager,
) : BaseScreenModel<SubscriptionViewState, SubscriptionEvent, SubscriptionAction, SubscriptionEffect, EmptyDeps>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun init(deps: EmptyDeps) {
        if (!isInitialize) {
            super.init(deps)
            dispatchEvent(SubscriptionEvent.Init)
        }
    }

    override suspend fun WorkScope<SubscriptionViewState, SubscriptionAction, SubscriptionEffect>.handleEvent(
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
            is SubscriptionEvent.TransferRemoteData -> {
                launchBackgroundWork(BackgroundKey.TRANSFER_DATA) {
                    val command = SubscriptionWorkCommand.TransferRemoteData
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is SubscriptionEvent.TransferLocalData -> {
                launchBackgroundWork(BackgroundKey.TRANSFER_DATA) {
                    val command = SubscriptionWorkCommand.TransferLocalData
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
                val screen = screenProvider.provideBillingScreen(BillingScreen.Subscription)
                sendEffect(NavigateToGlobal(screen))
            }
        }
    }

    override suspend fun reduce(
        action: SubscriptionAction,
        currentState: SubscriptionViewState,
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
}

@Composable
internal fun Screen.rememberSubscriptionScreenModel(): SubscriptionScreenModel {
    val di = SettingsFeatureDIHolder.fetchDI()
    return rememberScreenModel { di.instance<SubscriptionScreenModel>() }
}