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

package remote.organizations

import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.where
import exceptions.FirebaseUserException
import extensions.exists
import extensions.snapshotGet
import functional.UID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.serializer
import mappers.organizations.mapToDetailsData
import mappers.organizations.mapToRemoteData
import mappers.subjects.mapToDetailsData
import models.organizations.OrganizationDetailsData
import models.organizations.OrganizationPojo
import models.subjects.SubjectPojo
import models.users.EmployeeDetailsData
import remote.StudyAssistantFirestore.UserData

/**
 * @author Stanislav Aleshin on 29.04.2024.
 */
interface OrganizationsRemoteDataSource {

    suspend fun addOrUpdateOrganization(organization: OrganizationDetailsData, targetUser: UID): UID
    suspend fun fetchOrganizationById(uid: UID, targetUser: UID): Flow<OrganizationDetailsData>
    suspend fun fetchAllOrganization(targetUser: UID): Flow<List<OrganizationDetailsData>>

    class Base(
        private val database: FirebaseFirestore,
    ) : OrganizationsRemoteDataSource {

        override suspend fun addOrUpdateOrganization(organization: OrganizationDetailsData, targetUser: UID): UID {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)
            val organizationPojo = organization.mapToRemoteData()

            val reference = userDataRoot.collection(UserData.ORGANIZATIONS)

            return database.runTransaction {
                val isExist = organizationPojo.uid.isNotEmpty() && reference.document(organizationPojo.uid).exists()
                if (isExist) {
                    reference.document(organizationPojo.uid).set(organizationPojo)
                    return@runTransaction organizationPojo.uid
                } else {
                    val uid = reference.add(organizationPojo).id
                    reference.document(uid).update(UserData.UID to uid)
                    return@runTransaction uid
                }
            }
        }

        override suspend fun fetchOrganizationById(uid: UID, targetUser: UID): Flow<OrganizationDetailsData> {
            require(uid.isNotEmpty())
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.ORGANIZATIONS).document(uid)

            val organizationPojoFlow = reference.snapshots.map { snapshot ->
                snapshot.data(serializer<OrganizationPojo>())
            }

            return organizationPojoFlow.map { organizationPojo ->
                val employeeReference = userDataRoot.collection(UserData.EMPLOYEE).where {
                    UserData.ORGANIZATION_ID equalTo organizationPojo.uid
                }
                val subjectsReference = userDataRoot.collection(UserData.SUBJECTS).where {
                    UserData.ORGANIZATION_ID equalTo organizationPojo.uid
                }

                val employeeList = employeeReference.snapshotGet().map { it.data<EmployeeDetailsData>() }
                val subjectList = subjectsReference.snapshotGet().map { it.data<SubjectPojo>() }.map { subjectPojo ->
                    subjectPojo.mapToDetailsData(employeeList.find { it.uid == subjectPojo.teacherId })
                }

                organizationPojo.mapToDetailsData(
                    subjects = subjectList,
                    employee = employeeList,
                )
            }
        }

        override suspend fun fetchAllOrganization(targetUser: UID): Flow<List<OrganizationDetailsData>> {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.ORGANIZATIONS)

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

                    val employeeList = employeeReference.snapshotGet().map { it.data<EmployeeDetailsData>() }
                    val subjectList = subjectsReference.snapshotGet().map { it.data<SubjectPojo>() }.map { subjectPojo ->
                        subjectPojo.mapToDetailsData(employeeList.find { it.uid == subjectPojo.teacherId })
                    }

                    organizationPojo.mapToDetailsData(
                        employee = employeeList,
                        subjects = subjectList,
                    )
                }
            }
        }
    }
}