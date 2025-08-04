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

@file:OptIn(ExperimentalCoroutinesApi::class)

package ru.aleshin.studyassistant.core.data.repositories

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import ru.aleshin.studyassistant.core.api.auth.UserSessionProvider
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.data.mappers.subjects.convertToLocal
import ru.aleshin.studyassistant.core.data.mappers.subjects.convertToRemote
import ru.aleshin.studyassistant.core.data.mappers.subjects.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.subjects.mapToLocalData
import ru.aleshin.studyassistant.core.data.mappers.subjects.mapToRemoteData
import ru.aleshin.studyassistant.core.data.utils.SubscriptionChecker
import ru.aleshin.studyassistant.core.data.utils.sync.RemoteResultSyncHandler
import ru.aleshin.studyassistant.core.database.datasource.subjects.SubjectsLocalDataSource
import ru.aleshin.studyassistant.core.domain.common.DataTransferDirection
import ru.aleshin.studyassistant.core.domain.entities.subject.Subject
import ru.aleshin.studyassistant.core.domain.entities.sync.OfflineChangeType
import ru.aleshin.studyassistant.core.domain.managers.sync.SubjectsSourceSyncManager.Companion.SUBJECT_SOURCE_KEY
import ru.aleshin.studyassistant.core.domain.repositories.SubjectsRepository
import ru.aleshin.studyassistant.core.remote.datasources.subjects.SubjectsRemoteDataSource

/**
 * @author Stanislav Aleshin on 02.06.2024.
 */
