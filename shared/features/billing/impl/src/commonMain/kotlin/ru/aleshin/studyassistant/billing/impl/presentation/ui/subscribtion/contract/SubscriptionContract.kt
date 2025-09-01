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

package ru.aleshin.studyassistant.billing.impl.presentation.ui.subscribtion.contract

import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.billing.impl.domain.entities.BillingFailures
import ru.aleshin.studyassistant.billing.impl.presentation.models.products.SubscriptionProductUi
import ru.aleshin.studyassistant.core.common.architecture.component.BaseOutput
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreAction
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEffect
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEvent
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreState

/**
 * @author Stanislav Aleshin on 17.06.2025
 */
@Serializable
internal data class SubscriptionState(
    val isLoadingProducts: Boolean = true,
    val isLoadingPurchase: Boolean = false,
    val isPaidUser: Boolean = false,
    val selectedProduct: SubscriptionProductUi? = null,
    val products: List<SubscriptionProductUi> = emptyList(),
) : StoreState

internal sealed class SubscriptionEvent : StoreEvent {
    data object LoadProducts : SubscriptionEvent()
    data class PurchaseProduct(val productId: String) : SubscriptionEvent()
    data class ChooseProduct(val product: SubscriptionProductUi) : SubscriptionEvent()
    data object ClickBack : SubscriptionEvent()
}

internal sealed class SubscriptionEffect : StoreEffect {
    data object ShowSuccessDialog : SubscriptionEffect()
    data class ShowError(val failures: BillingFailures) : SubscriptionEffect()
}

internal sealed class SubscriptionAction : StoreAction {
    data class UpdateProducts(val products: List<SubscriptionProductUi>) : SubscriptionAction()
    data class UpdateSelectedProduct(val product: SubscriptionProductUi?) : SubscriptionAction()
    data class UpdateUserPaidStatus(val isPaidUser: Boolean) : SubscriptionAction()
    data class UpdateLoadingProduct(val isLoading: Boolean) : SubscriptionAction()
}

internal sealed class SubscriptionOutput : BaseOutput {
    data object NavigateToBack : SubscriptionOutput()
}