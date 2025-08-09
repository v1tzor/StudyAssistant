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

package ru.aleshin.studyassistant.core.domain.entities.billing

import ru.aleshin.studyassistant.core.common.platform.services.iap.IapPurchase
import ru.aleshin.studyassistant.core.common.platform.services.iap.Store

/**
 * @author Stanislav Aleshin on 09.08.2025.
 */
sealed interface SubscriptionIdentifier {

    val store: Store

    data class RuStore(val subscriptionId: String, val subscriptionToken: String) : SubscriptionIdentifier {
        override val store = Store.RU_STORE
    }

    data class AppGallery(val purchaseToken: String, val purchaseOrderId: String) : SubscriptionIdentifier {
        override val store = Store.APP_GALLERY
    }

    data class GooglePlay(val subscriptionId: String, val subscriptionToken: String) : SubscriptionIdentifier {
        override val store = Store.GOOGLE_PLAY
    }

    // Not supported yet
    object AppStore : SubscriptionIdentifier {
        override val store = Store.APP_STORE
    }
}

fun IapPurchase.fetchIdentifier(store: Store): SubscriptionIdentifier? {
    return when (store) {
        Store.RU_STORE -> if (subscriptionToken != null) {
            SubscriptionIdentifier.RuStore(productId, subscriptionToken!!)
        } else {
            null
        }
        Store.APP_GALLERY -> if (purchaseId != null && orderId != null) {
            SubscriptionIdentifier.AppGallery(purchaseId!!, orderId!!)
        } else {
            null
        }
        Store.GOOGLE_PLAY -> if (subscriptionToken != null) {
            SubscriptionIdentifier.GooglePlay(productId, subscriptionToken!!)
        } else {
            null
        }
        Store.APP_STORE -> SubscriptionIdentifier.AppStore
        else -> null
    }
}