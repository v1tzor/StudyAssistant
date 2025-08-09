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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.io.IOException
import ru.aleshin.studyassistant.core.api.AppwriteApi.Products
import ru.aleshin.studyassistant.core.api.databases.DatabaseService
import ru.aleshin.studyassistant.core.common.platform.services.CrashlyticsService
import ru.aleshin.studyassistant.core.domain.entities.billing.SubscriptionIdentifier
import ru.aleshin.studyassistant.core.remote.models.billing.ProductPojo
import ru.aleshin.studyassistant.core.remote.models.billing.SubscriptionStatusPojo

/**
 * @author Stanislav Aleshin on 18.06.2025.
 */
interface SubscriptionsRemoteApi {

    suspend fun fetchSubscriptionsIds(): Flow<List<ProductPojo>>

    suspend fun fetchSubscriptionStatus(identifier: SubscriptionIdentifier): SubscriptionStatusPojo?

    class Base(
        private val database: DatabaseService,
        private val subscriptionStatusProviderFactory: SubscriptionStatusProviderFactory,
        private val crashlyticsService: CrashlyticsService,
    ) : SubscriptionsRemoteApi {

        private companion object {
            const val TAG = "SUBSCRIPTION_CHECKER"
        }

        override suspend fun fetchSubscriptionsIds(): Flow<List<ProductPojo>> {
            return database.listDocumentsFlow(
                databaseId = Products.DATABASE_ID,
                collectionId = Products.COLLECTION_ID,
                nestedType = ProductPojo.serializer(),
            ).map { documents ->
                documents.map { it.data }
            }
        }

        override suspend fun fetchSubscriptionStatus(identifier: SubscriptionIdentifier): SubscriptionStatusPojo? {
            return try {
                val statusProvider = subscriptionStatusProviderFactory.createProvider(identifier)
                statusProvider.fetchStatus(identifier)
            } catch (_: IOException) {
                null
            } catch (e: Exception) {
                crashlyticsService.recordException(TAG, e.message ?: "", e)
                null
            }
        }
    }
}