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

package ru.aleshin.studyassistant.core.common.platform.services.iap

import ru.aleshin.studyassistant.core.common.platform.PlatformActivity

/**
 * @author Stanislav Aleshin on 16.06.2025.
 */
interface IapService {
    fun init(activity: PlatformActivity)
    fun fetchStore(): Store
    fun proceedIntent(intent: PlatformIntent?, requestCode: Int?)
    suspend fun isAuthorizedUser(): Boolean
    suspend fun fetchServiceAvailability(): IapServiceAvailability
    suspend fun purchaseProduct(params: IapProductPurchaseParams): IapPaymentResult
    suspend fun fetchProducts(ids: List<String>): List<IapProduct>
    suspend fun fetchPurchases(): List<IapPurchase>
    suspend fun fetchPurchaseInfo(purchaseId: String): IapPurchase?
    suspend fun confirmPurchase(purchaseId: String, developerPayload: String?)
    suspend fun deletePurchase(purchaseId: String)
}