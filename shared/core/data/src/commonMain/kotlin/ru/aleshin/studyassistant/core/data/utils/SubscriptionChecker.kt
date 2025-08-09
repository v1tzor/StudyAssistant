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

package ru.aleshin.studyassistant.core.data.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import ru.aleshin.studyassistant.core.common.functional.Constants
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.database.datasource.user.UserLocalDataSource
import ru.aleshin.studyassistant.core.database.models.users.AppUserDetailsEntity

/**
 * @author Stanislav Aleshin on 30.04.2024.
 */
interface SubscriptionChecker {

    suspend fun getSubscriptionActive(): Boolean

    suspend fun getSubscriptionActiveFlow(): Flow<Boolean>

    class Base(
        private val currentUserStorage: UserLocalDataSource,
        private val dateManager: DateManager,
    ) : SubscriptionChecker {

        override suspend fun getSubscriptionActive(): Boolean {
            val currentUser = currentUserStorage.fetchCurrentUserDetails().first()
            return isActiveSubscription(currentUser)
        }

        override suspend fun getSubscriptionActiveFlow(): Flow<Boolean> {
            val currentUserFlow = currentUserStorage.fetchCurrentUserDetails()
            return currentUserFlow.map { currentUser -> isActiveSubscription(currentUser) }.distinctUntilChanged()
        }

        private fun isActiveSubscription(currentUser: AppUserDetailsEntity?): Boolean {
            val subscriptionInfo = currentUser?.subscriptionInfo
            return if (subscriptionInfo != null) {
                val gracePeriod = Constants.Date.MILLIS_IN_DAY * Constants.Date.DAYS_IN_WEEK
                val endTime = subscriptionInfo.expiryTimeMillis + gracePeriod
                val currentTime = dateManager.fetchCurrentInstant().toEpochMilliseconds()
                currentTime <= endTime
            } else {
                false
            }
        }
    }
}