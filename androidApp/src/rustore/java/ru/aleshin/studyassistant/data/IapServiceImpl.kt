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

package ru.aleshin.studyassistant.data

import android.app.Activity
import android.content.Context
import ru.aleshin.studyassistant.core.common.platform.PlatformActivity
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapFailure
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapPaymentResult
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapProduct
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapProductPurchaseParams
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapPurchase
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapService
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapServiceAvailability
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapServiceError
import ru.aleshin.studyassistant.core.common.platform.services.iap.PlatformIntent
import ru.aleshin.studyassistant.core.common.platform.services.iap.Store
import ru.aleshin.studyassistant.data.mappers.convertToCommon
import ru.rustore.sdk.billingclient.RuStoreBillingClient
import ru.rustore.sdk.billingclient.model.purchase.PurchaseAvailabilityResult
import ru.rustore.sdk.billingclient.usecase.ProductsUseCase
import ru.rustore.sdk.billingclient.usecase.PurchasesUseCase
import ru.rustore.sdk.billingclient.usecase.UserInfoUseCase
import ru.rustore.sdk.core.exception.RuStoreApplicationBannedException
import ru.rustore.sdk.core.exception.RuStoreException
import ru.rustore.sdk.core.exception.RuStoreNotInstalledException
import ru.rustore.sdk.core.exception.RuStoreOutdatedException
import ru.rustore.sdk.core.exception.RuStoreUserBannedException
import ru.rustore.sdk.core.exception.RuStoreUserUnauthorizedException
import ru.rustore.sdk.core.tasks.Task
import java.lang.ref.WeakReference

/**
 * @author Stanislav Aleshin on 16.06.2025.
 */
class IapServiceImpl(
    private val applicationContext: Context,
    private val billingClient: RuStoreBillingClient,
    private val purchaseInteractor: PurchasesUseCase = billingClient.purchases,
    private val productInteractor: ProductsUseCase = billingClient.products,
    private val userInteractor: UserInfoUseCase = billingClient.userInfo,
) : IapService {

    private var activity: WeakReference<Activity?> = WeakReference(null)

    private companion object {
        const val PACKAGE_NAME = "ru.aleshin.studyassistant"
        const val CONFIRM_SUB_BASE_URL = "https://public-api.rustore.ru/public/glike/subscription"
        const val JWE_TOKEN_HEADER = "Public-Token"
    }

    override fun init(activity: PlatformActivity) {
        this.activity = WeakReference(activity)
    }

    override fun fetchStore(): Store {
        return Store.RU_STORE
    }

    override suspend fun isAuthorizedUser(): Boolean {
        return userInteractor.getAuthorizationStatus().await().authorized
    }

    override suspend fun fetchServiceAvailability(): IapServiceAvailability {
        val result = purchaseInteractor.checkPurchasesAvailability().await()
        return when (result) {
            is PurchaseAvailabilityResult.Available -> {
                IapServiceAvailability.Available
            }
            is PurchaseAvailabilityResult.Unavailable -> {
                IapServiceAvailability.Unavailable(result.cause)
            }
            is PurchaseAvailabilityResult.Unknown -> {
                IapServiceAvailability.Unavailable(null)
            }
        }
    }

    override fun proceedIntent(intent: PlatformIntent?, requestCode: Int?) {
        if (requestCode == null) billingClient.onNewIntent(intent)
    }

    override suspend fun purchaseProduct(params: IapProductPurchaseParams): IapPaymentResult {
        val result = purchaseInteractor.purchaseProduct(
            productId = params.productId,
            orderId = params.orderId,
            quantity = params.quantity,
            developerPayload = params.developerPayload ?: params.appUserId,
        ).handledAwait()
        return result.convertToCommon()
    }

    override suspend fun fetchProducts(ids: List<String>): List<IapProduct> {
        return productInteractor.getProducts(ids).handledAwait().map { it.convertToCommon() }
    }

    override suspend fun fetchPurchaseInfo(purchaseId: String): IapPurchase? {
        return purchaseInteractor.getPurchaseInfo(purchaseId).handledAwait().convertToCommon()
    }

    override suspend fun fetchPurchases(): List<IapPurchase> {
        return purchaseInteractor.getPurchases().handledAwait().map { it.convertToCommon() }
    }

    override suspend fun confirmPurchase(purchaseId: String, developerPayload: String?) {
        purchaseInteractor.confirmPurchase(purchaseId, developerPayload).handledAwait()
    }

    override suspend fun deletePurchase(purchaseId: String) {
        purchaseInteractor.deletePurchase(purchaseId).handledAwait()
    }

    private fun <T> Task<T>.handledAwait(): T {
        return try {
            await()
        } catch (exception: Exception) {
            exception.printStackTrace()
            val type = when (exception) {
                is RuStoreNotInstalledException -> IapFailure.RuStoreNotInstalled
                is RuStoreOutdatedException -> IapFailure.RuStoreOutdated
                is RuStoreUserUnauthorizedException -> IapFailure.RuStoreUserUnauthorized
                is RuStoreApplicationBannedException -> IapFailure.RuStoreApplicationBanned
                is RuStoreUserBannedException -> IapFailure.RuStoreUserBanned
                is RuStoreException -> IapFailure.RuStoreBaseError
                else -> IapFailure.UnknownError
            }
            throw IapServiceError(type)
        }
    }
}