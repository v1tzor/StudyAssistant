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

package ru.aleshin.studyassistant.data.mappers

import ru.aleshin.studyassistant.core.common.platform.services.iap.IapFailure
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapPaymentResultCancelled
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapPaymentResultFailure
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapPaymentResultInvalidPaymentState
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapPaymentResultSuccess
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapProduct
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapProductSubscription
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapProductType
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapPurchase
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapPurchaseStatus
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapSubscriptionPeriod
import ru.rustore.sdk.billingclient.model.product.Product
import ru.rustore.sdk.billingclient.model.product.ProductType
import ru.rustore.sdk.billingclient.model.product.SubscriptionPeriod
import ru.rustore.sdk.billingclient.model.purchase.PaymentResult
import ru.rustore.sdk.billingclient.model.purchase.Purchase
import ru.rustore.sdk.billingclient.model.purchase.PurchaseState

/**
 * @author Stanislav Aleshin on 16.06.2025.
 */
fun Product.convertToCommon() = IapProduct(
    productId = productId,
    type = when (productType) {
        ProductType.NON_CONSUMABLE -> IapProductType.NON_CONSUMABLE_PRODUCT
        ProductType.CONSUMABLE -> IapProductType.CONSUMABLE_PRODUCT
        ProductType.SUBSCRIPTION -> IapProductType.SUBSCRIPTION
        null -> error("Unknow product type")
    },
    amountLabel = priceLabel,
    price = price,
    currency = currency,
    imageUrl = imageUrl.toString(),
    title = title,
    description = description,
    subscription = subscription?.let {
        IapProductSubscription(
            subscriptionPeriod = it.subscriptionPeriod?.convertToCommon(),
            freeTrialPeriod = it.freeTrialPeriod?.convertToCommon(),
            gracePeriod = it.gracePeriod?.convertToCommon(),
            introductoryPrice = it.introductoryPrice,
            introductoryPriceAmount = it.introductoryPriceAmount,
            introductoryPricePeriod = it.introductoryPricePeriod?.convertToCommon(),
        )
    }
)

fun Purchase.convertToCommon() = IapPurchase(
    purchaseId = purchaseId,
    productId = productId,
    productType = when (productType) {
        ProductType.NON_CONSUMABLE -> IapProductType.NON_CONSUMABLE_PRODUCT
        ProductType.CONSUMABLE -> IapProductType.CONSUMABLE_PRODUCT
        ProductType.SUBSCRIPTION -> IapProductType.SUBSCRIPTION
        null -> error("Unknow product type")
    },
    invoiceId = invoiceId,
    purchaseTime = purchaseTime?.time,
    orderId = orderId,
    price = amount,
    amountLabel = amountLabel,
    currency = currency,
    quantity = quantity,
    status = when (purchaseState) {
        PurchaseState.CREATED -> IapPurchaseStatus.CREATED
        PurchaseState.INVOICE_CREATED -> IapPurchaseStatus.INVOICE_CREATED
        PurchaseState.CONFIRMED -> IapPurchaseStatus.CONFIRMED
        PurchaseState.PAID -> IapPurchaseStatus.PAID
        PurchaseState.CANCELLED -> IapPurchaseStatus.CANCELLED
        PurchaseState.CONSUMED -> IapPurchaseStatus.CONSUMED
        PurchaseState.CLOSED -> IapPurchaseStatus.CLOSED
        PurchaseState.PAUSED -> IapPurchaseStatus.PAUSED
        PurchaseState.TERMINATED -> IapPurchaseStatus.TERMINATED
        null -> error("Unknow purchaseState")
    },
    developerPayload = developerPayload,
    subscriptionToken = subscriptionToken,
    sandbox = sandbox,
)

fun SubscriptionPeriod.convertToCommon() = IapSubscriptionPeriod(
    years = years,
    months = months,
    days = days,
)

fun PaymentResult.convertToCommon() = when (this) {
    is PaymentResult.Success -> IapPaymentResultSuccess(
        orderId = orderId,
        purchaseId = purchaseId,
        productId = productId,
        invoiceId = invoiceId,
        sandbox = sandbox,
        subscriptionToken = subscriptionToken
    )
    is PaymentResult.Failure -> IapPaymentResultFailure(
        purchaseId = purchaseId,
        invoiceId = invoiceId,
        orderId = orderId,
        quantity = quantity,
        productId = productId,
        sandbox = sandbox,
        errorCode = errorCode,
        failure = when (errorCode) {
            40001 -> IapFailure.RuStoreInvalidParams
            40003 -> IapFailure.RuStoreAppNotFound
            40004 -> IapFailure.RuStoreAppInactive
            40005 -> IapFailure.RuStoreProductNotFound
            40006 -> IapFailure.RuStoreProductInactive
            40007 -> IapFailure.RuStoreInvalidProductType
            40008 -> IapFailure.RuStoreDuplicateOrder
            40009 -> IapFailure.RuStoreInvoiceCreated
            40010 -> IapFailure.RuStoreConsumableAlreadyPaid
            40011 -> IapFailure.RuStoreNonConsumableAlreadyOwned
            40012 -> IapFailure.RuStoreSubscriptionAlreadyOwned
            40013 -> IapFailure.RuStoreSubscriptionQueryFailed
            40014 -> IapFailure.RuStoreMissingRequiredAttributes
            40015 -> IapFailure.RuStorePurchaseStatusInvalid
            40016 -> IapFailure.RuStoreQuantityMoreThanOne
            40017 -> IapFailure.RuStoreProductDeleted
            40018 -> IapFailure.RuStoreInvalidProductState
            40101 -> IapFailure.RuStoreTokenInvalid
            40102 -> IapFailure.RuStoreTokenExpired
            40301 -> IapFailure.RuStoreAccessDenied
            40302 -> IapFailure.RuStoreMethodForbidden
            40303 -> IapFailure.RuStoreAppIdMismatch
            40305 -> IapFailure.RuStoreInvalidTokenType
            40401 -> IapFailure.RuStoreNotFound
            40801 -> IapFailure.RuStoreTimeout
            in 50000..59999 -> IapFailure.RuStoreInternalError
            else -> IapFailure.UnknownError
        }
    )
    is PaymentResult.Cancelled -> IapPaymentResultCancelled(
        purchaseId = purchaseId,
        sandbox = sandbox
    )
    is PaymentResult.InvalidPaymentState -> IapPaymentResultInvalidPaymentState
}