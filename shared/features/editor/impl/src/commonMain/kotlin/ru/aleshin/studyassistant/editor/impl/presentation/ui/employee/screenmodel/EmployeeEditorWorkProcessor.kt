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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.screenmodel

import architecture.screenmodel.work.EffectResult
import architecture.screenmodel.work.FlowWorkProcessor
import architecture.screenmodel.work.WorkCommand
import functional.UID
import functional.handle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import ru.aleshin.studyassistant.editor.impl.domain.interactors.EmployeeInteractor
import ru.aleshin.studyassistant.editor.impl.domain.interactors.OrganizationInteractor
import ru.aleshin.studyassistant.editor.impl.presentation.mappers.mapToDomain
import ru.aleshin.studyassistant.editor.impl.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.EditEmployeeUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.convertToBase
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.convertToEdit
import ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.contract.EmployeeEditorAction
import ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.contract.EmployeeEditorEffect

/**
 * @author Stanislav Aleshin on 06.06.2024.
 */
internal interface EmployeeEditorWorkProcessor :
    FlowWorkProcessor<EmployeeEditorWorkCommand, EmployeeEditorAction, EmployeeEditorEffect> {

    class Base(
        private val employeeInteractor: EmployeeInteractor,
        private val organizationInteractor: OrganizationInteractor,
    ) : EmployeeEditorWorkProcessor {

        override suspend fun work(command: EmployeeEditorWorkCommand) = when (command) {
            is EmployeeEditorWorkCommand.LoadEditModel -> loadEditModelWork(command.employeeId, command.organizationId)
            is EmployeeEditorWorkCommand.SaveEditModel -> saveEditModelWork(command.editableEmployee)
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        private fun loadEditModelWork(employeeId: UID?, organizationId: UID) = flow {
            val organizationFlow = organizationInteractor.fetchOrganizationById(organizationId)
            val employeeFlow = employeeInteractor.fetchEmployeeById(employeeId ?: "")

            employeeFlow.flatMapLatestWithResult(
                secondFlow = organizationFlow,
                onError = { EmployeeEditorEffect.ShowError(it) },
                onData = { employeeModel, organizationModel ->
                    val organization = organizationModel.mapToUi()
                    val employee = employeeModel?.mapToUi()
                    val editModel = employee?.convertToEdit() ?: EditEmployeeUi.createEditModel(
                        uid = employeeId,
                        organizationId = organizationId,
                    )
                    EmployeeEditorAction.SetupEditModel(editModel, organization)
                },
            ).collect { result ->
                emit(result)
            }
        }

        private fun saveEditModelWork(editableEmployee: EditEmployeeUi) = flow {
            val employee = editableEmployee.convertToBase().mapToDomain()
            employeeInteractor.addOrUpdateEmployee(employee).handle(
                onLeftAction = { emit(EffectResult(EmployeeEditorEffect.ShowError(it))) },
                onRightAction = { emit(EffectResult(EmployeeEditorEffect.NavigateToBack)) },
            )
        }
    }
}

internal sealed class EmployeeEditorWorkCommand : WorkCommand {
    data class LoadEditModel(val employeeId: UID?, val organizationId: UID) : EmployeeEditorWorkCommand()
    data class SaveEditModel(val editableEmployee: EditEmployeeUi) : EmployeeEditorWorkCommand()
}