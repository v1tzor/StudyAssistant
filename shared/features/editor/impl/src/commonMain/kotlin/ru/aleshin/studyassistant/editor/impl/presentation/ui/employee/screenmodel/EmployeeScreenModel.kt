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

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.BaseScreenModel
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.ui.models.ActionWithAvatar
import ru.aleshin.studyassistant.editor.impl.di.holder.EditorFeatureDIHolder
import ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.contract.EmployeeAction
import ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.contract.EmployeeDeps
import ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.contract.EmployeeEffect
import ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.contract.EmployeeEvent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.contract.EmployeeViewState

/**
 * @author Stanislav Aleshin on 06.06.2024
 */
internal class EmployeeScreenModel(
    private val workProcessor: EmployeeWorkProcessor,
    stateCommunicator: EmployeeStateCommunicator,
    effectCommunicator: EmployeeEffectCommunicator,
    coroutineManager: CoroutineManager,
) : BaseScreenModel<EmployeeViewState, EmployeeEvent, EmployeeAction, EmployeeEffect, EmployeeDeps>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun init(deps: EmployeeDeps) {
        if (!isInitialize) {
            super.init(deps)
            dispatchEvent(EmployeeEvent.Init(deps.employeeId, deps.organizationId))
        }
    }

    override suspend fun WorkScope<EmployeeViewState, EmployeeAction, EmployeeEffect>.handleEvent(
        event: EmployeeEvent,
    ) {
        when (event) {
            is EmployeeEvent.Init -> {
                launchBackgroundWork(BackgroundKey.LOAD_ORGANIZATION) {
                    val command = EmployeeWorkCommand.LoadOrganization(event.organizationId)
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_EMPLOYEE) {
                    val command = EmployeeWorkCommand.LoadEditModel(event.employeeId, event.organizationId)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is EmployeeEvent.UpdateAvatar -> with(event) {
                sendAction(EmployeeAction.UpdateActionWithAvatar(ActionWithAvatar.Set(image)))
            }
            is EmployeeEvent.DeleteAvatar -> with(state()) {
                val action = if (editableEmployee?.avatar != null) {
                    ActionWithAvatar.Delete
                } else {
                    ActionWithAvatar.None(null)
                }
                sendAction(EmployeeAction.UpdateActionWithAvatar(action))
            }
            is EmployeeEvent.UpdateName -> with(state()) {
                val updatedEmployee = editableEmployee?.copy(
                    firstName = event.first,
                    secondName = event.second,
                    patronymic = event.patronymic,
                )
                sendAction(EmployeeAction.UpdateEditModel(updatedEmployee))
            }
            is EmployeeEvent.UpdatePost -> with(state()) {
                val updatedEmployee = editableEmployee?.copy(post = event.post)
                sendAction(EmployeeAction.UpdateEditModel(updatedEmployee))
            }
            is EmployeeEvent.UpdateWorkTime -> with(state()) {
                val updatedEmployee = editableEmployee?.copy(
                    workTimeStart = event.start,
                    workTimeEnd = event.end,
                )
                sendAction(EmployeeAction.UpdateEditModel(updatedEmployee))
            }
            is EmployeeEvent.UpdateBirthday -> with(state()) {
                val updatedEmployee = editableEmployee?.copy(birthday = event.date)
                sendAction(EmployeeAction.UpdateEditModel(updatedEmployee))
            }
            is EmployeeEvent.UpdateEmails -> with(state()) {
                val updatedEmployee = editableEmployee?.copy(emails = event.emails)
                sendAction(EmployeeAction.UpdateEditModel(updatedEmployee))
            }
            is EmployeeEvent.UpdatePhones -> with(state()) {
                val updatedEmployee = editableEmployee?.copy(phones = event.phones)
                sendAction(EmployeeAction.UpdateEditModel(updatedEmployee))
            }
            is EmployeeEvent.UpdateWebs -> with(state()) {
                val updatedEmployee = editableEmployee?.copy(webs = event.webs)
                sendAction(EmployeeAction.UpdateEditModel(updatedEmployee))
            }
            is EmployeeEvent.UpdateLocations -> with(state()) {
                val updatedEmployee = editableEmployee?.copy(locations = event.locations)
                sendAction(EmployeeAction.UpdateEditModel(updatedEmployee))
            }
            is EmployeeEvent.SaveEmployee -> with(state()) {
                launchBackgroundWork(BackgroundKey.SAVE_EMPLOYEE) {
                    val employee = checkNotNull(editableEmployee)
                    val command = EmployeeWorkCommand.SaveEditModel(employee, actionWithAvatar)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is EmployeeEvent.NavigateToBack -> {
                sendEffect(EmployeeEffect.NavigateToBack)
            }
        }
    }

    override suspend fun reduce(
        action: EmployeeAction,
        currentState: EmployeeViewState,
    ) = when (action) {
        is EmployeeAction.SetupEditModel -> currentState.copy(
            editableEmployee = action.editModel,
            actionWithAvatar = ActionWithAvatar.None(action.editModel.avatar),
            isLoading = false,
        )
        is EmployeeAction.UpdateEditModel -> currentState.copy(
            editableEmployee = action.editModel,
        )
        is EmployeeAction.UpdateOrganization -> currentState.copy(
            organization = action.organization,
        )
        is EmployeeAction.UpdateActionWithAvatar -> currentState.copy(
            actionWithAvatar = action.actionWithAvatar,
        )
        is EmployeeAction.UpdateLoading -> currentState.copy(
            isLoading = action.isLoading,
        )
    }

    enum class BackgroundKey : BackgroundWorkKey {
        LOAD_EMPLOYEE, LOAD_ORGANIZATION, SAVE_EMPLOYEE
    }
}

@Composable
internal fun Screen.rememberEmployeeScreenModel(): EmployeeScreenModel {
    val di = EditorFeatureDIHolder.fetchDI()
    return rememberScreenModel { di.instance<EmployeeScreenModel>() }
}