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

package ru.aleshin.studyassistant.billing.impl.domain.interactors

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import ru.aleshin.studyassistant.billing.impl.domain.common.BillingEitherWrapper
import ru.aleshin.studyassistant.billing.impl.domain.entities.BillingFailures
import ru.aleshin.studyassistant.core.common.functional.DeviceInfoProvider
import ru.aleshin.studyassistant.core.common.functional.DomainResult
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapFailure
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapPaymentResultCancelled
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapPaymentResultFailure
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapPaymentResultInvalidPaymentState
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapPaymentResultSuccess
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapProduct
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapProductPurchaseParams
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapProductType
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapService
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapServiceError
import ru.aleshin.studyassistant.core.domain.entities.users.SubscribeInfo
import ru.aleshin.studyassistant.core.domain.repositories.ProductsRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository

/**
 * @author Stanislav Aleshin on 17.06.2025.
 */
internal interface PurchaseInteractor {

    suspend fun fetchProducts(): DomainResult<BillingFailures, List<IapProduct>>

    suspend fun purchaseSubscription(productId: String): UnitDomainResult<BillingFailures>

    class Base(
        private val iapService: IapService,
        private val productsRepository: ProductsRepository,
        private val usersRepository: UsersRepository,
        private val dataManager: DateManager,
        private val deviceInfoProvider: DeviceInfoProvider,
        private val eitherWrapper: BillingEitherWrapper,
    ) : PurchaseInteractor {

        override suspend fun fetchProducts() = eitherWrapper.wrap {
            val products = productsRepository.fetchProducts().first()
            iapService.fetchProducts(products)
        }

        override suspend fun purchaseSubscription(productId: String) = eitherWrapper.wrapUnit {
            val currentUser = usersRepository.fetchCurrentUserOrError()
            val appUserProfile = checkNotNull(usersRepository.fetchUserById(currentUser.uid).firstOrNull())
            val productInfo = checkNotNull(iapService.fetchProducts(listOf(productId)).firstOrNull())
            val params = IapProductPurchaseParams(
                productId = productId,
                productType = IapProductType.SUBSCRIPTION,
                developerPayload = currentUser.uid,
                appUserId = currentUser.uid,
                appUserEmail = appUserProfile.email,
            )
            val purchaseResult = iapService.purchaseProduct(params)
            when (purchaseResult) {
                is IapPaymentResultSuccess -> with(purchaseResult) {
                    val currentTime = dataManager.fetchCurrentInstant().toEpochMilliseconds()
                    val periodTime = productInfo.subscription?.subscriptionPeriod?.inMillis() ?: 0L
                    val subscriptionInfo = SubscribeInfo(
                        deviceId = deviceInfoProvider.fetchDeviceId(),
                        purchaseId = purchaseId,
                        productId = productId,
                        subscriptionToken = subscriptionToken,
                        orderId = orderId,
                        startTimeMillis = currentTime,
                        expiryTimeMillis = currentTime + periodTime,
                        store = iapService.fetchStore(),
                    )
                    val updateAppUser = appUserProfile.copy(subscriptionInfo = subscriptionInfo)
                    usersRepository.updateAppUser(updateAppUser)
                }

                is IapPaymentResultCancelled -> {
                    purchaseResult.purchaseId?.let { iapService.deletePurchase(it) }
                    throw IapServiceError(IapFailure.UserCancelled)
                }

                is IapPaymentResultFailure -> {
                    purchaseResult.purchaseId?.let { iapService.deletePurchase(it) }
                    throw IapServiceError(purchaseResult.failure)
                }

                is IapPaymentResultInvalidPaymentState -> {
                    throw IapServiceError(IapFailure.UnknownError)
                }
            }
        }
    }
}