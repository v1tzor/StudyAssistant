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

@file:OptIn(ExperimentalObjCName::class)

package ru.aleshin.studyassistant.core.common.platform.services.iap

import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

/**
 * @author Stanislav Aleshin on 16.06.2025.
 */
@ObjCName("IapPaymentResult", exact = true)
sealed interface IapPaymentResult

@ObjCName("IapPaymentResultSuccess", exact = true)
data class IapPaymentResultSuccess(
    val orderId: String?,
    val purchaseId: String,
    val productId: String,
    val invoiceId: String?,
    val sandbox: Boolean,
    val subscriptionToken: String? = null,
) : IapPaymentResult

@ObjCName("IapPaymentResultCancelled", exact = true)
data class IapPaymentResultCancelled(
    val purchaseId: String?,
    val sandbox: Boolean,
) : IapPaymentResult

@ObjCName("IapPaymentResultFailure", exact = true)
data class IapPaymentResultFailure(
    val purchaseId: String? = null,
    val invoiceId: String? = null,
    val orderId: String? = null,
    val quantity: Int? = null,
    val productId: String? = null,
    val sandbox: Boolean = false,
    val errorCode: Int? = null,
    val failure: IapFailure = IapFailure.UnknownError,
) : IapPaymentResult

@ObjCName("IapPaymentResultInvalidPaymentState", exact = true)
object IapPaymentResultInvalidPaymentState : IapPaymentResult

enum class IapFailure {
    // ==== RuStore Errors ====
    RuStoreNotInstalled,
    RuStoreOutdated,
    RuStoreUserUnauthorized,
    RuStoreApplicationBanned,
    RuStoreUserBanned,
    RuStoreBaseError,

    // RuStore Billing Error Codes
    RuStoreInvalidParams, // 40001
    RuStoreAppNotFound, // 40003
    RuStoreAppInactive, // 40004
    RuStoreProductNotFound, // 40005
    RuStoreProductInactive, // 40006
    RuStoreInvalidProductType, // 40007
    RuStoreDuplicateOrder, // 40008
    RuStoreInvoiceCreated, // 40009
    RuStoreConsumableAlreadyPaid, // 40010
    RuStoreNonConsumableAlreadyOwned, // 40011
    RuStoreSubscriptionAlreadyOwned, // 40012
    RuStoreSubscriptionQueryFailed, // 40013
    RuStoreMissingRequiredAttributes, // 40014
    RuStorePurchaseStatusInvalid, // 40015
    RuStoreQuantityMoreThanOne, // 40016
    RuStoreProductDeleted, // 40017
    RuStoreInvalidProductState, // 40018

    RuStoreTokenInvalid, // 40101
    RuStoreTokenExpired, // 40102

    RuStoreAccessDenied, // 40301
    RuStoreMethodForbidden, // 40302
    RuStoreAppIdMismatch, // 40303
    RuStoreInvalidTokenType, // 40305

    RuStoreNotFound, // 40401
    RuStoreTimeout, // 40801
    RuStoreInternalError, // 500xx

    // ==== AppGallery Errors ====
    AppGalleryDefaultError, // 1
    UserCancelled, // 60000
    AppGalleryParamError, // 60001
    AppGalleryIapNotActivated, // 60002
    AppGalleryInvalidProduct, // 60003
    AppGalleryTooManyRequests, // 60004
    AppGalleryNetworkError, // 60005
    AppGalleryProductTypeMismatch, // 60006
    AppGalleryCountryNotSupported, // 60007

    AppGalleryUserNotSignedIn, // 60050
    AppGalleryProductAlreadyOwned, // 60051
    AppGalleryProductNotOwned, // 60052
    AppGalleryProductAlreadyConsumed, // 60053
    AppGalleryAccountCountryUnsupported, // 60054
    AppGalleryHighRiskOperation, // 60056
    AppGalleryPendingPurchase, // 60057

    // ==== Unknown / Other ====
    UnknownError
}