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

import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import ru.aleshin.studyassistant.core.common.functional.Constants
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.database.mappers.user.convertToDetails
import ru.aleshin.studyassistant.core.database.mappers.user.mapToBase
import ru.aleshin.studyassistant.core.database.models.users.BaseAppUserEntity
import ru.aleshin.studyassistant.sqldelight.user.CurrentUserQueries
import kotlin.coroutines.CoroutineContext

/**
 * @author Stanislav Aleshin on 30.04.2024.
 */
interface SubscriptionChecker {

    suspend fun getSubscriberStatus(): Boolean

    suspend fun getSubscriberStatusFlow(): Flow<Boolean>

    class Base(
        private val currentUserStorage: CurrentUserQueries,
        private val coroutineManager: CoroutineManager,
        private val dateManager: DateManager,
    ) : SubscriptionChecker {

        private val coroutineContext: CoroutineContext
            get() = coroutineManager.backgroundDispatcher

        override suspend fun getSubscriberStatus(): Boolean {
            val currentUser = currentUserStorage.fetchUser().awaitAsOneOrNull()
            return isActiveSubscription(currentUser?.mapToBase())
        }

        override suspend fun getSubscriberStatusFlow(): Flow<Boolean> {
            val currentUserFlow = currentUserStorage.fetchUser().asFlow().mapToOneOrNull(coroutineContext)
            return currentUserFlow.map { currentUser ->
                isActiveSubscription(currentUser?.mapToBase())
            }.distinctUntilChanged()
        }

        private fun isActiveSubscription(currentUser: BaseAppUserEntity?): Boolean {
            val subscriptionInfo = currentUser?.convertToDetails()?.subscriptionInfo
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