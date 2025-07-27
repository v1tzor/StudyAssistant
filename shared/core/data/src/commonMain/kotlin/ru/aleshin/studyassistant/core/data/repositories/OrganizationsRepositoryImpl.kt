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
import ru.aleshin.studyassistant.core.data.mappers.organizations.convertToLocal
import ru.aleshin.studyassistant.core.data.mappers.organizations.convertToRemote
import ru.aleshin.studyassistant.core.data.mappers.organizations.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.organizations.mapToLocalData
import ru.aleshin.studyassistant.core.data.mappers.organizations.mapToRemoteData
import ru.aleshin.studyassistant.core.data.utils.SubscriptionChecker
import ru.aleshin.studyassistant.core.data.utils.sync.RemoteResultSyncHandler
import ru.aleshin.studyassistant.core.database.datasource.organizations.OrganizationsLocalDataSource
import ru.aleshin.studyassistant.core.domain.common.DataTransferDirection
import ru.aleshin.studyassistant.core.domain.entities.files.InputFile
import ru.aleshin.studyassistant.core.domain.entities.organizations.Organization
import ru.aleshin.studyassistant.core.domain.entities.organizations.OrganizationShort
import ru.aleshin.studyassistant.core.domain.entities.sync.OfflineChangeType
import ru.aleshin.studyassistant.core.domain.managers.sync.OrganizationsSourceSyncManager.Companion.ORGANIZATIONS_SOURCE_KEY
import ru.aleshin.studyassistant.core.domain.repositories.OrganizationsRepository
import ru.aleshin.studyassistant.core.remote.datasources.organizations.OrganizationsRemoteDataSource

/**
 * @author Stanislav Aleshin on 29.04.2024.
 */
