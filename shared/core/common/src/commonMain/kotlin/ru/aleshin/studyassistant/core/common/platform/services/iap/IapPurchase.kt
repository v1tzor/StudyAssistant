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

package ru.aleshin.studyassistant.core.common.platform.services.iap

/**
 * @author Stanislav Aleshin on 16.06.2025.
 */
data class IapPurchase(
    val purchaseId: String?,
    val productId: String,
    val productType: IapProductType,
    val invoiceId: String?,
    val purchaseType: IapPurchaseType = IapPurchaseType.UNDEFINED,
    val description: String? = null,
    val purchaseTime: Long?,
    val orderId: String?,
    val price: Int?,
    val amountLabel: String?,
    val currency: String?,
    val quantity: Int?,
    val status: IapPurchaseStatus,
    val developerPayload: String?,
    val subscriptionToken: String?,
    val sandbox: Boolean,
)

enum class IapPurchaseType {
    ONE_STEP, TWO_STEP, UNDEFINED
}

enum class IapPurchaseStatus {
    CREATED,
    INVOICE_CREATED,
    PROCESSING,
    PAID,
    CONFIRMED,
    CONSUMED,
    CANCELLED,
    REJECTED,
    EXPIRED,
    CLOSED,
    PAUSED,
    TERMINATED,
    REFUNDED,
}