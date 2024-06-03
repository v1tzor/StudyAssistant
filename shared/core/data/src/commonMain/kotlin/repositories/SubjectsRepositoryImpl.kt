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

import database.subjects.SubjectsLocalDataSource
import entities.subject.Subject
import functional.UID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import mappers.subjects.mapToData
import mappers.subjects.mapToDomain
import payments.SubscriptionChecker
import remote.subjects.SubjectsRemoteDataSource

/**
 * @author Stanislav Aleshin on 02.06.2024.
 */
class SubjectsRepositoryImpl(
    private val remoteDataSource: SubjectsRemoteDataSource,
    private val localDataSource: SubjectsLocalDataSource,
    private val subscriptionChecker: SubscriptionChecker,
) : SubjectsRepository {

    override suspend fun addOrUpdateSubject(subject: Subject, targetUser: UID): UID {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        return if (isSubscriber) {
            remoteDataSource.addOrUpdateSubject(subject.mapToData(), targetUser)
        } else {
            localDataSource.addOrUpdateSubject(subject.mapToData())
        }
    }

    override suspend fun fetchSubjectById(uid: UID, targetUser: UID): Flow<Subject?> {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        val subjectFlow = if (isSubscriber) {
            remoteDataSource.fetchSubjectById(uid, targetUser)
        } else {
            localDataSource.fetchSubjectById(uid)
        }

        return subjectFlow.map { subjectData ->
            subjectData?.mapToDomain()
        }
    }
    override suspend fun fetchAllSubjectsByOrganization(organizationId: UID, targetUser: UID): Flow<List<Subject>> {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        val subjectListFlow = if (isSubscriber) {
            remoteDataSource.fetchAllSubjectsByOrganization(organizationId, targetUser)
        } else {
            localDataSource.fetchAllSubjectsByOrganization(organizationId)
        }

        return subjectListFlow.map { subjectListData ->
            subjectListData.map { it.mapToDomain() }
        }
    }

    override suspend fun deleteSubject(targetId: UID, targetUser: UID) {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        return if (isSubscriber) {
            remoteDataSource.deleteSubject(targetId, targetUser)
        } else {
            localDataSource.deleteSubject(targetId)
        }
    }
}