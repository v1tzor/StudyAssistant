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

package ru.aleshin.studyassistant.billing.impl.presentation.models.products

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

/**
 * @author Stanislav Aleshin on 17.06.2025.
 */
@Immutable
@Serializable
internal data class SubscriptionProductUi(
    val productId: String,
    val amountLabel: String?,
    val price: Int?,
    val currency: String?,
    val imageUrl: String?,
    val title: String?,
    val description: String?,
    val subscription: ProductSubscriptionUi?,
)

@Immutable
@Serializable
internal data class ProductSubscriptionUi(
    val subscriptionPeriod: SubscriptionPeriodUi?,
    val freeTrialPeriod: SubscriptionPeriodUi?,
    val gracePeriod: SubscriptionPeriodUi?,
    val introductoryPrice: String?,
    val introductoryPriceAmount: String?,
    val introductoryPricePeriod: SubscriptionPeriodUi?,
)

@Immutable
@Serializable
internal data class SubscriptionPeriodUi(
    val years: Int,
    val months: Int,
    val days: Int,
)