class OrganizationsRepositoryImpl(
    private val localDataSource: OrganizationsLocalDataSource,
    private val remoteDataSource: OrganizationsRemoteDataSource,
    private val subscriptionChecker: SubscriptionChecker,
    private val userSessionProvider: UserSessionProvider,
    private val resultSyncHandler: RemoteResultSyncHandler,
) : OrganizationsRepository {

    override suspend fun addOrUpdateOrganization(organization: Organization): UID {
        val currentUser = userSessionProvider.getCurrentUserId()
        val isSubscriber = subscriptionChecker.getSubscriberStatus()

        val upsertModel = organization.copy(uid = organization.uid.ifBlank { randomUUID() })

        if (isSubscriber) {
            localDataSource.sync().addOrUpdateItem(upsertModel.mapToLocalData())
            resultSyncHandler.executeOrAddToQueue(
                data = upsertModel.mapToRemoteData(userId = currentUser),
                type = OfflineChangeType.UPSERT,
                sourceKey = ORGANIZATIONS_SOURCE_KEY,
            ) {
                remoteDataSource.addOrUpdateItem(it)
            }
        } else {
            localDataSource.offline().addOrUpdateItem(upsertModel.mapToLocalData())
        }

        return upsertModel.uid
    }

    override suspend fun addOrUpdateOrganizationsGroup(organizations: List<Organization>) {
        val currentUser = userSessionProvider.getCurrentUserId()
        val isSubscriber = subscriptionChecker.getSubscriberStatus()

        val upsertModels = organizations.map { subject ->
            subject.copy(uid = subject.uid.ifBlank { randomUUID() })
        }

        if (isSubscriber) {
            localDataSource.sync().addOrUpdateItems(upsertModels.map { it.mapToLocalData() })
            resultSyncHandler.executeOrAddToQueue(
                data = upsertModels.map { it.mapToRemoteData(userId = currentUser) },
                type = OfflineChangeType.UPSERT,
                sourceKey = ORGANIZATIONS_SOURCE_KEY,
            ) {
                remoteDataSource.addOrUpdateItems(it)
            }
        } else {
            localDataSource.offline().addOrUpdateItems(upsertModels.map { it.mapToLocalData() })
        }
    }

    override suspend fun uploadAvatar(oldAvatarUrl: String?, file: InputFile): String {
        return remoteDataSource.uploadAvatar(oldAvatarUrl, file)
    }

    override suspend fun fetchOrganizationById(uid: UID): Flow<Organization?> {
        return subscriptionChecker.getSubscriberStatusFlow().flatMapLatest { isSubscriber ->
            if (isSubscriber) {
                localDataSource.sync().fetchOrganizationDetailsById(uid).map { organizationEntity ->
                    organizationEntity?.mapToDomain()
                }
            } else {
                localDataSource.offline().fetchOrganizationDetailsById(uid).map { organizationEntity ->
                    organizationEntity?.mapToDomain()
                }
            }
        }
    }

    override suspend fun fetchOrganizationsById(uid: List<UID>): Flow<List<Organization>> {
        return subscriptionChecker.getSubscriberStatusFlow().flatMapLatest { isSubscriber ->
            if (isSubscriber) {
                localDataSource.sync().fetchOrganizationsDetailsById(uid).map { organizations ->
                    organizations.map { organizationEntity -> organizationEntity.mapToDomain() }
                }
            } else {
                localDataSource.offline().fetchOrganizationsDetailsById(uid).map { organizations ->
                    organizations.map { organizationEntity -> organizationEntity.mapToDomain() }
                }
            }
        }
    }

    override suspend fun fetchShortOrganizationById(uid: UID): Flow<OrganizationShort?> {
        return subscriptionChecker.getSubscriberStatusFlow().flatMapLatest { isSubscriber ->
            if (isSubscriber) {
                localDataSource.sync().fetchShortOrganizationById(uid).map { organizationEntity ->
                    organizationEntity?.mapToDomain()
                }
            } else {
                localDataSource.offline().fetchShortOrganizationById(uid).map { organizationEntity ->
                    organizationEntity?.mapToDomain()
                }
            }
        }
    }

    override suspend fun fetchAllOrganization(): Flow<List<Organization>> {
        return subscriptionChecker.getSubscriberStatusFlow().flatMapLatest { isSubscriber ->
            if (isSubscriber) {
                localDataSource.sync().fetchAllOrganizationDetails().map { organizations ->
                    organizations.map { organizationEntity -> organizationEntity.mapToDomain() }
                }
            } else {
                localDataSource.offline().fetchAllOrganizationDetails().map { organizations ->
                    organizations.map { organizationEntity -> organizationEntity.mapToDomain() }
                }
            }
        }
    }

    override suspend fun fetchAllShortOrganization(): Flow<List<OrganizationShort>> {
        return subscriptionChecker.getSubscriberStatusFlow().flatMapLatest { isSubscriber ->
            if (isSubscriber) {
                localDataSource.sync().fetchAllShortOrganization().map { organizations ->
                    organizations.map { organizationEntity -> organizationEntity.mapToDomain() }
                }
            } else {
                localDataSource.offline().fetchAllShortOrganization().map { organizations ->
                    organizations.map { organizationEntity -> organizationEntity.mapToDomain() }
                }
            }
        }
    }

    override suspend fun deleteAvatar(avatarUrl: String) {
        remoteDataSource.deleteAvatar(avatarUrl)
    }

    override suspend fun transferData(direction: DataTransferDirection) {
        val currentUser = userSessionProvider.getCurrentUserId()
        when (direction) {
            DataTransferDirection.REMOTE_TO_LOCAL -> {
                val allOrganizationsFlow = remoteDataSource.fetchAllItems(currentUser)
                val organizations = allOrganizationsFlow.first().map { it.convertToLocal() }
                localDataSource.offline().deleteAllItems()
                localDataSource.offline().addOrUpdateItems(organizations)
            }
            DataTransferDirection.LOCAL_TO_REMOTE -> {
                val allOrganizations = localDataSource.offline().fetchAllOrganization().first()
                val organizationsRemote = allOrganizations.map { it.convertToRemote(currentUser) }

                remoteDataSource.deleteAllItems(currentUser)
                remoteDataSource.addOrUpdateItems(organizationsRemote)

                localDataSource.sync().deleteAllItems()
                localDataSource.sync().addOrUpdateItems(allOrganizations)
            }
        }
    }
}