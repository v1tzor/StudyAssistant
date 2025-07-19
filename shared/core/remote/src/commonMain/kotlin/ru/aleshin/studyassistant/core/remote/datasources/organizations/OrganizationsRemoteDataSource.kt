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

package ru.aleshin.studyassistant.core.remote.datasources.organizations

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import ru.aleshin.studyassistant.core.api.AppwriteApi.Employee
import ru.aleshin.studyassistant.core.api.AppwriteApi.Organizations
import ru.aleshin.studyassistant.core.api.AppwriteApi.Storage.BUCKET
import ru.aleshin.studyassistant.core.api.AppwriteApi.Subjects
import ru.aleshin.studyassistant.core.api.databases.DatabaseApi
import ru.aleshin.studyassistant.core.api.models.extractBucketIdFromFileUrl
import ru.aleshin.studyassistant.core.api.models.extractIdFromFileUrl
import ru.aleshin.studyassistant.core.api.storage.StorageApi
import ru.aleshin.studyassistant.core.api.utils.Permission
import ru.aleshin.studyassistant.core.api.utils.Query
import ru.aleshin.studyassistant.core.common.exceptions.AppwriteUserException
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.files.InputFile
import ru.aleshin.studyassistant.core.remote.mappers.organizations.mapToDetails
import ru.aleshin.studyassistant.core.remote.mappers.subjects.mapToDetails
import ru.aleshin.studyassistant.core.remote.models.organizations.OrganizationDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.organizations.OrganizationPojo
import ru.aleshin.studyassistant.core.remote.models.organizations.OrganizationShortPojo
import ru.aleshin.studyassistant.core.remote.models.subjects.SubjectPojo
import ru.aleshin.studyassistant.core.remote.models.users.EmployeePojo

/**
 * @author Stanislav Aleshin on 29.04.2024.
 */
interface OrganizationsRemoteDataSource {

    suspend fun addOrUpdateOrganization(organization: OrganizationPojo, targetUser: UID): UID
    suspend fun addOrUpdateOrganizationsGroup(organizations: List<OrganizationPojo>, targetUser: UID)
    suspend fun uploadAvatar(oldAvatarUrl: String?, file: InputFile, targetUser: UID): String
    suspend fun fetchOrganizationById(uid: UID, targetUser: UID): Flow<OrganizationDetailsPojo?>
    suspend fun fetchOrganizationsById(
        uid: List<UID>,
        targetUser: UID
    ): Flow<List<OrganizationDetailsPojo>>

    suspend fun fetchShortOrganizationById(uid: UID, targetUser: UID): Flow<OrganizationShortPojo?>
    suspend fun fetchAllOrganization(
        targetUser: UID,
        showHide: Boolean = false
    ): Flow<List<OrganizationDetailsPojo>>

    suspend fun fetchAllShortOrganization(targetUser: UID): Flow<List<OrganizationShortPojo>>
    suspend fun deleteAllOrganizations(targetUser: UID)
    suspend fun deleteAvatar(avatarUrl: String, targetUser: UID)