class SubjectsRepositoryImpl(
    private val remoteDataSource: SubjectsRemoteDataSource,
    private val localDataSource: SubjectsLocalDataSource,
    private val subscriptionChecker: SubscriptionChecker,
    private val userSessionProvider: UserSessionProvider,
    private val resultSyncHandler: RemoteResultSyncHandler
) : SubjectsRepository {

    override suspend fun addOrUpdateSubject(subject: Subject): UID {
        val currentUser = userSessionProvider.getCurrentUserId()
        val isSubscriber = subscriptionChecker.getSubscriberStatus()

        val upsertModel = subject.copy(uid = subject.uid.ifBlank { randomUUID() })

        if (isSubscriber) {
            localDataSource.sync().addOrUpdateItem(upsertModel.mapToLocalData())
            resultSyncHandler.executeOrAddToQueue(
                data = upsertModel.mapToRemoteData(userId = currentUser),
                type = OfflineChangeType.UPSERT,
                sourceKey = SUBJECT_SOURCE_KEY,
            ) {
                remoteDataSource.addOrUpdateItem(it)
            }
        } else {
            localDataSource.offline().addOrUpdateItem(upsertModel.mapToLocalData())
        }

        return upsertModel.uid
    }

    override suspend fun addOrUpdateSubjectsGroup(subjects: List<Subject>) {
        val currentUser = userSessionProvider.getCurrentUserId()
        val isSubscriber = subscriptionChecker.getSubscriberStatus()

        val upsertModels = subjects.map { subject ->
            subject.copy(uid = subject.uid.ifBlank { randomUUID() })
        }

        if (isSubscriber) {
            localDataSource.sync().addOrUpdateItems(upsertModels.map { it.mapToLocalData() })
            resultSyncHandler.executeOrAddToQueue(
                data = upsertModels.map { it.mapToRemoteData(userId = currentUser) },
                type = OfflineChangeType.UPSERT,
                sourceKey = SUBJECT_SOURCE_KEY,
            ) {
                remoteDataSource.addOrUpdateItems(it)
            }
        } else {
            localDataSource.offline().addOrUpdateItems(upsertModels.map { it.mapToLocalData() })
        }
    }

    override suspend fun fetchSubjectById(uid: UID): Flow<Subject?> {
        return subscriptionChecker.getSubscriberStatusFlow().flatMapLatest { isSubscriber ->
            if (isSubscriber) {
                localDataSource.sync().fetchSubjectDetailsById(uid).map { subjectEntity ->
                    subjectEntity?.mapToDomain()
                }
            } else {
                localDataSource.offline().fetchSubjectDetailsById(uid).map { subjectEntity ->
                    subjectEntity?.mapToDomain()
                }
            }
        }
    }
    override suspend fun fetchAllSubjectsByOrganization(organizationId: UID): Flow<List<Subject>> {
        return subscriptionChecker.getSubscriberStatusFlow().flatMapLatest { isSubscriber ->
            if (isSubscriber) {
                localDataSource.sync().fetchAllSubjectsDetailsByOrg(organizationId).map { subjects ->
                    subjects.map { subjectEntity -> subjectEntity.mapToDomain() }
                }
            } else {
                localDataSource.offline().fetchAllSubjectsDetailsByOrg(organizationId).map { subjects ->
                    subjects.map { subjectEntity -> subjectEntity.mapToDomain() }
                }
            }
        }
    }

    override suspend fun fetchSubjectsByEmployee(employeeId: UID): Flow<List<Subject>> {
        return subscriptionChecker.getSubscriberStatusFlow().flatMapLatest { isSubscriber ->
            if (isSubscriber) {
                localDataSource.sync().fetchSubjectsDetailsByEmployee(employeeId).map { subjects ->
                    subjects.map { subjectEntity -> subjectEntity.mapToDomain() }
                }
            } else {
                localDataSource.offline().fetchSubjectsDetailsByEmployee(employeeId).map { subjects ->
                    subjects.map { subjectEntity -> subjectEntity.mapToDomain() }
                }
            }
        }
    }

    override suspend fun fetchAllSubjectsByNames(names: List<UID>): List<Subject> {
        val isSubscriber = subscriptionChecker.getSubscriberStatus()

        return if (isSubscriber) {
            localDataSource.sync().fetchAllSubjectsDetailsByNames(names).map { subjectEntity ->
                subjectEntity.mapToDomain()
            }
        } else {
            localDataSource.offline().fetchAllSubjectsDetailsByNames(names).map { subjectEntity ->
                subjectEntity.mapToDomain()
            }
        }
    }

    override suspend fun deleteSubject(targetId: UID) {
        val isSubscriber = subscriptionChecker.getSubscriberStatus()

        return if (isSubscriber) {
            localDataSource.sync().deleteItemsById(listOf(targetId))
            resultSyncHandler.executeOrAddToQueue(
                documentId = targetId,
                type = OfflineChangeType.DELETE,
                sourceKey = SUBJECT_SOURCE_KEY,
            ) {
                remoteDataSource.deleteItemById(targetId)
            }
        } else {
            localDataSource.offline().deleteItemsById(listOf(targetId))
        }
    }

    override suspend fun transferData(direction: DataTransferDirection, mergeData: Boolean) {
        val currentUser = userSessionProvider.getCurrentUserId()
        when (direction) {
            DataTransferDirection.REMOTE_TO_LOCAL -> {
                val allSubjectsFlow = remoteDataSource.fetchAllItems(currentUser)
                val subjects = allSubjectsFlow.first().map { it.convertToLocal() }

                if (!mergeData) {
                    localDataSource.offline().deleteAllItems()
                }
                localDataSource.offline().addOrUpdateItems(subjects)
            }
            DataTransferDirection.LOCAL_TO_REMOTE -> {
                val allSubjects = localDataSource.offline().fetchAllSubjects().first()
                val subjectsRemote = allSubjects.map { it.convertToRemote(currentUser) }

                if (!mergeData) {
                    remoteDataSource.deleteAllItems(currentUser)
                }
                remoteDataSource.addOrUpdateItems(subjectsRemote)

                localDataSource.sync().deleteAllItems()
                localDataSource.sync().addOrUpdateItems(allSubjects)
            }
        }
    }
}