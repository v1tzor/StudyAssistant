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

package repositories

import database.classes.ClassLocalDataSource
import entities.classes.Class
import functional.UID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import mappers.tasks.mapToData
import mappers.tasks.mapToDomain
import payments.SubscriptionChecker
import remote.classes.ClassRemoteDataSource

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
class ClassRepositoryImpl(
    private val remoteDataSource: ClassRemoteDataSource,
    private val localDataSource: ClassLocalDataSource,
    private val subscriptionChecker: SubscriptionChecker,
) : ClassRepository {

    override suspend fun fetchClassById(uid: UID, targetUser: UID): Flow<Class?> {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        val classFlow = if (isSubscriber) {
            remoteDataSource.fetchClassById(uid, targetUser)
        } else {
            localDataSource.fetchClassById(uid)
        }

        return classFlow.map { scheduleClass ->
            scheduleClass?.mapToDomain()
        }
    }

    override suspend fun addOrUpdateClass(scheduleClass: Class, targetUser: UID): UID {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        return if (isSubscriber) {
            remoteDataSource.addOrUpdateClass(scheduleClass.mapToData(), targetUser)
        } else {
            localDataSource.addOrUpdateClass(scheduleClass.mapToData())
        }
    }

    override suspend fun deleteClass(uid: UID, targetUser: UID) {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        return if (isSubscriber) {
            remoteDataSource.deleteClass(uid, targetUser)
        } else {
            localDataSource.deleteClass(uid)
        }
    }
}
