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

package ru.aleshin.studyassistant.core.remote.datasources.billing

import ru.aleshin.studyassistant.core.api.AppwriteApi.Users
import ru.aleshin.studyassistant.core.api.auth.AccountApi
import ru.aleshin.studyassistant.core.api.databases.DatabaseApi
import ru.aleshin.studyassistant.core.common.functional.Constants.Date.DAYS_IN_WEEK
import ru.aleshin.studyassistant.core.common.functional.Constants.Date.MILLIS_IN_DAY
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.remote.mappers.users.convertToDetails
import ru.aleshin.studyassistant.core.remote.models.users.AppUserPojo

/**
 * @author Stanislav Aleshin on 30.04.2024.
 */
interface SubscriptionChecker {

    suspend fun checkSubscriptionActivity(): Boolean

    class Base(
        private val account: AccountApi,
        private val database: DatabaseApi,
        private val dateManager: DateManager,
    ) : SubscriptionChecker {

        private var cacheResponse: Int = -1
        private var cacheResponseUser: String? = null

        override suspend fun checkSubscriptionActivity(): Boolean {
            val userId = account.getCurrentUser()?.id
            if (cacheResponse != -1 && cacheResponseUser == userId) {
                return cacheResponse == 1
            } else {
                cacheResponseUser = userId
                val isActive = if (userId == null) {
                    false
                } else {
                    val appUser = database.getDocumentOrNull(
                        databaseId = Users.DATABASE_ID,
                        collectionId = Users.COLLECTION_ID,
                        documentId = userId,
                        nestedType = AppUserPojo.serializer()
                    )?.data?.convertToDetails()
                    val subscriptionInfo = appUser?.subscriptionInfo
                    return if (subscriptionInfo != null) {
                        val gracePeriod = MILLIS_IN_DAY * DAYS_IN_WEEK
                        val endTime = subscriptionInfo.expiryTimeMillis + gracePeriod
                        val currentTime = dateManager.fetchCurrentInstant().toEpochMilliseconds()
                        currentTime <= endTime
                    } else {
                        false
                    }
                }
                cacheResponse = if (isActive) 1 else 0
                return isActive
            }
        }
    }
}