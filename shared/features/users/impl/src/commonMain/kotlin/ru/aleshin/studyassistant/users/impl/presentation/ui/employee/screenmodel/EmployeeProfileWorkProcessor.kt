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

package ru.aleshin.studyassistant.users.impl.presentation.ui.employee.screenmodel

import kotlinx.coroutines.flow.flow
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.ActionResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.EffectResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.FlowWorkProcessor
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkCommand
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.collectAndHandle
import ru.aleshin.studyassistant.users.impl.domain.interactors.EmployeeInteractor
import ru.aleshin.studyassistant.users.impl.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.users.impl.presentation.ui.employee.contract.EmployeeProfileAction
import ru.aleshin.studyassistant.users.impl.presentation.ui.employee.contract.EmployeeProfileEffect

/**
 * @author Stanislav Aleshin on 10.07.2024.
 */
internal interface EmployeeProfileWorkProcessor :
    FlowWorkProcessor<EmployeeProfileWorkCommand, EmployeeProfileAction, EmployeeProfileEffect> {

    class Base(
        private val employeeInteractor: EmployeeInteractor,
    ) : EmployeeProfileWorkProcessor {
        override suspend fun work(command: EmployeeProfileWorkCommand) = when (command) {
            is EmployeeProfileWorkCommand.LoadEmployee -> loadEmployeeWork(command.employeeId)
        }

        private fun loadEmployeeWork(employeeId: UID) = flow {
            employeeInteractor.fetchEmployeeById(employeeId).collectAndHandle(
                onLeftAction = { emit(EffectResult(EmployeeProfileEffect.ShowError(it))) },
                onRightAction = { employee ->
                    emit(ActionResult(EmployeeProfileAction.UpdateEmployee(employee?.mapToUi())))
                },
            )
        }
    }
}

internal sealed class EmployeeProfileWorkCommand : WorkCommand {
    data class LoadEmployee(val employeeId: UID) : EmployeeProfileWorkCommand()
}