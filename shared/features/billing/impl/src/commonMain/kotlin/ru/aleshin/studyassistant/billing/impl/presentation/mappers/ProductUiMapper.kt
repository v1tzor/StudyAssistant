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

package ru.aleshin.studyassistant.billing.impl.presentation.mappers

import ru.aleshin.studyassistant.billing.impl.presentation.models.products.ProductSubscriptionUi
import ru.aleshin.studyassistant.billing.impl.presentation.models.products.SubscriptionPeriodUi
import ru.aleshin.studyassistant.billing.impl.presentation.models.products.SubscriptionProductUi
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapProduct
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapProductSubscription
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapSubscriptionPeriod

/**
 * @author Stanislav Aleshin on 18.06.2025.
 */
internal fun IapSubscriptionPeriod.mapToUi() = SubscriptionPeriodUi(
    years = years,
    months = months,
    days = days
)

internal fun IapProductSubscription.mapToUi() = ProductSubscriptionUi(
    subscriptionPeriod = subscriptionPeriod?.mapToUi(),
    freeTrialPeriod = freeTrialPeriod?.mapToUi(),
    gracePeriod = gracePeriod?.mapToUi(),
    introductoryPrice = introductoryPrice,
    introductoryPriceAmount = introductoryPriceAmount,
    introductoryPricePeriod = introductoryPricePeriod?.mapToUi()
)

internal fun IapProduct.mapToUi() = SubscriptionProductUi(
    productId = productId,
    amountLabel = amountLabel,
    price = price,
    currency = currency,
    imageUrl = imageUrl,
    title = title,
    description = description,
    subscription = subscription?.mapToUi()
)