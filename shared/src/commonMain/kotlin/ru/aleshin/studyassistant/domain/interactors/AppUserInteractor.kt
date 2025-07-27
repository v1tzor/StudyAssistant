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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import ru.aleshin.studyassistant.core.common.exceptions.InternetConnectionException
import ru.aleshin.studyassistant.core.common.extensions.equalsDay
import ru.aleshin.studyassistant.core.common.extensions.mapEpochTimeToInstant
import ru.aleshin.studyassistant.core.common.functional.DeviceInfoProvider
import ru.aleshin.studyassistant.core.common.functional.DomainResult
import ru.aleshin.studyassistant.core.common.functional.FlowDomainResult
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.common.messages.UniversalPushToken
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapPurchaseStatus.CONFIRMED
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapService
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapServiceAvailability
import ru.aleshin.studyassistant.core.domain.entities.users.AppUser
import ru.aleshin.studyassistant.core.domain.entities.users.AuthUser
import ru.aleshin.studyassistant.core.domain.entities.users.SubscribeInfo
import ru.aleshin.studyassistant.core.domain.repositories.MessageRepository
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
        private val usersRepository: UsersRepository,
        private val messagingRepository: MessageRepository,
        private val deviceInfoProvider: DeviceInfoProvider,
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
            usersRepository.fetchStateChanged()
        }

        override suspend fun fetchAppToken() = eitherWrapper.wrapFlow {
            messagingRepository.fetchToken()
        }

        override suspend fun updateUserSubscriptionInfo() = eitherWrapper.wrap {
            val currentUser = usersRepository.fetchCurrentAuthUser()
            val isAvailability = iapService.fetchServiceAvailability()
            val isAuth = iapService.isAuthorizedUser()

            val allPurchases = if (isAuth && isAvailability is IapServiceAvailability.Available) {
                iapService.fetchPurchases()
            } else {
                emptyList()
            }
            if (currentUser != null) {
                val activePurchase = allPurchases.find { product ->
                    product.status == CONFIRMED && product.developerPayload == currentUser.uid
                }

                val appUserInfo = usersRepository.fetchCurrentUserProfile().first() ?: return@wrap
                val currentSubscriptionInfo = appUserInfo.subscriptionInfo

                if (activePurchase != null) {
                    val productInfo = iapService.fetchProducts(ids = listOf(activePurchase.productId)).firstOrNull()
                    val subscriptionPeriod = productInfo?.subscription?.subscriptionPeriod
                    val startTime = activePurchase.purchaseTime

                    if (subscriptionPeriod != null && startTime != null) {
                        val endTime = (startTime + subscriptionPeriod.inMillis()).mapEpochTimeToInstant()

                        if (currentSubscriptionInfo != null) {
                            val currentExpiryTime = currentSubscriptionInfo.expiryTimeMillis.mapEpochTimeToInstant()
                            if (!currentExpiryTime.equalsDay(endTime) && endTime > currentExpiryTime) {
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
                                usersRepository.updateCurrentUserProfile(updatedAppUser)
                            }
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
                            usersRepository.updateCurrentUserProfile(updatedAppUser)
                        }
                    }
                }
            }
        }

        override suspend fun updateUser(user: AppUser) = eitherWrapper.wrapUnit {
            usersRepository.updateCurrentUserProfile(user)
        }
    }
}