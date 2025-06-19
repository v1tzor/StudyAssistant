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

import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.ActionResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.EffectResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.FlowWorkProcessor
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkCommand
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkResult
import ru.aleshin.studyassistant.core.common.functional.collectAndHandle
import ru.aleshin.studyassistant.core.common.functional.handle
import ru.aleshin.studyassistant.settings.impl.domain.entities.SettingsFailures
import ru.aleshin.studyassistant.settings.impl.domain.interactors.AppUserInteractor
import ru.aleshin.studyassistant.settings.impl.domain.interactors.SubscriptionInteractor
import ru.aleshin.studyassistant.settings.impl.domain.interactors.SyncInteractor
import ru.aleshin.studyassistant.settings.impl.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.settings.impl.presentation.ui.subscription.contract.SubscriptionAction
import ru.aleshin.studyassistant.settings.impl.presentation.ui.subscription.contract.SubscriptionEffect

/**
 * @author Stanislav Aleshin on 28.08.2024.
 */
internal interface SubscriptionWorkProcessor :
    FlowWorkProcessor<SubscriptionWorkCommand, SubscriptionAction, SubscriptionEffect> {

    class Base(
        private val syncInteractor: SyncInteractor,
        private val appUserInteractor: AppUserInteractor,
        private val subscriptionInteractor: SubscriptionInteractor,
    ) : SubscriptionWorkProcessor {

        override suspend fun work(command: SubscriptionWorkCommand) = when (command) {
            is SubscriptionWorkCommand.LoadCurrentStore -> loadCurrentStoreWork()
            is SubscriptionWorkCommand.LoadSubscriptions -> loadSubscriptionsWork()
            is SubscriptionWorkCommand.LoadRemoteDataStatus -> loadRemoteDataStatusWork()
            is SubscriptionWorkCommand.LoadUserPaidStatus -> loadUserPaidStatusWork()
            is SubscriptionWorkCommand.TransferRemoteData -> transferRemoteDataWork()
            is SubscriptionWorkCommand.TransferLocalData -> transferLocalDataWork()
            is SubscriptionWorkCommand.RestoreSubscription -> restoreSubscriptionWork()
        }

        private fun loadCurrentStoreWork() = flow {
            val store = subscriptionInteractor.fetchCurrentStore()
            emit(ActionResult(SubscriptionAction.UpdateCurrentStore(store)))
        }

        private fun loadSubscriptionsWork() = flow<SubscriptionWorkResult> {
            subscriptionInteractor.fetchSubscriptions().handle(
                onLeftAction = { emit(EffectResult(SubscriptionEffect.ShowError(it))) },
                onRightAction = { subscriptionsList ->
                    val subscriptions = subscriptionsList.map { it.mapToUi() }
                    emit(ActionResult(SubscriptionAction.UpdateSubscriptions(subscriptions)))
                },
            )
        }.onStart {
            emit(ActionResult(SubscriptionAction.UpdateLoadingSubscriptions(true)))
        }

        private fun loadUserPaidStatusWork() = flow {
            appUserInteractor.fetchAppUserPaidStatus().collectAndHandle(
                onLeftAction = { emit(EffectResult(SubscriptionEffect.ShowError(it))) },
                onRightAction = { emit(ActionResult(SubscriptionAction.UpdateUserPaidStatus(it))) },
            )
        }

        private fun loadRemoteDataStatusWork() = flow {
            appUserInteractor.fetchRemoteDataStatus().collectAndHandle(
                onLeftAction = { emit(EffectResult(SubscriptionEffect.ShowError(it))) },
                onRightAction = { emit(ActionResult(SubscriptionAction.UpdateRemoteDataStatus(it))) },
            )
        }

        private fun transferRemoteDataWork() = flow<SubscriptionWorkResult> {
            syncInteractor.transferRemoteData().handle(
                onLeftAction = { emit(EffectResult(SubscriptionEffect.ShowError(it))) }
            )
        }.onStart {
            emit(ActionResult(SubscriptionAction.UpdateLoadingSync(true)))
        }.onCompletion {
            emit(ActionResult(SubscriptionAction.UpdateLoadingSync(false)))
        }

        private fun transferLocalDataWork() = flow<SubscriptionWorkResult> {
            syncInteractor.transferLocalData().handle(
                onLeftAction = { emit(EffectResult(SubscriptionEffect.ShowError(it))) }
            )
        }.onStart {
            emit(ActionResult(SubscriptionAction.UpdateLoadingSync(true)))
        }.onCompletion {
            emit(ActionResult(SubscriptionAction.UpdateLoadingSync(false)))
        }

        private fun restoreSubscriptionWork() = flow<SubscriptionWorkResult> {
            subscriptionInteractor.restoreSubscription().handle(
                onLeftAction = { emit(EffectResult(SubscriptionEffect.ShowError(it))) },
                onRightAction = { isSuccess ->
                    if (isSuccess) {
                        emit(EffectResult(SubscriptionEffect.SuccessRestoreMessage))
                    } else {
                        emit(EffectResult(SubscriptionEffect.ShowError(SettingsFailures.RestoreError)))
                    }
                },
            )
        }
    }
}

internal sealed class SubscriptionWorkCommand : WorkCommand {
    data object LoadCurrentStore : SubscriptionWorkCommand()
    data object LoadSubscriptions : SubscriptionWorkCommand()
    data object LoadUserPaidStatus : SubscriptionWorkCommand()
    data object LoadRemoteDataStatus : SubscriptionWorkCommand()
    data object TransferRemoteData : SubscriptionWorkCommand()
    data object TransferLocalData : SubscriptionWorkCommand()
    data object RestoreSubscription : SubscriptionWorkCommand()
}

internal typealias SubscriptionWorkResult = WorkResult<SubscriptionAction, SubscriptionEffect>