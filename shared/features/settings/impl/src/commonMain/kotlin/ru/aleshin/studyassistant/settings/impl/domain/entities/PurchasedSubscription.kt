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

package ru.aleshin.studyassistant.settings.impl.domain.entities

import ru.aleshin.studyassistant.core.common.platform.services.iap.IapProduct
import ru.aleshin.studyassistant.core.domain.entities.users.SubscribeInfo

/**
 * @author Stanislav Aleshin on 19.06.2025.
 */
internal data class PurchasedSubscription(
    val purchaseId: String?,
    val productId: String,
    val subscriptionToken: String?,
    val expiryTime: Long,
    val purchaseTime: Long?,
    val isActive: Boolean,
    val currency: String?,
    val amountLabel: String?,
    val title: String?,
    val description: String?,
    val subscriptionPeriod: Long?,
)

internal fun SubscribeInfo.convertToDetails(
    isActive: Boolean,
    product: IapProduct?,
) = PurchasedSubscription(
    purchaseId = purchaseId,
    productId = productId,
    subscriptionToken = subscriptionToken,
    purchaseTime = startTimeMillis,
    expiryTime = expiryTimeMillis,
    isActive = isActive,
    amountLabel = product?.amountLabel,
    currency = product?.currency,
    title = product?.title,
    description = product?.description,
    subscriptionPeriod = product?.subscription?.subscriptionPeriod?.inMillis(),
)