    class Base(
        private val database: DatabaseApi,
        private val storage: StorageApi,
    ) : OrganizationsRemoteDataSource {

        override suspend fun addOrUpdateOrganization(
            organization: OrganizationPojo,
            targetUser: UID
        ): UID {
            if (targetUser.isEmpty()) throw AppwriteUserException()

            val organizationId = organization.uid.takeIf { it.isNotBlank() } ?: randomUUID()

            database.upsertDocument(
                databaseId = Organizations.DATABASE_ID,
                collectionId = Organizations.COLLECTION_ID,
                documentId = organizationId,
                data = organization.copy(uid = organizationId),
                permissions = Permission.onlyUserData(targetUser),
                nestedType = OrganizationPojo.serializer(),
            )

            return organizationId
        }

        override suspend fun addOrUpdateOrganizationsGroup(
            organizations: List<OrganizationPojo>,
            targetUser: UID
        ) {
            if (targetUser.isEmpty()) throw AppwriteUserException()
            organizations.forEach { organization -> addOrUpdateOrganization(organization, targetUser) }
        }

        override suspend fun uploadAvatar(oldAvatarUrl: String?, file: InputFile, targetUser: UID): String {
            if (targetUser.isEmpty()) throw AppwriteUserException()

            if (!oldAvatarUrl.isNullOrBlank()) {
                deleteAvatar(oldAvatarUrl, targetUser)
            }

            val file = storage.createFile(
                bucketId = BUCKET,
                fileId = randomUUID(),
                file = file,
                permissions = Permission.avatarData(targetUser),
            )

            return file.getDownloadUrl()
        }

        override suspend fun fetchOrganizationById(
            uid: UID,
            targetUser: UID
        ): Flow<OrganizationDetailsPojo?> {
            if (uid.isEmpty()) return flowOf(null)
            if (targetUser.isEmpty()) throw AppwriteUserException()

            val organizationFlow = database.getDocumentFlow(
                databaseId = Organizations.DATABASE_ID,
                collectionId = Organizations.COLLECTION_ID,
                documentId = uid,
                nestedType = OrganizationPojo.serializer(),
            )

            return organizationFlow.flatMapToDetails()
        }

        override suspend fun fetchOrganizationsById(
            uid: List<UID>,
            targetUser: UID,
        ): Flow<List<OrganizationDetailsPojo>> {
            if (targetUser.isEmpty()) throw AppwriteUserException()

            val organizationFlow = database.listDocumentsFlow(
                databaseId = Organizations.DATABASE_ID,
                collectionId = Organizations.COLLECTION_ID,
                queries = listOf(Query.equal(Organizations.UID, uid)),
                nestedType = OrganizationPojo.serializer(),
            )

            return organizationFlow.flatMapListToDetails()
        }

        override suspend fun fetchShortOrganizationById(
            uid: UID,
            targetUser: UID
        ): Flow<OrganizationShortPojo?> {
            if (targetUser.isEmpty()) throw AppwriteUserException()

            val organizationFlow = database.getDocumentFlow(
                databaseId = Organizations.DATABASE_ID,
                collectionId = Organizations.COLLECTION_ID,
                documentId = uid,
                nestedType = OrganizationShortPojo.serializer(),
            )

            return organizationFlow
        }

        override suspend fun fetchAllOrganization(
            targetUser: UID,
            showHide: Boolean,
        ): Flow<List<OrganizationDetailsPojo>> {
            if (targetUser.isEmpty()) throw AppwriteUserException()

            val organizationFlow = database.listDocumentsFlow(
                databaseId = Organizations.DATABASE_ID,
                collectionId = Organizations.COLLECTION_ID,
                queries = if (showHide) {
                    listOf(Query.equal(Organizations.USER_ID, targetUser))
                } else {
                    listOf(
                        Query.equal(Organizations.USER_ID, targetUser),
                        Query.equal(Organizations.HIDE, false)
                    )
                },
                nestedType = OrganizationPojo.serializer(),
            )

            return organizationFlow.flatMapListToDetails()
        }

        override suspend fun fetchAllShortOrganization(targetUser: UID): Flow<List<OrganizationShortPojo>> {
            if (targetUser.isEmpty()) throw AppwriteUserException()

            val organizationFlow = database.listDocumentsFlow(
                databaseId = Organizations.DATABASE_ID,
                collectionId = Organizations.COLLECTION_ID,
                queries = listOf(
                    Query.equal(Organizations.USER_ID, targetUser),
                    Query.equal(Organizations.HIDE, false)
                ),
                nestedType = OrganizationShortPojo.serializer(),
            )

            return organizationFlow
        }

        override suspend fun deleteAllOrganizations(targetUser: UID) {
            if (targetUser.isEmpty()) throw AppwriteUserException()
            val organizationsFlow = fetchAllOrganization(targetUser, true)

            organizationsFlow.first().forEach { organization ->

                // Premium avatars are not deleted

                database.deleteDocument(
                    databaseId = Organizations.DATABASE_ID,
                    collectionId = Organizations.COLLECTION_ID,
                    documentId = organization.uid
                )
            }
        }

        override suspend fun deleteAvatar(avatarUrl: String, targetUser: UID) {
            if (targetUser.isEmpty()) throw AppwriteUserException()
            require(avatarUrl.isNotBlank()) { "Organization avatar url is empty" }

            storage.deleteFile(
                bucketId = avatarUrl.extractBucketIdFromFileUrl(),
                fileId = avatarUrl.extractIdFromFileUrl(),
            )
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        private fun Flow<List<OrganizationPojo>>.flatMapListToDetails() = flatMapLatest { organizations ->
            if (organizations.isEmpty()) {
                flowOf(emptyList())
            } else {
                val organizationsIds = organizations.map { it.uid }.toSet().toList()

                val subjectsMapFlow = database.listDocumentsFlow(
                    databaseId = Subjects.DATABASE_ID,
                    collectionId = Subjects.COLLECTION_ID,
                    queries = listOf(Query.equal(Subjects.ORGANIZATION_ID, organizationsIds)),
                    nestedType = SubjectPojo.serializer(),
                ).map { items ->
                    items.groupBy { it.organizationId }
                }

                val employeesMapFlow = database.listDocumentsFlow(
                    databaseId = Employee.DATABASE_ID,
                    collectionId = Employee.COLLECTION_ID,
                    queries = listOf(Query.equal(Employee.ORGANIZATION_ID, organizationsIds)),
                    nestedType = EmployeePojo.serializer(),
                ).map { items ->
                    items.groupBy { it.organizationId }
                }

                combine(
                    flowOf(organizations),
                    subjectsMapFlow,
                    employeesMapFlow,
                ) { organizationsList, subjectsMap, employeesMap ->
                    organizationsList.map { organization ->
                        organization.mapToDetails(
                            employee = employeesMap.getOrElse(organization.uid) { emptyList() },
                            subjects = subjectsMap.getOrElse(organization.uid) { emptyList() }.map { subject ->
                                val employee = employeesMap[organization.uid]?.find { it.uid == subject.teacherId }
                                subject.mapToDetails(employee = employee)
                            },
                        )
                    }
                }
            }
        }

        private fun Flow<OrganizationPojo?>.flatMapToDetails(): Flow<OrganizationDetailsPojo?> {
            return map { it?.let { listOf(it) } ?: emptyList() }
                .flatMapListToDetails()
                .map { it.getOrNull(0) }
        }
    }
}