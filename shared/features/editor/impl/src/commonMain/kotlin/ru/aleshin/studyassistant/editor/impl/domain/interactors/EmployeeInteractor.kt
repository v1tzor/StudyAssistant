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

import entities.employee.Employee
import functional.DomainResult
import functional.FlowDomainResult
import functional.UID
import repositories.EmployeeRepository
import repositories.UsersRepository
import ru.aleshin.studyassistant.editor.impl.domain.common.EditorEitherWrapper
import ru.aleshin.studyassistant.editor.impl.domain.entities.EditorFailures

/**
 * @author Stanislav Aleshin on 02.06.2024.
 */
internal interface EmployeeInteractor {

    suspend fun addOrUpdateEmployee(employee: Employee): DomainResult<EditorFailures, UID>
    suspend fun fetchAllEmployeeByOrganization(organizationId: UID): FlowDomainResult<EditorFailures, List<Employee>>
    suspend fun fetchEmployeeById(uid: UID): FlowDomainResult<EditorFailures, Employee?>

    class Base(
        private val employeeRepository: EmployeeRepository,
        private val usersRepository: UsersRepository,
        private val eitherWrapper: EditorEitherWrapper,
    ) : EmployeeInteractor {

        private val targetUser: UID
            get() = usersRepository.fetchCurrentUserOrError().uid

        override suspend fun addOrUpdateEmployee(employee: Employee) = eitherWrapper.wrap {
            employeeRepository.addOrUpdateEmployee(employee, targetUser)
        }

        override suspend fun fetchAllEmployeeByOrganization(organizationId: UID) = eitherWrapper.wrapFlow {
            employeeRepository.fetchAllEmployeeByOrganization(organizationId, targetUser)
        }

        override suspend fun fetchEmployeeById(uid: UID) = eitherWrapper.wrapFlow {
            employeeRepository.fetchEmployeeById(uid, targetUser)
        }
    }
}
