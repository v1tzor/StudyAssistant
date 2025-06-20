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

package ru.aleshin.studyassistant.settings.impl.domain.interactors

import kotlinx.coroutines.flow.first
import ru.aleshin.studyassistant.core.common.extensions.mapEpochTimeToInstant
import ru.aleshin.studyassistant.core.common.functional.DeviceInfoProvider
import ru.aleshin.studyassistant.core.common.functional.DomainResult
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapPurchaseStatus.CONFIRMED
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapService
import ru.aleshin.studyassistant.core.common.platform.services.iap.Store
import ru.aleshin.studyassistant.core.domain.entities.users.SubscribeInfo
import ru.aleshin.studyassistant.core.domain.repositories.ProductsRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.settings.impl.domain.common.SettingsEitherWrapper
import ru.aleshin.studyassistant.settings.impl.domain.entities.SettingsFailures
import ru.aleshin.studyassistant.settings.impl.domain.entities.Subscription
import ru.aleshin.studyassistant.settings.impl.domain.entities.convertToDetails

/**
 * @author Stanislav Aleshin on 19.06.2025.
 */
internal interface SubscriptionInteractor {

    suspend fun fetchCurrentStore(): Store
    suspend fun fetchSubscriptions(): DomainResult<SettingsFailures, List<Subscription>>
    suspend fun restoreSubscription(): DomainResult<SettingsFailures, Boolean>

    class Base(
        private val usersRepository: UsersRepository,
        private val productsRepository: ProductsRepository,
        private val iapService: IapService,
        private val deviceInfoProvider: DeviceInfoProvider,
        private val eitherWrapper: SettingsEitherWrapper,
    ) : SubscriptionInteractor {

        private val currentUser: UID
            get() = usersRepository.fetchCurrentUserOrError().uid

        override suspend fun fetchCurrentStore(): Store {
            return iapService.fetchStore()
        }

        override suspend fun fetchSubscriptions() = eitherWrapper.wrap {
            val appUserInfo = usersRepository.fetchUserById(currentUser).first()
            val userSubscriptionInfo = appUserInfo?.subscriptionInfo

            val allPurchases = try {
                iapService.fetchPurchases().filter { it.developerPayload == currentUser }
            } catch (e: Throwable) {
                emptyList()
            }
            val allProducts = productsRepository.fetchProducts().first()
            val allProductsInfo = iapService.fetchProducts(allProducts)

            return@wrap buildList {
                val linkedPurchase = allPurchases.find {
                    val productFilter = it.productId == userSubscriptionInfo?.productId
                    val purchaseIdFilter = it.purchaseId == userSubscriptionInfo?.purchaseId
                    val subscriptionTokenFilter = it.subscriptionToken == userSubscriptionInfo?.subscriptionToken
                    return@find productFilter && (purchaseIdFilter || subscriptionTokenFilter)
                }
                if (userSubscriptionInfo != null) {
                    val linkedProduct = allProductsInfo.find { it.productId == userSubscriptionInfo.productId }
                    val userSubscription = userSubscriptionInfo.convertToDetails(
                        purchase = linkedPurchase,
                        product = linkedProduct,
                    )
                    add(userSubscription)
                }
                allPurchases.filter { it != linkedPurchase }.forEach { purchase ->
                    val linkedProduct = allProductsInfo.find { it.productId == purchase.productId }
                    add(purchase.convertToDetails(linkedProduct))
                }
            }
        }

        override suspend fun restoreSubscription() = eitherWrapper.wrap {
            val allPurchases = iapService.fetchPurchases()
            val activePurchase = allPurchases.find { product ->
                product.status == CONFIRMED && product.developerPayload == currentUser
            }

            val appUserInfo = checkNotNull(usersRepository.fetchUserById(currentUser).first())
            val currentSubscriptionInfo = appUserInfo.subscriptionInfo

            return@wrap if (activePurchase != null) {
                val productInfo = iapService.fetchProducts(ids = listOf(activePurchase.productId)).firstOrNull()
                val subscriptionPeriod = productInfo?.subscription?.subscriptionPeriod
                val startTime = activePurchase.purchaseTime

                if (subscriptionPeriod != null && startTime != null) {
                    val endTime = (startTime + subscriptionPeriod.inMillis()).mapEpochTimeToInstant()

                    if (currentSubscriptionInfo != null) {
                        val updatedSubscriptionInfo = currentSubscriptionInfo.copy(
                            purchaseId = activePurchase.purchaseId
                                ?: currentSubscriptionInfo.purchaseId,
                            productId = activePurchase.productId,
                            subscriptionToken = activePurchase.subscriptionToken
                                ?: currentSubscriptionInfo.subscriptionToken,
                            orderId = activePurchase.orderId
                                ?: currentSubscriptionInfo.orderId,
                            startTimeMillis = startTime,
                            expiryTimeMillis = endTime.toEpochMilliseconds(),
                        )
                        val updatedAppUser = appUserInfo.copy(subscriptionInfo = updatedSubscriptionInfo)
                        usersRepository.addOrUpdateAppUser(updatedAppUser)
                    } else {
                        val updatedSubscriptionInfo = SubscribeInfo(
                            deviceId = deviceInfoProvider.fetchDeviceId(),
                            purchaseId = checkNotNull(activePurchase.purchaseId),
                            productId = activePurchase.productId,
                            subscriptionToken = activePurchase.subscriptionToken,
                            orderId = activePurchase.orderId,
                            startTimeMillis = startTime,
                            expiryTimeMillis = endTime.toEpochMilliseconds(),
                            store = iapService.fetchStore(),
                        )
                        val updatedAppUser = appUserInfo.copy(subscriptionInfo = updatedSubscriptionInfo)
                        usersRepository.addOrUpdateAppUser(updatedAppUser)
                    }
                    true
                } else {
                    false
                }
            } else {
                false
            }
        }
    }
}