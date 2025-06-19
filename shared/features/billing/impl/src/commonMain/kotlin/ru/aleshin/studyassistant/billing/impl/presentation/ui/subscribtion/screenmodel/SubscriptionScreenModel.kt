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

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import org.kodein.di.instance
import ru.aleshin.studyassistant.billing.impl.di.holder.BillingFeatureDIHolder
import ru.aleshin.studyassistant.billing.impl.presentation.ui.subscribtion.contract.SubscriptionAction
import ru.aleshin.studyassistant.billing.impl.presentation.ui.subscribtion.contract.SubscriptionEffect
import ru.aleshin.studyassistant.billing.impl.presentation.ui.subscribtion.contract.SubscriptionEvent
import ru.aleshin.studyassistant.billing.impl.presentation.ui.subscribtion.contract.SubscriptionViewState
import ru.aleshin.studyassistant.billing.impl.presentation.ui.subscribtion.screenmodel.SubscriptionWorkCommand.PurchaseProduct
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.BaseScreenModel
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.EmptyDeps
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager

/**
 * @author Stanislav Aleshin on 17.06.2025
 */
internal class SubscriptionScreenModel(
    private val workProcessor: SubscriptionWorkProcessor,
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
            dispatchEvent(SubscriptionEvent.LoadProducts)
        }
    }

    override suspend fun WorkScope<SubscriptionViewState, SubscriptionAction, SubscriptionEffect>.handleEvent(
        event: SubscriptionEvent,
    ) {
        when (event) {
            is SubscriptionEvent.LoadProducts -> {
                launchBackgroundWork(BackgroundKey.LOAD_PRODUCTS) {
                    val command = SubscriptionWorkCommand.LoadProducts
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_PAID_USER_STATUS) {
                    val command = SubscriptionWorkCommand.LoadUserPaidStatus
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is SubscriptionEvent.PurchaseProduct -> {
                launchBackgroundWork(BackgroundKey.PRODUCT_ACTION) {
                    val command = PurchaseProduct(event.productId)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is SubscriptionEvent.ChooseProduct -> {
                sendAction(SubscriptionAction.UpdateSelectedProduct(event.product))
            }
            is SubscriptionEvent.NavigateToBack -> {
                sendEffect(SubscriptionEffect.NavigateToBack)
            }
        }
    }

    override suspend fun reduce(
        action: SubscriptionAction,
        currentState: SubscriptionViewState,
    ) = when (action) {
        is SubscriptionAction.UpdateProducts -> currentState.copy(
            products = action.products,
            selectedProduct = action.products.getOrNull(0),
            isLoadingProducts = false,
        )
        is SubscriptionAction.UpdateSelectedProduct -> currentState.copy(
            selectedProduct = action.product,
        )
        is SubscriptionAction.UpdateUserPaidStatus -> currentState.copy(
            isPaidUser = action.isPaidUser,
        )
        is SubscriptionAction.UpdateLoadingProduct -> currentState.copy(
            isLoadingProducts = action.isLoading,
        )
    }

    enum class BackgroundKey : BackgroundWorkKey {
        LOAD_PRODUCTS, LOAD_PAID_USER_STATUS, PRODUCT_ACTION,
    }
}

@Composable
internal fun Screen.rememberSubscriptionScreenModel(): SubscriptionScreenModel {
    val di = BillingFeatureDIHolder.fetchDI()
    return rememberScreenModel { di.instance<SubscriptionScreenModel>() }
}