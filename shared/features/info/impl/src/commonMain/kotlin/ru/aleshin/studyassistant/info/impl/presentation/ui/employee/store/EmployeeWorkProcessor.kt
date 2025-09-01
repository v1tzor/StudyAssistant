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

package ru.aleshin.studyassistant.info.impl.presentation.ui.employee.store

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import ru.aleshin.studyassistant.core.common.architecture.store.work.ActionResult
import ru.aleshin.studyassistant.core.common.architecture.store.work.EffectResult
import ru.aleshin.studyassistant.core.common.architecture.store.work.FlowWorkProcessor
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkCommand
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkResult
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.collectAndHandle
import ru.aleshin.studyassistant.core.common.functional.handle
import ru.aleshin.studyassistant.info.impl.domain.interactors.EmployeesInteractor
import ru.aleshin.studyassistant.info.impl.domain.interactors.OrganizationsInteractor
import ru.aleshin.studyassistant.info.impl.domain.interactors.SubjectsInteractor
import ru.aleshin.studyassistant.info.impl.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.info.impl.presentation.models.users.convertWithSubjects
import ru.aleshin.studyassistant.info.impl.presentation.ui.employee.contract.EmployeeAction
import ru.aleshin.studyassistant.info.impl.presentation.ui.employee.contract.EmployeeEffect
import ru.aleshin.studyassistant.info.impl.presentation.ui.employee.contract.EmployeeOutput

/**
 * @author Stanislav Aleshin on 17.06.2024.
 */
internal interface EmployeeWorkProcessor :
    FlowWorkProcessor<EmployeeWorkCommand, EmployeeAction, EmployeeEffect, EmployeeOutput> {

    class Base(
        private val employeesInteractor: EmployeesInteractor,
        private val subjectsInteractor: SubjectsInteractor,
        private val organizationsInteractor: OrganizationsInteractor,
    ) : EmployeeWorkProcessor {
        override suspend fun work(command: EmployeeWorkCommand) = when (command) {
            is EmployeeWorkCommand.LoadOrganizations -> loadOrganizationsWork(command.organization)
            is EmployeeWorkCommand.LoadEmployees -> loadEmployeesWork(command.organization)
            is EmployeeWorkCommand.SearchEmployees -> searchEmployeesWork(command.query, command.organization)
            is EmployeeWorkCommand.DeleteEmployee -> deleteEmployeeWork(command.targetId)
        }

        private fun loadOrganizationsWork(selectedOrganization: UID) = flow {
            organizationsInteractor.fetchAllShortOrganizations().collectAndHandle(
                onLeftAction = { emit(EffectResult(EmployeeEffect.ShowError(it))) },
                onRightAction = { organizationList ->
                    val organizations = organizationList.map { it.mapToUi() }
                    emit(ActionResult(EmployeeAction.UpdateOrganizations(selectedOrganization, organizations)))
                },
            )
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        private fun loadEmployeesWork(organization: UID) = flow {
            val subjectsFlow = subjectsInteractor.fetchSubjectsByOrganization(organization)
            val employeesFlow = employeesInteractor.fetchEmployeesByOrganization(organization)

            employeesFlow.combineWithResult(
                secondFlow = subjectsFlow,
                onError = { EmployeeEffect.ShowError(it) },
                onData = { employeeList, subjectsList ->
                    val subjects = subjectsList.map { it.mapToUi() }
                    val employees = employeeList.map { employee ->
                        val foundedSubjects = subjects.filter { it.teacher?.uid == employee.uid }
                        employee.mapToUi().convertWithSubjects(foundedSubjects)
                    }
                    val sortedEmployees = employees.sortedBy { it.data.firstName }
                    val groupedEmployees = sortedEmployees.groupBy { it.data.firstName.first() }
                    EmployeeAction.UpdateEmployees(groupedEmployees)
                },
            ).collect { workResult ->
                emit(workResult)
            }
        }.onStart {
            emit(ActionResult(EmployeeAction.UpdateLoading(true)))
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        private fun searchEmployeesWork(query: String, organization: UID) = flow {
            val subjectsFlow = subjectsInteractor.fetchSubjectsByOrganization(organization)
            val employeesFlow = employeesInteractor.fetchEmployeesByOrganization(organization)

            employeesFlow.combineWithResult(
                secondFlow = subjectsFlow,
                onError = { EmployeeEffect.ShowError(it) },
                onData = { employeeList, subjectsList ->
                    val subjects = subjectsList.map { it.mapToUi() }
                    val employees = employeeList.map { employee ->
                        val foundedSubjects = subjects.filter { it.teacher?.uid == employee.uid }
                        employee.mapToUi().convertWithSubjects(foundedSubjects)
                    }
                    val searchedEmployees = employees.filter { employee ->
                        if (query.isNotBlank()) {
                            val firstNameFilter = employee.data.firstName.contains(query, true)
                            val secondNameFilter = employee.data.secondName?.contains(query, true) ?: false
                            val patronymicFilter = employee.data.patronymic?.contains(query, true) ?: false
                            firstNameFilter or secondNameFilter or patronymicFilter
                        } else {
                            true
                        }
                    }
                    val sortedEmployees = searchedEmployees.sortedBy { it.data.firstName }
                    val groupedEmployees = sortedEmployees.groupBy { it.data.firstName.first() }
                    EmployeeAction.UpdateEmployees(groupedEmployees)
                },
            ).collect { workResult ->
                emit(workResult)
            }
        }

        private fun deleteEmployeeWork(targetId: UID) = flow {
            employeesInteractor.deleteEmployeeById(targetId).handle(
                onLeftAction = { emit(EffectResult(EmployeeEffect.ShowError(it))) },
            )
        }
    }
}

internal sealed class EmployeeWorkCommand : WorkCommand {
    data class LoadOrganizations(val organization: UID) : EmployeeWorkCommand()
    data class LoadEmployees(val organization: UID) : EmployeeWorkCommand()
    data class SearchEmployees(val query: String, val organization: UID) : EmployeeWorkCommand()
    data class DeleteEmployee(val targetId: UID) : EmployeeWorkCommand()
}

internal typealias EmployeeWorkResult = WorkResult<EmployeeAction, EmployeeEffect, EmployeeOutput>