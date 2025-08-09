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

package ru.aleshin.studyassistant.core.remote.api.billing

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import ru.aleshin.studyassistant.core.common.extensions.fromJson
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapFailure
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapServiceError
import ru.aleshin.studyassistant.core.domain.entities.billing.SubscriptionIdentifier
import ru.aleshin.studyassistant.core.remote.models.billing.RuStoreSubscriptionStatePojo
import ru.aleshin.studyassistant.core.remote.models.billing.SubscriptionStatusPojo

/**
 * @author Stanislav Aleshin on 09.08.2025.
 */
interface RuStoreSubscriptionStatusProvider : SubscriptionStatusProvider<SubscriptionIdentifier.RuStore> {

    class Base(
        private val httpClient: HttpClient,
        private val jweTokenProvider: RuStoreJweTokenProvider,
    ) : RuStoreSubscriptionStatusProvider {

        companion object {
            const val STATE_BASE_URL = "https://public-api.rustore.ru/public/v3/subscription"
            const val STATUS_BASE_URL = "https://public-api.rustore.ru/public/subscription"
            const val PACKAGE_NAME = "ru.aleshin.studyassistant"
            const val JWE_TOKEN_HEADER = "Public-Token"
        }

        override suspend fun fetchStatus(identifier: SubscriptionIdentifier.RuStore): SubscriptionStatusPojo {
            val jweToken = jweTokenProvider.getJweToken()

            val subscriptionStateResponse = httpClient.get {
                url("$STATE_BASE_URL/$PACKAGE_NAME/${identifier.subscriptionId}/${identifier.subscriptionToken}")
                headers.append(JWE_TOKEN_HEADER, jweToken)
            }

            val subscriptionStatusResponse = httpClient.get {
                url("$STATUS_BASE_URL/${identifier.subscriptionToken}/state")
                headers.append(JWE_TOKEN_HEADER, jweToken)
            }

            if (!subscriptionStateResponse.status.isSuccess()) {
                throw IapServiceError(IapFailure.UnknownError)
            }

            val subscriptionState = subscriptionStateResponse.bodyAsText().fromJson<RuStoreSubscriptionStatePojo>()
            val subscriptionStatusBody = try {
                subscriptionStatusResponse.bodyAsText().fromJson<JsonElement>().jsonObject["body"]
            } catch (e: Exception) {
                null
            }
            val subscriptionStatus = subscriptionStatusBody?.jsonObject["is_active"]?.jsonPrimitive?.booleanOrNull

            return SubscriptionStatusPojo(
                subscriptionId = identifier.subscriptionId,
                subscriptionToken = identifier.subscriptionToken,
                isActive = subscriptionStatus ?: (subscriptionState.paymentState != null),
                expiryTimeMillis = subscriptionState.expiryTimeMillis.toLong(),
                lastInvoiceId = subscriptionState.orderId.split("..")[0],
            )
        }
    }
}