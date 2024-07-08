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

package ru.aleshin.studyassistant.core.data.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.payments.SubscriptionChecker
import ru.aleshin.studyassistant.core.data.mappers.subjects.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.subjects.mapToLocalData
import ru.aleshin.studyassistant.core.data.mappers.subjects.mapToRemoteData
import ru.aleshin.studyassistant.core.database.datasource.subjects.SubjectsLocalDataSource
import ru.aleshin.studyassistant.core.domain.entities.subject.Subject
import ru.aleshin.studyassistant.core.domain.repositories.SubjectsRepository
import ru.aleshin.studyassistant.core.remote.datasources.subjects.SubjectsRemoteDataSource

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
            remoteDataSource.addOrUpdateSubject(subject.mapToRemoteData(), targetUser)
        } else {
            localDataSource.addOrUpdateSubject(subject.mapToLocalData())
        }
    }

    override suspend fun fetchSubjectById(uid: UID, targetUser: UID): Flow<Subject?> {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        return if (isSubscriber) {
            remoteDataSource.fetchSubjectById(uid, targetUser).map { subjectPojo -> subjectPojo?.mapToDomain() }
        } else {
            localDataSource.fetchSubjectById(uid).map { subjectEntity -> subjectEntity?.mapToDomain() }
        }
    }
    override suspend fun fetchAllSubjectsByOrganization(organizationId: UID, targetUser: UID): Flow<List<Subject>> {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        return if (isSubscriber) {
            remoteDataSource.fetchAllSubjectsByOrganization(organizationId, targetUser).map { subjects ->
                subjects.map { subjectPojo -> subjectPojo.mapToDomain() }
            }
        } else {
            localDataSource.fetchAllSubjectsByOrganization(organizationId).map { subjects ->
                subjects.map { subjectEntity -> subjectEntity.mapToDomain() }
            }
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