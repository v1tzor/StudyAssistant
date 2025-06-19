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
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapPurchase
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapPurchaseStatus
import ru.aleshin.studyassistant.core.domain.entities.users.SubscribeInfo

/**
 * @author Stanislav Aleshin on 19.06.2025.
 */
internal data class Subscription(
    val purchaseId: String?,
    val productId: String,
    val purchaseTime: Long?,
    val amountLabel: String?,
    val currency: String?,
    val title: String,
    val description: String?,
    val subscriptionPeriod: Long?,
    val status: IapPurchaseStatus?,
    val subscriptionToken: String?,
)

internal fun SubscribeInfo.convertToDetails(
    purchase: IapPurchase?,
    product: IapProduct?,
) = Subscription(
    purchaseId = purchaseId,
    productId = productId,
    purchaseTime = startTimeMillis,
    amountLabel = product?.amountLabel,
    currency = product?.currency,
    title = product?.title ?: "Subscribe",
    description = product?.description,
    subscriptionPeriod = product?.subscription?.subscriptionPeriod?.inMillis(),
    status = purchase?.status,
    subscriptionToken = subscriptionToken,
)

internal fun IapPurchase.convertToDetails(
    product: IapProduct?,
) = Subscription(
    purchaseId = purchaseId,
    productId = productId,
    purchaseTime = purchaseTime,
    amountLabel = amountLabel ?: product?.amountLabel,
    currency = currency ?: product?.currency,
    title = product?.title ?: "Subscribe",
    description = product?.description ?: description,
    subscriptionPeriod = product?.subscription?.subscriptionPeriod?.inMillis(),
    status = status,
    subscriptionToken = subscriptionToken,
)