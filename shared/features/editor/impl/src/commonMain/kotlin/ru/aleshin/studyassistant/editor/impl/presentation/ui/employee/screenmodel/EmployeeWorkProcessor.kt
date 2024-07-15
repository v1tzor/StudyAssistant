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

import kotlinx.coroutines.flow.flow
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.ActionResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.EffectResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.FlowWorkProcessor
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkCommand
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.firstHandleAndGet
import ru.aleshin.studyassistant.core.common.functional.firstOrNullHandleAndGet
import ru.aleshin.studyassistant.core.common.functional.handle
import ru.aleshin.studyassistant.editor.impl.domain.interactors.EmployeeInteractor
import ru.aleshin.studyassistant.editor.impl.domain.interactors.OrganizationInteractor
import ru.aleshin.studyassistant.editor.impl.presentation.mappers.mapToDomain
import ru.aleshin.studyassistant.editor.impl.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.EditEmployeeUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.convertToBase
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.convertToEdit
import ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.contract.EmployeeAction
import ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.contract.EmployeeEffect

/**
 * @author Stanislav Aleshin on 06.06.2024.
 */
internal interface EmployeeWorkProcessor :
    FlowWorkProcessor<EmployeeWorkCommand, EmployeeAction, EmployeeEffect> {

    class Base(
        private val employeeInteractor: EmployeeInteractor,
        private val organizationInteractor: OrganizationInteractor,
    ) : EmployeeWorkProcessor {

        override suspend fun work(command: EmployeeWorkCommand) = when (command) {
            is EmployeeWorkCommand.LoadEditModel -> loadEditModelWork(command.employeeId, command.organizationId)
            is EmployeeWorkCommand.SaveEditModel -> saveEditModelWork(command.editableEmployee)
        }

        private fun loadEditModelWork(employeeId: UID?, organizationId: UID) = flow {
            val employee = employeeInteractor.fetchEmployeeById(employeeId ?: "").firstOrNullHandleAndGet(
                onLeftAction = { emit(EffectResult(EmployeeEffect.ShowError(it))).let { null } },
                onRightAction = { employee -> employee?.mapToUi() },
            )
            val organization = organizationInteractor.fetchShortOrganizationById(organizationId).firstHandleAndGet(
                onLeftAction = { error(it) },
                onRightAction = { organization -> organization.mapToUi() }
            )

            val editModel = employee?.convertToEdit() ?: EditEmployeeUi.createEditModel(
                uid = employeeId,
                organizationId = organizationId,
            )
            emit(ActionResult(EmployeeAction.SetupEditModel(editModel, organization)))
        }

        private fun saveEditModelWork(editableEmployee: EditEmployeeUi) = flow {
            val employee = editableEmployee.convertToBase().mapToDomain()
            employeeInteractor.addOrUpdateEmployee(employee).handle(
                onLeftAction = { emit(EffectResult(EmployeeEffect.ShowError(it))) },
                onRightAction = { emit(EffectResult(EmployeeEffect.NavigateToBack)) },
            )
        }
    }
}

internal sealed class EmployeeWorkCommand : WorkCommand {
    data class LoadEditModel(val employeeId: UID?, val organizationId: UID) : EmployeeWorkCommand()
    data class SaveEditModel(val editableEmployee: EditEmployeeUi) : EmployeeWorkCommand()
}