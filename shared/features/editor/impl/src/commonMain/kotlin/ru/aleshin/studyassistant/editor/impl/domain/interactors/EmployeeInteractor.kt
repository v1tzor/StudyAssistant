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

package ru.aleshin.studyassistant.editor.impl.domain.interactors

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import ru.aleshin.studyassistant.core.common.functional.DomainResult
import ru.aleshin.studyassistant.core.common.functional.FlowDomainResult
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.domain.entities.employee.Employee
import ru.aleshin.studyassistant.core.domain.entities.employee.EmployeeDetails
import ru.aleshin.studyassistant.core.domain.entities.employee.convertToDetails
import ru.aleshin.studyassistant.core.domain.entities.files.InputFile
import ru.aleshin.studyassistant.core.domain.repositories.EmployeeRepository
import ru.aleshin.studyassistant.core.domain.repositories.SubjectsRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.editor.impl.domain.common.EditorEitherWrapper
import ru.aleshin.studyassistant.editor.impl.domain.entities.EditorFailures

/**
 * @author Stanislav Aleshin on 02.06.2024.
 */
internal interface EmployeeInteractor {

    suspend fun addOrUpdateEmployee(employee: Employee): DomainResult<EditorFailures, UID>
    suspend fun uploadAvatar(uid: UID, file: InputFile): DomainResult<EditorFailures, String>
    suspend fun fetchAllDetailsEmployee(organizationId: UID): FlowDomainResult<EditorFailures, List<EmployeeDetails>>
    suspend fun fetchEmployeeById(uid: UID): FlowDomainResult<EditorFailures, Employee?>
    suspend fun deleteAvatar(uid: UID): UnitDomainResult<EditorFailures>

    class Base(
        private val employeeRepository: EmployeeRepository,
        private val subjectsRepository: SubjectsRepository,
        private val usersRepository: UsersRepository,
        private val eitherWrapper: EditorEitherWrapper,
    ) : EmployeeInteractor {

        private val targetUser: UID
            get() = usersRepository.fetchCurrentUserOrError().uid

        override suspend fun addOrUpdateEmployee(employee: Employee) = eitherWrapper.wrap {
            employeeRepository.addOrUpdateEmployee(employee, targetUser)
        }

        override suspend fun uploadAvatar(uid: UID, file: InputFile) = eitherWrapper.wrap {
            employeeRepository.uploadAvatar(uid, file, targetUser)
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        override suspend fun fetchAllDetailsEmployee(organizationId: UID) = eitherWrapper.wrapFlow {
            val subjectsFlow = subjectsRepository.fetchAllSubjectsByOrganization(organizationId, targetUser)
            val employeesFlow = employeeRepository.fetchAllEmployeeByOrganization(organizationId, targetUser)

            return@wrapFlow employeesFlow.flatMapLatest { employeeList ->
                subjectsFlow.map { subjects ->
                    val groupedSubjectsByTeacher = subjects.groupBy { it.teacher?.uid }
                    employeeList.sortedBy { employee -> employee.firstName }.map { employee ->
                        val employeeSubjects = groupedSubjectsByTeacher[employee.uid] ?: emptyList()
                        employee.convertToDetails(subjects = employeeSubjects)
                    }
                }
            }
        }

        override suspend fun fetchEmployeeById(uid: UID) = eitherWrapper.wrapFlow {
            employeeRepository.fetchEmployeeById(uid, targetUser)
        }

        override suspend fun deleteAvatar(uid: UID) = eitherWrapper.wrap {
            employeeRepository.deleteAvatar(uid, targetUser)
        }
    }
}