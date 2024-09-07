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

import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.storage.File
import dev.gitlive.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.serialization.serializer
import ru.aleshin.studyassistant.core.common.exceptions.FirebaseUserException
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.extensions.snapshotGet
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.remote.datasources.StudyAssistantFirebase.Storage
import ru.aleshin.studyassistant.core.remote.datasources.StudyAssistantFirebase.UserData
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
    suspend fun uploadAvatar(uid: UID, file: File, targetUser: UID): String
    suspend fun fetchOrganizationById(uid: UID, targetUser: UID): Flow<OrganizationDetailsPojo?>
    suspend fun fetchOrganizationsById(uid: List<UID>, targetUser: UID): Flow<List<OrganizationDetailsPojo>>
    suspend fun fetchShortOrganizationById(uid: UID, targetUser: UID): Flow<OrganizationShortPojo?>
    suspend fun fetchAllOrganization(targetUser: UID, showHide: Boolean = false): Flow<List<OrganizationDetailsPojo>>
    suspend fun fetchAllShortOrganization(targetUser: UID): Flow<List<OrganizationShortPojo>>
    suspend fun deleteAllOrganizations(targetUser: UID)
    suspend fun deleteAvatar(uid: UID, targetUser: UID)

    class Base(
        private val database: FirebaseFirestore,
        private val storage: FirebaseStorage
    ) : OrganizationsRemoteDataSource {

        override suspend fun addOrUpdateOrganization(organization: OrganizationPojo, targetUser: UID): UID {
            if (targetUser.isEmpty()) throw FirebaseUserException()
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
            if (targetUser.isEmpty()) throw FirebaseUserException()
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

        override suspend fun uploadAvatar(uid: UID, file: File, targetUser: UID): String {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val storageRoot = storage.reference.child(targetUser).child(Storage.ORGANIZATIONS).child(uid)

            val avatarReference = storageRoot.child(Storage.ORGANIZATION_AVATAR).child(Storage.ORGANIZATION_AVATAR_FILE)
            avatarReference.putFile(file)

            return avatarReference.getDownloadUrl()
        }

        override suspend fun fetchOrganizationById(uid: UID, targetUser: UID): Flow<OrganizationDetailsPojo?> {
            if (uid.isEmpty()) return flowOf(null)
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.ORGANIZATIONS).document(uid)

            val organizationPojoFlow = reference.snapshots.map { snapshot ->
                snapshot.data(serializer<OrganizationPojo?>())
            }

            return organizationPojoFlow.map { organizationPojo ->
                if (organizationPojo == null) return@map null
                val employeeReference = userDataRoot.collection(UserData.EMPLOYEE).where {
                    UserData.ORGANIZATION_ID equalTo organizationPojo.uid
                }
                val subjectsReference = userDataRoot.collection(UserData.SUBJECTS).where {
                    UserData.ORGANIZATION_ID equalTo organizationPojo.uid
                }

                val employeeList = employeeReference.snapshotGet().map { snapshot ->
                    snapshot.data(serializer<EmployeePojo?>())
                }
                val subjectList = subjectsReference.snapshotGet().map { snapshot ->
                    snapshot.data(serializer<SubjectPojo?>())
                }.map { subjectPojo ->
                    subjectPojo?.mapToDetails(employeeList.find { it?.uid == subjectPojo.teacherId })
                }

                organizationPojo.mapToDetails(
                    subjects = subjectList.filterNotNull(),
                    employee = employeeList.filterNotNull(),
                )
            }
        }

        override suspend fun fetchOrganizationsById(
            uid: List<UID>,
            targetUser: UID,
        ): Flow<List<OrganizationDetailsPojo>> {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.ORGANIZATIONS).where {
                UserData.UID inArray uid
            }

            val organizationPojoListFlow = reference.snapshots.map { snapshot ->
                snapshot.documents.map { it.data(serializer<OrganizationPojo>()) }
            }

            return organizationPojoListFlow.map { organizations ->
                organizations.map { organizationPojo ->
                    val employeeReference = userDataRoot.collection(UserData.EMPLOYEE).where {
                        UserData.ORGANIZATION_ID equalTo organizationPojo.uid
                    }
                    val subjectsReference = userDataRoot.collection(UserData.SUBJECTS).where {
                        UserData.ORGANIZATION_ID equalTo organizationPojo.uid
                    }

                    val employeeList = employeeReference.snapshotGet().map { snapshot ->
                        snapshot.data(serializer<EmployeePojo?>())
                    }
                    val subjectList = subjectsReference.snapshotGet().map { snapshot ->
                        snapshot.data(serializer<SubjectPojo?>())
                    }.map { subjectPojo ->
                        subjectPojo?.mapToDetails(employeeList.find { it?.uid == subjectPojo.teacherId })
                    }

                    organizationPojo.mapToDetails(
                        employee = employeeList.filterNotNull(),
                        subjects = subjectList.filterNotNull(),
                    )
                }
            }
        }

        override suspend fun fetchShortOrganizationById(uid: UID, targetUser: UID): Flow<OrganizationShortPojo?> {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.ORGANIZATIONS).document(uid)

            return reference.snapshots.map { snapshot ->
                snapshot.data(serializer<OrganizationShortPojo?>())
            }
        }

        override suspend fun fetchAllOrganization(
            targetUser: UID,
            showHide: Boolean,
        ): Flow<List<OrganizationDetailsPojo>> {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = if (showHide) {
                userDataRoot.collection(UserData.ORGANIZATIONS)
            } else {
                userDataRoot.collection(UserData.ORGANIZATIONS).where {
                    UserData.ORGANIZATION_HIDE equalTo false
                }
            }

            val organizationPojoListFlow = reference.snapshots.map { snapshot ->
                snapshot.documents.map { it.data(serializer<OrganizationPojo>()) }
            }

            return organizationPojoListFlow.map { organizations ->
                organizations.map { organizationPojo ->
                    val employeeReference = userDataRoot.collection(UserData.EMPLOYEE).where {
                        UserData.ORGANIZATION_ID equalTo organizationPojo.uid
                    }
                    val subjectsReference = userDataRoot.collection(UserData.SUBJECTS).where {
                        UserData.ORGANIZATION_ID equalTo organizationPojo.uid
                    }

                    val employeeList = employeeReference.snapshotGet().map { snapshot ->
                        snapshot.data(serializer<EmployeePojo?>())
                    }
                    val subjectList = subjectsReference.snapshotGet().map { snapshot ->
                        snapshot.data(serializer<SubjectPojo?>())
                    }.map { subjectPojo ->
                        subjectPojo?.mapToDetails(employeeList.find { it?.uid == subjectPojo.teacherId })
                    }

                    organizationPojo.mapToDetails(
                        employee = employeeList.filterNotNull(),
                        subjects = subjectList.filterNotNull(),
                    )
                }
            }
        }

        override suspend fun fetchAllShortOrganization(targetUser: UID): Flow<List<OrganizationShortPojo>> {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.ORGANIZATIONS).where {
                UserData.ORGANIZATION_HIDE equalTo false
            }

            return reference.snapshots.map { snapshot ->
                snapshot.documents.map { it.data(serializer<OrganizationShortPojo>()) }
            }
        }

        override suspend fun deleteAllOrganizations(targetUser: UID) {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.ORGANIZATIONS)

            val deletableOrganizationReferences = reference.snapshotGet().map { snapshot ->
                snapshot.reference
            }

            database.batch().apply {
                deletableOrganizationReferences.forEach { organizationReference ->
                    delete(organizationReference)
                }
                return@apply commit()
            }
        }

        override suspend fun deleteAvatar(uid: UID, targetUser: UID) {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val storageRoot = storage.reference.child(targetUser).child(Storage.ORGANIZATIONS).child(uid)

            val avatarReference = storageRoot.child(Storage.ORGANIZATION_AVATAR).child(Storage.ORGANIZATION_AVATAR_FILE)
            avatarReference.delete()
        }
    }
}