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
import ru.aleshin.studyassistant.core.common.functional.DeviceInfoProvider
import ru.aleshin.studyassistant.core.common.functional.DomainResult
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapPurchaseStatus.CONFIRMED
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapService
import ru.aleshin.studyassistant.core.common.platform.services.iap.Store
import ru.aleshin.studyassistant.core.domain.entities.billing.fetchIdentifier
import ru.aleshin.studyassistant.core.domain.entities.users.SubscribeInfo
import ru.aleshin.studyassistant.core.domain.repositories.SubscriptionsRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.settings.impl.domain.common.SettingsEitherWrapper
import ru.aleshin.studyassistant.settings.impl.domain.entities.PurchasedSubscription
import ru.aleshin.studyassistant.settings.impl.domain.entities.SettingsFailures
import ru.aleshin.studyassistant.settings.impl.domain.entities.convertToDetails

/**
 * @author Stanislav Aleshin on 19.06.2025.
 */
internal interface SubscriptionInteractor {

    suspend fun fetchCurrentStore(): Store
    suspend fun fetchSubscriptions(): DomainResult<SettingsFailures, List<PurchasedSubscription>>
    suspend fun restoreSubscription(): DomainResult<SettingsFailures, Boolean>

    class Base(
        private val usersRepository: UsersRepository,
        private val subscriptionsRepository: SubscriptionsRepository,
        private val iapService: IapService,
        private val deviceInfoProvider: DeviceInfoProvider,
        private val dateManager: DateManager,
        private val eitherWrapper: SettingsEitherWrapper,
    ) : SubscriptionInteractor {

        override suspend fun fetchCurrentStore(): Store {
            return iapService.fetchStore()
        }

        override suspend fun fetchSubscriptions() = eitherWrapper.wrap {
            val appUserInfo = usersRepository.fetchCurrentUserProfile().first()
            val userSubscriptionInfo = appUserInfo?.subscriptionInfo

            val allProducts = subscriptionsRepository.fetchSubscriptionsIds().first()
            val allProductsInfo = iapService.fetchProducts(allProducts)

            val currentTime = dateManager.fetchCurrentInstant().toEpochMilliseconds()
            val currentSubscription = if (userSubscriptionInfo != null) {
                val linkedProduct = allProductsInfo.find { it.productId == userSubscriptionInfo.productId }
                userSubscriptionInfo.convertToDetails(
                    isActive = currentTime <= userSubscriptionInfo.expiryTimeMillis,
                    product = linkedProduct,
                )
            } else {
                null
            }

            return@wrap currentSubscription?.let { listOf(it) } ?: emptyList()
        }

        override suspend fun restoreSubscription() = eitherWrapper.wrap {
            val currentUser = usersRepository.fetchCurrentUserOrError().uid
            val currentTime = dateManager.fetchCurrentInstant().toEpochMilliseconds()

            val allPurchases = iapService.fetchPurchases()
            val activePurchase = allPurchases.find { product ->
                product.status == CONFIRMED && product.developerPayload == currentUser
            }

            val appUserInfo = checkNotNull(usersRepository.fetchCurrentUserProfile().first())

            return@wrap if (activePurchase != null) {
                val identifier = activePurchase.fetchIdentifier(iapService.fetchStore())
                val actualStatus = identifier?.let { subscriptionsRepository.fetchSubscriptionStatus(it) }

                val startTime = activePurchase.purchaseTime ?: currentTime
                val expiryTime = if (actualStatus != null) {
                    actualStatus.expiryTimeMillis
                } else {
                    val productInfo = try {
                        iapService.fetchProducts(ids = listOf(activePurchase.productId)).firstOrNull()
                    } catch (_: Exception) {
                        null
                    }
                    val subscriptionPeriod = productInfo?.subscription?.subscriptionPeriod
                    if (subscriptionPeriod != null) {
                        startTime + subscriptionPeriod.inMillis()
                    } else {
                        null
                    }
                }

                val updatedSubscriptionInfo = SubscribeInfo(
                    deviceId = deviceInfoProvider.fetchDeviceId(),
                    purchaseId = checkNotNull(activePurchase.purchaseId),
                    productId = activePurchase.productId,
                    subscriptionToken = activePurchase.subscriptionToken,
                    orderId = activePurchase.orderId,
                    startTimeMillis = startTime,
                    expiryTimeMillis = checkNotNull(expiryTime) {
                        "Expiry time must be not null for purchase: $activePurchase"
                    },
                    store = iapService.fetchStore(),
                )
                val updatedAppUser = appUserInfo.copy(
                    subscriptionInfo = updatedSubscriptionInfo,
                    updatedAt = currentTime,
                )
                usersRepository.updateCurrentUserProfile(updatedAppUser)
                true
            } else {
                false
            }
        }
    }
}