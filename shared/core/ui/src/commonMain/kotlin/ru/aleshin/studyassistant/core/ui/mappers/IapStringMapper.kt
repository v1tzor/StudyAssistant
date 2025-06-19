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

package ru.aleshin.studyassistant.core.ui.mappers

import ru.aleshin.studyassistant.core.common.platform.services.iap.IapFailure
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapPurchaseStatus
import ru.aleshin.studyassistant.core.common.platform.services.iap.Store
import ru.aleshin.studyassistant.core.ui.theme.tokens.StudyAssistantStrings

/**
 * @author Stanislav Aleshin on 18.06.2025.
 */
fun IapFailure.mapToString(strings: StudyAssistantStrings) = when (this) {
    IapFailure.RuStoreNotInstalled -> strings.ruStoreNotInstalled
    IapFailure.RuStoreOutdated -> strings.ruStoreOutdated
    IapFailure.RuStoreUserUnauthorized -> strings.ruStoreUserUnauthorized
    IapFailure.RuStoreApplicationBanned -> strings.ruStoreApplicationBanned
    IapFailure.RuStoreUserBanned -> strings.ruStoreUserBanned
    IapFailure.RuStoreBaseError -> strings.ruStoreBaseError
    IapFailure.RuStoreInvalidParams -> strings.ruStoreInvalidParams
    IapFailure.RuStoreAppNotFound -> strings.ruStoreAppNotFound
    IapFailure.RuStoreAppInactive -> strings.ruStoreAppInactive
    IapFailure.RuStoreProductNotFound -> strings.ruStoreProductNotFound
    IapFailure.RuStoreProductInactive -> strings.ruStoreProductInactive
    IapFailure.RuStoreInvalidProductType -> strings.ruStoreInvalidProductType
    IapFailure.RuStoreDuplicateOrder -> strings.ruStoreDuplicateOrder
    IapFailure.RuStoreInvoiceCreated -> strings.ruStoreInvoiceCreated
    IapFailure.RuStoreConsumableAlreadyPaid -> strings.ruStoreConsumableAlreadyPaid
    IapFailure.RuStoreNonConsumableAlreadyOwned -> strings.ruStoreNonConsumableAlreadyOwned
    IapFailure.RuStoreSubscriptionAlreadyOwned -> strings.ruStoreSubscriptionAlreadyOwned
    IapFailure.RuStoreSubscriptionQueryFailed -> strings.ruStoreSubscriptionQueryFailed
    IapFailure.RuStoreMissingRequiredAttributes -> strings.ruStoreMissingRequiredAttributes
    IapFailure.RuStorePurchaseStatusInvalid -> strings.ruStorePurchaseStatusInvalid
    IapFailure.RuStoreQuantityMoreThanOne -> strings.ruStoreQuantityMoreThanOne
    IapFailure.RuStoreProductDeleted -> strings.ruStoreProductDeleted
    IapFailure.RuStoreInvalidProductState -> strings.ruStoreInvalidProductState
    IapFailure.RuStoreTokenInvalid -> strings.ruStoreTokenInvalid
    IapFailure.RuStoreTokenExpired -> strings.ruStoreTokenExpired
    IapFailure.RuStoreAccessDenied -> strings.ruStoreAccessDenied
    IapFailure.RuStoreMethodForbidden -> strings.ruStoreMethodForbidden
    IapFailure.RuStoreAppIdMismatch -> strings.ruStoreAppIdMismatch
    IapFailure.RuStoreInvalidTokenType -> strings.ruStoreInvalidTokenType
    IapFailure.RuStoreNotFound -> strings.ruStoreNotFound
    IapFailure.RuStoreTimeout -> strings.ruStoreTimeout
    IapFailure.RuStoreInternalError -> strings.ruStoreInternalError
    IapFailure.AppGalleryDefaultError -> strings.appGalleryDefaultError
    IapFailure.UserCancelled -> strings.userCancelled
    IapFailure.AppGalleryParamError -> strings.appGalleryParamError
    IapFailure.AppGalleryIapNotActivated -> strings.appGalleryNotActivated
    IapFailure.AppGalleryInvalidProduct -> strings.appGalleryProductInvalid
    IapFailure.AppGalleryTooManyRequests -> strings.appGalleryTooFrequent
    IapFailure.AppGalleryNetworkError -> strings.appGalleryNetworkError
    IapFailure.AppGalleryProductTypeMismatch -> strings.appGalleryProductTypeMismatch
    IapFailure.AppGalleryCountryNotSupported -> strings.appGalleryCountryNotSupported
    IapFailure.AppGalleryUserNotSignedIn -> strings.appGalleryUserNotSignedIn
    IapFailure.AppGalleryProductAlreadyOwned -> strings.appGalleryProductAlreadyOwned
    IapFailure.AppGalleryProductNotOwned -> strings.appGalleryProductNotOwned
    IapFailure.AppGalleryProductAlreadyConsumed -> strings.appGalleryProductConsumed
    IapFailure.AppGalleryAccountCountryUnsupported -> strings.appGalleryAccountAreaUnsupported
    IapFailure.AppGalleryHighRiskOperation -> strings.appGalleryHighRisk
    IapFailure.AppGalleryPendingPurchase -> strings.appGalleryPending
    IapFailure.UnknownError -> strings.otherErrorMessage
}

fun IapPurchaseStatus.mapToString(strings: StudyAssistantStrings) = when (this) {
    IapPurchaseStatus.CREATED -> strings.purchaseStatusCreated
    IapPurchaseStatus.INVOICE_CREATED -> strings.purchaseStatusCreated
    IapPurchaseStatus.PROCESSING -> strings.purchaseStatusProcessing
    IapPurchaseStatus.PAID -> strings.purchaseStatusPaid
    IapPurchaseStatus.CONFIRMED -> strings.purchaseStatusConfirmed
    IapPurchaseStatus.CONSUMED -> strings.purchaseStatusConfirmed
    IapPurchaseStatus.CANCELLED -> strings.purchaseStatusCancelled
    IapPurchaseStatus.REJECTED -> strings.purchaseStatusRejected
    IapPurchaseStatus.EXPIRED -> strings.purchaseStatusExpired
    IapPurchaseStatus.CLOSED -> strings.purchaseStatusTerminated
    IapPurchaseStatus.PAUSED -> strings.purchaseStatusPaused
    IapPurchaseStatus.TERMINATED -> strings.purchaseStatusTerminated
    IapPurchaseStatus.REFUNDED -> strings.purchaseStatusRefunded
}

fun Store.mapToString() = when (this) {
    Store.RU_STORE -> "RuStore"
    Store.GOOGLE_PLAY -> "Google Play"
    Store.APP_GALLERY -> "App Gallery"
    Store.APP_STORE -> "App Store"
}