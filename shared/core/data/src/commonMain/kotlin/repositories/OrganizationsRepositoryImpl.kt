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

import database.organizations.OrganizationsLocalDataSource
import entities.organizations.Organization
import functional.UID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import mappers.organizations.mapToData
import mappers.organizations.mapToDomain
import payments.SubscriptionChecker
import remote.organizations.OrganizationsRemoteDataSource

/**
 * @author Stanislav Aleshin on 29.04.2024.
 */
class OrganizationsRepositoryImpl(
    private val localDataSource: OrganizationsLocalDataSource,
    private val remoteDataSource: OrganizationsRemoteDataSource,
    private val subscriptionChecker: SubscriptionChecker,
) : OrganizationsRepository {

    override suspend fun fetchAllOrganization(targetUser: UID): Flow<List<Organization>> {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()
        val organizationsFlow = if (isSubscriber) {
            remoteDataSource.fetchAllOrganization(targetUser)
        } else {
            localDataSource.fetchAllOrganization()
        }

        return organizationsFlow.map { organizationListData ->
            organizationListData.map { organization -> organization.mapToDomain() }
        }
    }

    override suspend fun fetchOrganizationById(uid: UID, targetUser: UID): Flow<Organization> {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()
        val organizationFlow = if (isSubscriber) {
            remoteDataSource.fetchOrganizationById(uid, targetUser)
        } else {
            localDataSource.fetchOrganizationById(uid)
        }

        return organizationFlow.map { organizationData ->
            organizationData.mapToDomain()
        }
    }

    override suspend fun addOrUpdateOrganization(organization: Organization, targetUser: UID): UID {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        return if (isSubscriber) {
            remoteDataSource.addOrUpdateOrganization(organization.mapToData(), targetUser)
        } else {
            localDataSource.addOrUpdateOrganization(organization.mapToData())
        }
    }
}
