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

import dev.gitlive.firebase.firestore.DocumentReference
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import ru.aleshin.studyassistant.core.common.exceptions.AppwriteUserException
import ru.aleshin.studyassistant.core.common.extensions.deleteAll
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.extensions.snapshotFlowGet
import ru.aleshin.studyassistant.core.common.extensions.snapshotListFlowGet
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.files.InputFile
import ru.aleshin.studyassistant.core.remote.appwrite.storage.AppwriteStorage
import ru.aleshin.studyassistant.core.remote.appwrite.utils.Permission
import ru.aleshin.studyassistant.core.remote.datasources.StudyAssistantAppwrite.Storage.BUCKET
import ru.aleshin.studyassistant.core.remote.datasources.StudyAssistantAppwrite.UserData
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
    suspend fun uploadAvatar(uid: UID, file: InputFile, targetUser: UID): String
    suspend fun fetchOrganizationById(uid: UID, targetUser: UID): Flow<OrganizationDetailsPojo?>
    suspend fun fetchOrganizationsById(uid: List<UID>, targetUser: UID): Flow<List<OrganizationDetailsPojo>>
    suspend fun fetchShortOrganizationById(uid: UID, targetUser: UID): Flow<OrganizationShortPojo?>
    suspend fun fetchAllOrganization(targetUser: UID, showHide: Boolean = false): Flow<List<OrganizationDetailsPojo>>
    suspend fun fetchAllShortOrganization(targetUser: UID): Flow<List<OrganizationShortPojo>>
    suspend fun deleteAllOrganizations(targetUser: UID)
    suspend fun deleteAvatar(uid: UID, targetUser: UID)

    class Base(
        private val database: FirebaseFirestore,
        private val storage: AppwriteStorage,
    ) : OrganizationsRemoteDataSource {

        override suspend fun addOrUpdateOrganization(
            organization: OrganizationPojo,
            targetUser: UID
        ): UID {
            if (targetUser.isEmpty()) throw AppwriteUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.ORGANIZATIONS)

            val organizationId = organization.uid.takeIf { it.isNotBlank() } ?: randomUUID()

            return reference.document(organizationId).set(organization.copy(uid = organizationId)).let {
                return@let organizationId
            }
        }

        override suspend fun addOrUpdateOrganizationsGroup(
            organizations: List<OrganizationPojo>,
            targetUser: UID
        ) {
            if (targetUser.isEmpty()) throw AppwriteUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.ORGANIZATIONS)

            database.batch().apply {
                organizations.forEach { organization ->
                    val uid = organization.uid.takeIf { it.isNotBlank() } ?: randomUUID()
                    set(reference.document(uid), organization.copy(uid = uid))
                }
                return@apply commit()
            }
        }

        override suspend fun uploadAvatar(uid: UID, file: InputFile, targetUser: UID): String {
            if (targetUser.isEmpty()) throw AppwriteUserException()

            val file = storage.createFile(
                bucketId = BUCKET,
                fileId = randomUUID(),
                fileBytes = file.fileBytes,
                filename = file.filename,
                mimeType = file.mimeType,
                permissions = Permission.onlyUsersVisibleData(targetUser),
            )

            return file.getDownloadUrl()
        }

        override suspend fun fetchOrganizationById(
            uid: UID,
            targetUser: UID
        ): Flow<OrganizationDetailsPojo?> {
            if (uid.isEmpty()) return flowOf(null)
            if (targetUser.isEmpty()) throw AppwriteUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val organizationFlow = userDataRoot.collection(UserData.ORGANIZATIONS).document(uid)
                .snapshotFlowGet<OrganizationPojo>()
                .flatMapToDetails(userDataRoot)

            return organizationFlow
        }

        override suspend fun fetchOrganizationsById(
            uid: List<UID>,
            targetUser: UID,
        ): Flow<List<OrganizationDetailsPojo>> {
            if (targetUser.isEmpty()) throw AppwriteUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.ORGANIZATIONS).where {
                UserData.UID inArray uid
            }

            return reference.snapshotListFlowGet<OrganizationPojo>().flatMapListToDetails(userDataRoot)
        }

        override suspend fun fetchShortOrganizationById(
            uid: UID,
            targetUser: UID
        ): Flow<OrganizationShortPojo?> {
            if (targetUser.isEmpty()) throw AppwriteUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            return userDataRoot.collection(UserData.ORGANIZATIONS).document(uid).snapshotFlowGet()
        }

        override suspend fun fetchAllOrganization(
            targetUser: UID,
            showHide: Boolean,
        ): Flow<List<OrganizationDetailsPojo>> {
            if (targetUser.isEmpty()) throw AppwriteUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = if (showHide) {
                userDataRoot.collection(UserData.ORGANIZATIONS)
            } else {
                userDataRoot.collection(UserData.ORGANIZATIONS).where {
                    UserData.ORGANIZATION_HIDE equalTo false
                }
            }

            return reference.snapshotListFlowGet<OrganizationPojo>().flatMapListToDetails(userDataRoot)
        }

        override suspend fun fetchAllShortOrganization(targetUser: UID): Flow<List<OrganizationShortPojo>> {
            if (targetUser.isEmpty()) throw AppwriteUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            return userDataRoot
                .collection(UserData.ORGANIZATIONS)
                .where { UserData.ORGANIZATION_HIDE equalTo false }
                .snapshotListFlowGet<OrganizationShortPojo>()
        }

        override suspend fun deleteAllOrganizations(targetUser: UID) {
            if (targetUser.isEmpty()) throw AppwriteUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.ORGANIZATIONS)

            database.deleteAll(reference)
        }

        override suspend fun deleteAvatar(uid: UID, targetUser: UID) {
            if (targetUser.isEmpty()) throw AppwriteUserException()
            storage.deleteFile(BUCKET, uid)
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        private fun Flow<List<OrganizationPojo>>.flatMapListToDetails(
            userDataRoot: DocumentReference
        ): Flow<List<OrganizationDetailsPojo>> = flatMapLatest { organizations ->
            if (organizations.isEmpty()) {
                flowOf(emptyList())
            } else {
                val organizationsIds = organizations.map { it.uid }.toSet()

                val employeesMapFlow = userDataRoot
                    .collection(UserData.EMPLOYEE)
                    .where { UserData.ORGANIZATION_ID inArray organizationsIds.toList() }
                    .snapshotListFlowGet<EmployeePojo>()
                    .map { items -> items.groupBy { it.organizationId } }

                val subjectsMapFlow = userDataRoot
                    .collection(UserData.SUBJECTS)
                    .where { UserData.ORGANIZATION_ID inArray organizationsIds.toList() }
                    .snapshotListFlowGet<SubjectPojo>()
                    .map { items -> items.groupBy { it.organizationId } }

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

        private fun Flow<OrganizationPojo?>.flatMapToDetails(
            userDataRoot: DocumentReference
        ): Flow<OrganizationDetailsPojo?> {
            return map { it?.let { listOf(it) } ?: emptyList() }
                .flatMapListToDetails(userDataRoot)
                .map { it.getOrNull(0) }
        }
    }
}