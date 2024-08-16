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

import dev.gitlive.firebase.storage.File
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.uriString
import ru.aleshin.studyassistant.core.common.payments.SubscriptionChecker
import ru.aleshin.studyassistant.core.data.mappers.organizations.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.organizations.mapToLocalData
import ru.aleshin.studyassistant.core.data.mappers.organizations.mapToRemoteData
import ru.aleshin.studyassistant.core.database.datasource.organizations.OrganizationsLocalDataSource
import ru.aleshin.studyassistant.core.domain.entities.organizations.Organization
import ru.aleshin.studyassistant.core.domain.entities.organizations.OrganizationShort
import ru.aleshin.studyassistant.core.domain.repositories.OrganizationsRepository
import ru.aleshin.studyassistant.core.remote.datasources.organizations.OrganizationsRemoteDataSource

/**
 * @author Stanislav Aleshin on 29.04.2024.
 */
class OrganizationsRepositoryImpl(
    private val localDataSource: OrganizationsLocalDataSource,
    private val remoteDataSource: OrganizationsRemoteDataSource,
    private val subscriptionChecker: SubscriptionChecker,
) : OrganizationsRepository {

    override suspend fun addOrUpdateOrganization(organization: Organization, targetUser: UID): UID {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        return if (isSubscriber) {
            remoteDataSource.addOrUpdateOrganization(organization.mapToRemoteData(), targetUser)
        } else {
            localDataSource.addOrUpdateOrganization(organization.mapToLocalData())
        }
    }

    override suspend fun addOrUpdateOrganizationsGroup(organizations: List<Organization>, targetUser: UID) {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        return if (isSubscriber) {
            remoteDataSource.addOrUpdateOrganizationsGroup(organizations.map { it.mapToRemoteData() }, targetUser)
        } else {
            localDataSource.addOrUpdateOrganizationsGroup(organizations.map { it.mapToLocalData() })
        }
    }

    override suspend fun uploadAvatar(uid: UID, file: File, targetUser: UID): String {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        return if (isSubscriber) {
            remoteDataSource.uploadAvatar(uid, file, targetUser)
        } else {
            file.uriString()
        }
    }

    override suspend fun fetchOrganizationById(uid: UID, targetUser: UID): Flow<Organization?> {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        return if (isSubscriber) {
            remoteDataSource.fetchOrganizationById(uid, targetUser).map { organizationPojo ->
                organizationPojo?.mapToDomain()
            }
        } else {
            localDataSource.fetchOrganizationById(uid).map { organizationEntity ->
                organizationEntity?.mapToDomain()
            }
        }
    }

    override suspend fun fetchOrganizationsById(uid: List<UID>, targetUser: UID): Flow<List<Organization>> {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        return if (isSubscriber) {
            remoteDataSource.fetchOrganizationsById(uid, targetUser).map { organizations ->
                organizations.map { organizationPojo -> organizationPojo.mapToDomain() }
            }
        } else {
            localDataSource.fetchOrganizationsById(uid).map { organizations ->
                organizations.map { organizationEntity -> organizationEntity.mapToDomain() }
            }
        }
    }

    override suspend fun fetchShortOrganizationById(uid: UID, targetUser: UID): Flow<OrganizationShort?> {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        return if (isSubscriber) {
            remoteDataSource.fetchShortOrganizationById(uid, targetUser).map { organizationPojo ->
                organizationPojo?.mapToDomain()
            }
        } else {
            localDataSource.fetchShortOrganizationById(uid).map { organizationEntity ->
                organizationEntity?.mapToDomain()
            }
        }
    }

    override suspend fun fetchAllOrganization(targetUser: UID): Flow<List<Organization>> {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        return if (isSubscriber) {
            remoteDataSource.fetchAllOrganization(targetUser).map { organizations ->
                organizations.map { organizationPojo -> organizationPojo.mapToDomain() }
            }
        } else {
            localDataSource.fetchAllOrganization().map { organizations ->
                organizations.map { organizationEntity -> organizationEntity.mapToDomain() }
            }
        }
    }

    override suspend fun fetchAllShortOrganization(targetUser: UID): Flow<List<OrganizationShort>> {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        return if (isSubscriber) {
            remoteDataSource.fetchAllShortOrganization(targetUser).map { organizations ->
                organizations.map { organizationPojo -> organizationPojo.mapToDomain() }
            }
        } else {
            localDataSource.fetchAllShortOrganization().map { organizations ->
                organizations.map { organizationEntity -> organizationEntity.mapToDomain() }
            }
        }
    }

    override suspend fun deleteAvatar(uid: UID, targetUser: UID) {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        if (isSubscriber) {
            remoteDataSource.deleteAvatar(uid, targetUser)
        }
    }
}