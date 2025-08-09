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

package ru.aleshin.studyassistant.settings.impl.presentation.mappers

import ru.aleshin.studyassistant.settings.impl.domain.entities.PurchasedSubscription
import ru.aleshin.studyassistant.settings.impl.presentation.models.billing.SubscriptionUi

/**
 * @author Stanislav Aleshin on 19.06.2025.
 */
internal fun PurchasedSubscription.mapToUi() = SubscriptionUi(
    purchaseId = purchaseId,
    productId = productId,
    purchaseTime = purchaseTime,
    amountLabel = amountLabel,
    currency = currency,
    title = title,
    description = description,
    subscriptionPeriod = subscriptionPeriod,
    expiryTime = expiryTime,
    isActive = isActive,
    subscriptionToken = subscriptionToken,
)