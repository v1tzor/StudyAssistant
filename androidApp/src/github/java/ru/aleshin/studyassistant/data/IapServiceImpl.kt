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

import ru.aleshin.studyassistant.core.common.platform.PlatformActivity
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapPaymentResultInvalidPaymentState
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapProduct
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapProductPurchaseParams
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapPurchase
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapService
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapServiceAvailability
import ru.aleshin.studyassistant.core.common.platform.services.iap.PlatformIntent
import ru.aleshin.studyassistant.core.common.platform.services.iap.Store

/**
 * @author Stanislav Aleshin on 20.06.2025.
 */
class IapServiceImpl : IapService {
    override fun init(activity: PlatformActivity) = Unit
    override fun fetchStore() = Store.NONE
    override fun proceedIntent(intent: PlatformIntent?, requestCode: Int?) = Unit
    override suspend fun isAuthorizedUser() = false
    override suspend fun fetchServiceAvailability() = IapServiceAvailability.Unavailable(null)
    override suspend fun purchaseProduct(params: IapProductPurchaseParams) = IapPaymentResultInvalidPaymentState
    override suspend fun fetchProducts(ids: List<String>) = emptyList<IapProduct>()
    override suspend fun fetchPurchases() = emptyList<IapPurchase>()
    override suspend fun fetchPurchaseInfo(purchaseId: String) = null
    override suspend fun confirmPurchase(purchaseId: String, developerPayload: String?) = Unit
    override suspend fun deletePurchase(purchaseId: String) = Unit
}