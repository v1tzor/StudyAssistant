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

import com.huawei.hms.iap.IapClient
import com.huawei.hms.iap.entity.OrderStatusCode
import com.huawei.hms.iap.entity.ProductInfo
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapFailure
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapProduct
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapProductSubscription
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapProductType
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapSubscriptionPeriod

/**
 * @author Stanislav Aleshin on 16.06.2025.
 */
fun Int.toIapFailure(): IapFailure = when (this) {
    OrderStatusCode.ORDER_HWID_NOT_LOGIN -> IapFailure.AppGalleryUserNotSignedIn
    OrderStatusCode.ORDER_ACCOUNT_AREA_NOT_SUPPORTED -> IapFailure.AppGalleryCountryNotSupported
    OrderStatusCode.ORDER_PRODUCT_OWNED -> IapFailure.AppGalleryProductAlreadyOwned
    OrderStatusCode.ORDER_PRODUCT_NOT_OWNED -> IapFailure.AppGalleryProductNotOwned
    OrderStatusCode.ORDER_PRODUCT_CONSUMED -> IapFailure.AppGalleryProductAlreadyConsumed
    OrderStatusCode.ORDER_STATE_CANCEL -> IapFailure.UserCancelled
    OrderStatusCode.ORDER_PRODUCT_OWNED -> IapFailure.AppGalleryProductAlreadyOwned
    in 60001..60007 -> when (this) {
        60001 -> IapFailure.AppGalleryParamError
        60002 -> IapFailure.AppGalleryIapNotActivated
        60003 -> IapFailure.AppGalleryInvalidProduct
        60004 -> IapFailure.AppGalleryTooManyRequests
        60005 -> IapFailure.AppGalleryNetworkError
        60006 -> IapFailure.AppGalleryProductTypeMismatch
        60007 -> IapFailure.AppGalleryCountryNotSupported
        else -> IapFailure.AppGalleryDefaultError
    }
    in 60050..60057 -> when (this) {
        60050 -> IapFailure.AppGalleryUserNotSignedIn
        60051 -> IapFailure.AppGalleryProductAlreadyOwned
        60052 -> IapFailure.AppGalleryProductNotOwned
        60053 -> IapFailure.AppGalleryProductAlreadyConsumed
        60054 -> IapFailure.AppGalleryAccountCountryUnsupported
        60056 -> IapFailure.AppGalleryHighRiskOperation
        60057 -> IapFailure.AppGalleryPendingPurchase
        else -> IapFailure.AppGalleryDefaultError
    }
    else -> IapFailure.UnknownError
}

fun Int.toIapProductType() = when (this) {
    IapClient.PriceType.IN_APP_SUBSCRIPTION -> IapProductType.SUBSCRIPTION
    IapClient.PriceType.IN_APP_CONSUMABLE -> IapProductType.CONSUMABLE_PRODUCT
    IapClient.PriceType.IN_APP_NONCONSUMABLE -> IapProductType.NON_CONSUMABLE_PRODUCT
    else -> IapProductType.NON_CONSUMABLE_PRODUCT
}

fun IapProductType.toHuaweiType() = when (this) {
    IapProductType.SUBSCRIPTION -> IapClient.PriceType.IN_APP_SUBSCRIPTION
    IapProductType.CONSUMABLE_PRODUCT -> IapClient.PriceType.IN_APP_CONSUMABLE
    IapProductType.NON_CONSUMABLE_PRODUCT -> IapClient.PriceType.IN_APP_NONCONSUMABLE
}

fun ProductInfo.mapToCommon() = IapProduct(
    productId = productId,
    type = when (priceType) {
        IapClient.PriceType.IN_APP_SUBSCRIPTION -> IapProductType.SUBSCRIPTION
        IapClient.PriceType.IN_APP_NONCONSUMABLE -> IapProductType.NON_CONSUMABLE_PRODUCT
        else -> IapProductType.CONSUMABLE_PRODUCT
    },
    amountLabel = price,
    price = microsPrice.toInt(),
    currency = currency,
    imageUrl = null,
    title = productName,
    description = productDesc,
    subscription = if (priceType == IapClient.PriceType.IN_APP_SUBSCRIPTION) {
        IapProductSubscription(
            subscriptionPeriod = subPeriod?.let { IapSubscriptionPeriod.fromIso8601(it) },
            freeTrialPeriod = subFreeTrialPeriod?.let { IapSubscriptionPeriod.fromIso8601(it) },
            gracePeriod = null,
            introductoryPrice = subSpecialPriceMicros?.toString(),
            introductoryPriceAmount = subSpecialPrice,
            introductoryPricePeriod = subSpecialPeriod?.let { IapSubscriptionPeriod.fromIso8601(it) },
        )
    } else {
        null
    }
)