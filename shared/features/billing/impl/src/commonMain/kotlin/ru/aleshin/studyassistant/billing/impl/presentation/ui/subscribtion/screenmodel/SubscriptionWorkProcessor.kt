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

package ru.aleshin.studyassistant.billing.impl.presentation.ui.subscribtion.screenmodel

import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import ru.aleshin.studyassistant.billing.impl.domain.interactors.AppUserInteractor
import ru.aleshin.studyassistant.billing.impl.domain.interactors.PurchaseInteractor
import ru.aleshin.studyassistant.billing.impl.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.billing.impl.presentation.ui.subscribtion.contract.SubscriptionAction
import ru.aleshin.studyassistant.billing.impl.presentation.ui.subscribtion.contract.SubscriptionEffect
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.ActionResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.EffectResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.FlowWorkProcessor
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkCommand
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkResult
import ru.aleshin.studyassistant.core.common.functional.collectAndHandle
import ru.aleshin.studyassistant.core.common.functional.handle

/**
 * @author Stanislav Aleshin on 17.06.2025.
 */
internal interface SubscriptionWorkProcessor :
    FlowWorkProcessor<SubscriptionWorkCommand, SubscriptionAction, SubscriptionEffect> {

    class Base(
        private val purchaseInteractor: PurchaseInteractor,
        private val appUserInteractor: AppUserInteractor,
    ) : SubscriptionWorkProcessor {

        override suspend fun work(command: SubscriptionWorkCommand) = when (command) {
            is SubscriptionWorkCommand.LoadProducts -> loadProductsWork()
            is SubscriptionWorkCommand.PurchaseProduct -> purchaseProductWork(command.productId)
            is SubscriptionWorkCommand.LoadUserPaidStatus -> loadUserPaidStatusWork()
        }

        private fun loadProductsWork() = flow<SubscriptionWorkResult> {
            purchaseInteractor.fetchProducts().handle(
                onLeftAction = { failure ->
                    emit(EffectResult(SubscriptionEffect.ShowError(failure)))
                },
                onRightAction = { products ->
                    emit(ActionResult(SubscriptionAction.UpdateProducts(products.map { it.mapToUi() })))
                }
            )
        }.onStart {
            emit(ActionResult(SubscriptionAction.UpdateLoadingProduct(true)))
        }

        private fun purchaseProductWork(productId: String) = flow<SubscriptionWorkResult> {
            purchaseInteractor.purchaseSubscription(productId).handle(
                onLeftAction = { failure ->
                    emit(EffectResult(SubscriptionEffect.ShowError(failure)))
                },
                onRightAction = {
                    emit(EffectResult(SubscriptionEffect.ShowSuccessDialog))
                }
            )
        }

        private fun loadUserPaidStatusWork() = flow {
            appUserInteractor.fetchAppUserPaidStatus().collectAndHandle(
                onLeftAction = { emit(EffectResult(SubscriptionEffect.ShowError(it))) },
                onRightAction = { emit(ActionResult(SubscriptionAction.UpdateUserPaidStatus(it))) },
            )
        }
    }
}

internal sealed class SubscriptionWorkCommand : WorkCommand {
    data object LoadProducts : SubscriptionWorkCommand()
    data object LoadUserPaidStatus : SubscriptionWorkCommand()
    data class PurchaseProduct(val productId: String) : SubscriptionWorkCommand()
}

internal typealias SubscriptionWorkResult = WorkResult<SubscriptionAction, SubscriptionEffect>