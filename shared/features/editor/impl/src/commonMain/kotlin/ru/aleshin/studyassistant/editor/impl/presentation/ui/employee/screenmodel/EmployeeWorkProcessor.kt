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
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.firstHandleAndGet
import ru.aleshin.studyassistant.core.common.functional.firstOrNullHandleAndGet
import ru.aleshin.studyassistant.core.common.functional.handle
import ru.aleshin.studyassistant.core.common.functional.handleAndGet
import ru.aleshin.studyassistant.core.ui.mappers.mapToDomain
import ru.aleshin.studyassistant.core.ui.models.ActionWithAvatar
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
            is EmployeeWorkCommand.LoadOrganization -> loadOrganizationWork(command.organizationId)
            is EmployeeWorkCommand.SaveEditModel -> saveEditModelWork(command.editModel, command.actionWithAvatar)
        }

        private fun loadEditModelWork(employeeId: UID?, organizationId: UID) = flow {
            val employee = employeeInteractor.fetchEmployeeById(employeeId ?: "").firstOrNullHandleAndGet(
                onLeftAction = { emit(EffectResult(EmployeeEffect.ShowError(it))).let { null } },
                onRightAction = { employee -> employee?.mapToUi() },
            )
            val editModel = employee?.convertToEdit() ?: EditEmployeeUi.createEditModel(
                uid = employeeId,
                organizationId = organizationId,
            )
            emit(ActionResult(EmployeeAction.SetupEditModel(editModel)))
        }

        private fun loadOrganizationWork(organizationId: UID) = flow {
            organizationInteractor.fetchShortOrganizationById(organizationId).firstHandleAndGet(
                onLeftAction = { error(it) },
                onRightAction = { organization ->
                    emit(ActionResult(EmployeeAction.UpdateOrganization(organization.mapToUi())))
                }
            )
        }

        private fun saveEditModelWork(
            editModel: EditEmployeeUi,
            actionWithAvatar: ActionWithAvatar,
        ) = flow {
            val uid = editModel.uid.takeIf { it.isNotBlank() } ?: randomUUID()

            val avatar = when (actionWithAvatar) {
                is ActionWithAvatar.Set -> {
                    employeeInteractor.uploadAvatar(editModel.avatar, actionWithAvatar.file.mapToDomain()).handleAndGet(
                        onLeftAction = { emit(EffectResult(EmployeeEffect.ShowError(it))).let { null } },
                        onRightAction = { it },
                    )
                }
                is ActionWithAvatar.Delete -> {
                    employeeInteractor.deleteAvatar(editModel.avatar ?: "").handleAndGet(
                        onLeftAction = { emit(EffectResult(EmployeeEffect.ShowError(it))).let { null } },
                        onRightAction = { null },
                    )
                }
                is ActionWithAvatar.None -> actionWithAvatar.uri
            }

            val updatedEditModel = editModel.copy(uid = uid, avatar = avatar)
            val employee = updatedEditModel.convertToBase().mapToDomain()

            employeeInteractor.addOrUpdateEmployee(employee).handle(
                onLeftAction = { emit(EffectResult(EmployeeEffect.ShowError(it))) },
                onRightAction = { emit(EffectResult(EmployeeEffect.NavigateToBack)) },
            )
        }
    }
}

internal sealed class EmployeeWorkCommand : WorkCommand {
    data class LoadEditModel(val employeeId: UID?, val organizationId: UID) : EmployeeWorkCommand()
    data class LoadOrganization(val organizationId: UID) : EmployeeWorkCommand()
    data class SaveEditModel(
        val editModel: EditEmployeeUi,
        val actionWithAvatar: ActionWithAvatar,
    ) : EmployeeWorkCommand()
}