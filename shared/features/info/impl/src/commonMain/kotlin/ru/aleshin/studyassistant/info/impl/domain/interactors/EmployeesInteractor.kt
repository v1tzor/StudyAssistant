/*
 * Copyright 2026 Stanislav Aleshin
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

package ru.aleshin.studyassistant.info.impl.domain.interactors

import kotlinx.coroutines.flow.combine
import ru.aleshin.studyassistant.core.common.functional.FlowDomainResult
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.domain.repositories.EmployeeRepository
import ru.aleshin.studyassistant.core.domain.repositories.SubjectsRepository
import ru.aleshin.studyassistant.info.impl.domain.common.InfoEitherWrapper
import ru.aleshin.studyassistant.info.impl.domain.entities.EmployeeAndSubjects
import ru.aleshin.studyassistant.info.impl.domain.entities.InfoFailures

/**
 * @author Stanislav Aleshin on 17.06.2024.
 */
internal interface EmployeesInteractor {

    suspend fun fetchEmployeesByOrganization(
        organizationId: UID,
        query: String = "",
    ): FlowDomainResult<InfoFailures, Map<Char, List<EmployeeAndSubjects>>>
    suspend fun deleteEmployeeById(targetId: UID): UnitDomainResult<InfoFailures>

    class Base(
        private val employeeRepository: EmployeeRepository,
        private val subjectsRepository: SubjectsRepository,
        private val eitherWrapper: InfoEitherWrapper,
    ) : EmployeesInteractor {

        override suspend fun fetchEmployeesByOrganization(organizationId: UID, query: String) =
            eitherWrapper.wrapFlow {
                employeeRepository.fetchAllEmployeeByOrganization(organizationId).combine(
                    subjectsRepository.fetchAllSubjectsByOrganization(organizationId)
                ) { employees, subjects ->
                    employees.map { employee ->
                        EmployeeAndSubjects(
                            employee = employee,
                            subjects = subjects.filter { it.teacher?.uid == employee.uid },
                        )
                    }.filter { employee ->
                        query.isBlank() || employee.employee.firstName.contains(query, true) ||
                            employee.employee.secondName?.contains(query, true) == true ||
                            employee.employee.patronymic?.contains(query, true) == true
                    }.sortedBy { it.employee.firstName }.groupBy {
                        it.employee.firstName.firstOrNull() ?: '#'
                    }
                }
            }

        override suspend fun deleteEmployeeById(targetId: UID) = eitherWrapper.wrap {
            employeeRepository.deleteEmployee(targetId)
        }
    }
}
