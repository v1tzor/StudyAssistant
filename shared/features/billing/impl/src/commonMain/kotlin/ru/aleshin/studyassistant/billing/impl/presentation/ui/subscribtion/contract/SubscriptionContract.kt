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

import androidx.compose.runtime.Immutable
import dev.icerock.moko.parcelize.Parcelize
import ru.aleshin.studyassistant.billing.impl.domain.entities.BillingFailures
import ru.aleshin.studyassistant.billing.impl.presentation.models.products.SubscriptionProductUi
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseAction
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseEvent
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseUiEffect
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseViewState

/**
 * @author Stanislav Aleshin on 17.06.2025
 */
@Immutable
@Parcelize
internal data class SubscriptionViewState(
    val isLoadingProducts: Boolean = true,
    val isLoadingPurchase: Boolean = false,
    val isPaidUser: Boolean = false,
    val selectedProduct: SubscriptionProductUi? = null,
    val products: List<SubscriptionProductUi> = emptyList(),
) : BaseViewState

internal sealed class SubscriptionEvent : BaseEvent {
    data object LoadProducts : SubscriptionEvent()
    data class PurchaseProduct(val productId: String) : SubscriptionEvent()
    data class ChooseProduct(val product: SubscriptionProductUi) : SubscriptionEvent()
    data object NavigateToBack : SubscriptionEvent()
}

internal sealed class SubscriptionEffect : BaseUiEffect {
    data object ShowSuccessDialog : SubscriptionEffect()
    data class ShowError(val failures: BillingFailures) : SubscriptionEffect()
    data object NavigateToBack : SubscriptionEffect()
}

internal sealed class SubscriptionAction : BaseAction {
    data class UpdateProducts(val products: List<SubscriptionProductUi>) : SubscriptionAction()
    data class UpdateSelectedProduct(val product: SubscriptionProductUi?) : SubscriptionAction()
    data class UpdateUserPaidStatus(val isPaidUser: Boolean) : SubscriptionAction()
    data class UpdateLoadingProduct(val isLoading: Boolean) : SubscriptionAction()
}