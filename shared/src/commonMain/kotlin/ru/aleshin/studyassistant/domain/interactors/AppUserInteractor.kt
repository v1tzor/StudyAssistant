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

package ru.aleshin.studyassistant.domain.interactors

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import ru.aleshin.studyassistant.core.common.exceptions.InternetConnectionException
import ru.aleshin.studyassistant.core.common.functional.DeviceInfoProvider
import ru.aleshin.studyassistant.core.common.functional.DomainResult
import ru.aleshin.studyassistant.core.common.functional.FlowDomainResult
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.common.messages.UniversalPushToken
import ru.aleshin.studyassistant.core.common.platform.services.CrashlyticsService
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapPurchaseStatus.CONFIRMED
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapPurchaseStatus.PAID
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapService
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapServiceAvailability
import ru.aleshin.studyassistant.core.common.wrappers.EitherWrapper.Abstract.Companion.ERROR_TAG
import ru.aleshin.studyassistant.core.domain.entities.billing.fetchIdentifier
import ru.aleshin.studyassistant.core.domain.entities.users.AppUser
import ru.aleshin.studyassistant.core.domain.entities.users.AuthUser
import ru.aleshin.studyassistant.core.domain.entities.users.SubscribeInfo
import ru.aleshin.studyassistant.core.domain.repositories.MessageRepository
import ru.aleshin.studyassistant.core.domain.repositories.SubscriptionsRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.domain.common.MainEitherWrapper
import ru.aleshin.studyassistant.domain.entities.MainFailures

/**
 * @author Stanislav Aleshin on 25.04.2024.
 */
interface AppUserInteractor {

    suspend fun fetchAuthUser(): DomainResult<MainFailures, AuthUser?>
    suspend fun fetchAppUserInfo(): FlowDomainResult<MainFailures, AppUser?>
    suspend fun fetchAuthStateChanged(): FlowDomainResult<MainFailures, AuthUser?>
    suspend fun fetchAppToken(): FlowDomainResult<MainFailures, UniversalPushToken>
    suspend fun updateUserSubscriptionInfo(): UnitDomainResult<MainFailures>
    suspend fun updateUser(user: AppUser): UnitDomainResult<MainFailures>

    class Base(
        private val iapService: IapService,
        private val subscriptionsRepository: SubscriptionsRepository,
        private val usersRepository: UsersRepository,
        private val messagingRepository: MessageRepository,
        private val deviceInfoProvider: DeviceInfoProvider,
        private val crashlyticsService: CrashlyticsService,
        private val dateManager: DateManager,
        private val eitherWrapper: MainEitherWrapper,
    ) : AppUserInteractor {

        override suspend fun fetchAuthUser() = eitherWrapper.wrap {
            val currentUser = usersRepository.fetchCurrentAuthUser()
            return@wrap currentUser
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        override suspend fun fetchAppUserInfo() = eitherWrapper.wrapFlow {
            usersRepository.fetchStateChanged().flatMapLatest { authUser ->
                usersRepository.fetchCurrentUserProfile()
            }.catch { exception ->
                if (exception !is InternetConnectionException) throw exception
            }
        }

        override suspend fun fetchAuthStateChanged() = eitherWrapper.wrapFlow {
            usersRepository.fetchStateChanged().distinctUntilChangedBy { it?.uid }
        }

        override suspend fun fetchAppToken() = eitherWrapper.wrapFlow {
            messagingRepository.fetchToken()
        }

        override suspend fun updateUserSubscriptionInfo() = eitherWrapper.wrap {
            val currentTime = dateManager.fetchCurrentInstant().toEpochMilliseconds()
            val currentUser = usersRepository.fetchCurrentAuthUser()

            if (currentUser != null) {
                val appUserInfo = usersRepository.fetchCurrentUserProfile().first() ?: return@wrap
                val subscriptionInfo = appUserInfo.subscriptionInfo

                val isAvailability = iapService.fetchServiceAvailability()
                val isAuth = iapService.isAuthorizedUser()

                val allPurchases = if (isAuth && isAvailability is IapServiceAvailability.Available) {
                    try {
                        iapService.fetchPurchases().filter { it.developerPayload == currentUser.uid }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        return@wrap
                    }
                } else {
                    emptyList()
                }

                if (subscriptionInfo != null) {
                    val identifier = subscriptionInfo.fetchIdentifier() ?: return@wrap
                    val actualStatus = subscriptionsRepository.fetchSubscriptionStatus(identifier) ?: return@wrap
                    if (actualStatus.expiryTimeMillis > subscriptionInfo.expiryTimeMillis) {
                        val activePurchase = allPurchases.find { product ->
                            product.subscriptionToken == subscriptionInfo.subscriptionToken
                        }
                        val updatedSubscriptionInfo = subscriptionInfo.copy(
                            expiryTimeMillis = actualStatus.expiryTimeMillis,
                            orderId = activePurchase?.orderId ?: subscriptionInfo.orderId
                        )
                        val updatedAppUser = appUserInfo.copy(
                            subscriptionInfo = updatedSubscriptionInfo,
                            updatedAt = currentTime,
                        )
                        usersRepository.updateCurrentUserProfile(updatedAppUser)
                    }
                } else {
                    val activePurchase = allPurchases.find { product ->
                        product.status == CONFIRMED || product.status == PAID
                    }
                    if (activePurchase != null) {
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

                        if (activePurchase.purchaseId == null) {
                            val message = "PurchaseId time must be not null for purchase: $activePurchase"
                            val error = NullPointerException(message)
                            crashlyticsService.recordException(ERROR_TAG, message, error)
                            return@wrap
                        }
                        if (expiryTime == null) {
                            val message = "Expiry time must be not null for purchase: $activePurchase"
                            val error = NullPointerException(message)
                            crashlyticsService.recordException(ERROR_TAG, message, error)
                            return@wrap
                        }

                        val updatedSubscriptionInfo = SubscribeInfo(
                            deviceId = deviceInfoProvider.fetchDeviceId(),
                            purchaseId = checkNotNull(activePurchase.purchaseId),
                            productId = activePurchase.productId,
                            subscriptionToken = activePurchase.subscriptionToken,
                            orderId = activePurchase.orderId,
                            startTimeMillis = startTime,
                            expiryTimeMillis = expiryTime,
                            store = iapService.fetchStore(),
                        )
                        val updatedAppUser = appUserInfo.copy(
                            subscriptionInfo = updatedSubscriptionInfo,
                            updatedAt = currentTime,
                        )
                        usersRepository.updateCurrentUserProfile(updatedAppUser)
                    }
                }
            }
        }

        override suspend fun updateUser(user: AppUser) = eitherWrapper.wrapUnit {
            val updatedAt = dateManager.fetchCurrentInstant().toEpochMilliseconds()
            val updatedUser = user.copy(updatedAt = updatedAt)
            usersRepository.updateCurrentUserProfile(updatedUser)
        }
    }
}