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

package ru.aleshin.studyassistant.info.impl.presentation.ui.employee.store

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
import ru.aleshin.studyassistant.core.presentation.mappers.organizations.mapToUi
import ru.aleshin.studyassistant.info.impl.domain.interactors.EmployeesInteractor
import ru.aleshin.studyassistant.info.impl.domain.interactors.OrganizationsInteractor
import ru.aleshin.studyassistant.info.impl.presentation.mappers.mapToUi
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
        private val organizationsInteractor: OrganizationsInteractor,
    ) : EmployeeWorkProcessor {
        override suspend fun work(command: EmployeeWorkCommand) = when (command) {
            is EmployeeWorkCommand.LoadOrganizations -> loadOrganizationsWork(command.organization)
            is EmployeeWorkCommand.LoadEmployees -> loadEmployeesWork(command.organization)
            is EmployeeWorkCommand.SearchEmployees -> searchEmployeesWork(
                command.query,
                command.organization
            )

            is EmployeeWorkCommand.DeleteEmployee -> deleteEmployeeWork(command.targetId)
        }

        private fun loadOrganizationsWork(selectedOrganization: UID) = flow {
            organizationsInteractor.fetchAllShortOrganizations().collectAndHandle(
                onLeftAction = { emit(EffectResult(EmployeeEffect.ShowError(it))) },
                onRightAction = { organizationList ->
                    val organizations = organizationList.map { it.mapToUi() }
                    emit(
                        ActionResult(
                            EmployeeAction.UpdateOrganizations(
                                selectedOrganization,
                                organizations
                            )
                        )
                    )
                },
            )
        }

        private fun loadEmployeesWork(organization: UID) = flow<EmployeeWorkResult> {
            employeesInteractor.fetchEmployeesByOrganization(organization).collectAndHandle(
                onLeftAction = { emit(EffectResult(EmployeeEffect.ShowError(it))) },
                onRightAction = { groupedEmployees ->
                    val employees = groupedEmployees.mapValues { (_, employee) ->
                        employee.map { it.mapToUi() }
                    }
                    emit(ActionResult(EmployeeAction.UpdateEmployees(employees)))
                },
            )
        }.onStart {
            emit(ActionResult(EmployeeAction.UpdateLoading(true)))
        }

        private fun searchEmployeesWork(query: String, organization: UID) = flow {
            employeesInteractor.fetchEmployeesByOrganization(organization, query).collectAndHandle(
                onLeftAction = { emit(EffectResult(EmployeeEffect.ShowError(it))) },
                onRightAction = { groupedEmployees ->
                    val employees = groupedEmployees.mapValues { (_, employee) ->
                        employee.map { it.mapToUi() }
                    }
                    emit(ActionResult(EmployeeAction.UpdateEmployees(employees)))
                },
            )
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
