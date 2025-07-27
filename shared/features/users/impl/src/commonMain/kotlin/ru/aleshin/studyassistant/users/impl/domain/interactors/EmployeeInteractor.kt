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

package ru.aleshin.studyassistant.users.impl.domain.interactors

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import ru.aleshin.studyassistant.core.common.functional.FlowDomainResult
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.employee.EmployeeDetails
import ru.aleshin.studyassistant.core.domain.entities.employee.convertToDetails
import ru.aleshin.studyassistant.core.domain.repositories.EmployeeRepository
import ru.aleshin.studyassistant.core.domain.repositories.SubjectsRepository
import ru.aleshin.studyassistant.users.impl.domain.common.UsersEitherWrapper
import ru.aleshin.studyassistant.users.impl.domain.entities.UsersFailures

/**
 * @author Stanislav Aleshin on 02.06.2024.
 */
internal interface EmployeeInteractor {

    suspend fun fetchEmployeeById(uid: UID): FlowDomainResult<UsersFailures, EmployeeDetails?>

    class Base(
        private val employeeRepository: EmployeeRepository,
        private val subjectsRepository: SubjectsRepository,
        private val eitherWrapper: UsersEitherWrapper,
    ) : EmployeeInteractor {

        @OptIn(ExperimentalCoroutinesApi::class)
        override suspend fun fetchEmployeeById(uid: UID) = eitherWrapper.wrapFlow {
            val employeeFlow = employeeRepository.fetchEmployeeById(uid)

            return@wrapFlow employeeFlow.flatMapLatest { employee ->
                if (employee == null) return@flatMapLatest flowOf(null)
                subjectsRepository.fetchSubjectsByEmployee(employee.uid).map { subjects ->
                    employee.convertToDetails(subjects = subjects)
                }
            }
        }
    }
}