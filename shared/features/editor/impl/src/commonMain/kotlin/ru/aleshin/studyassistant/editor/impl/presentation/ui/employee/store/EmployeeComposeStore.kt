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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.store

import ru.aleshin.studyassistant.core.common.architecture.store.BaseComposeStore
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.EffectCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.StateCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.ui.mappers.convertToInputFile
import ru.aleshin.studyassistant.core.ui.models.ActionWithAvatar
import ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.contract.EmployeeAction
import ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.contract.EmployeeEffect
import ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.contract.EmployeeEvent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.contract.EmployeeInput
import ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.contract.EmployeeOutput
import ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.contract.EmployeeState

/**
 * @author Stanislav Aleshin on 06.06.2024
 */
internal class EmployeeComposeStore(
    private val workProcessor: EmployeeWorkProcessor,
    stateCommunicator: StateCommunicator<EmployeeState>,
    effectCommunicator: EffectCommunicator<EmployeeEffect>,
    coroutineManager: CoroutineManager,
) : BaseComposeStore<EmployeeState, EmployeeEvent, EmployeeAction, EmployeeEffect, EmployeeInput, EmployeeOutput>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun initialize(input: EmployeeInput, isRestore: Boolean) {
        if (!isRestore) {
            dispatchEvent(EmployeeEvent.Started(input))
        }
    }

    override suspend fun WorkScope<EmployeeState, EmployeeAction, EmployeeEffect, EmployeeOutput>.handleEvent(
        event: EmployeeEvent,
    ) {
        when (event) {
            is EmployeeEvent.Started -> with(event) {
                launchBackgroundWork(BackgroundKey.LOAD_ORGANIZATION) {
                    val command = EmployeeWorkCommand.LoadOrganization(inputData.organizationId)
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_EMPLOYEE) {
                    val command = EmployeeWorkCommand.LoadEditModel(inputData.employeeId, inputData.organizationId)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is EmployeeEvent.UpdateAvatar -> with(event) {
                val inputFile = image.convertToInputFile()
                sendAction(EmployeeAction.UpdateActionWithAvatar(ActionWithAvatar.Set(inputFile)))
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
                consumeOutput(EmployeeOutput.NavigateToBack)
            }
        }
    }

    override suspend fun reduce(
        action: EmployeeAction,
        currentState: EmployeeState,
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

    class Factory(
        private val workProcessor: EmployeeWorkProcessor,
        private val coroutineManager: CoroutineManager,
    ) : BaseComposeStore.Factory<EmployeeComposeStore, EmployeeState> {

        override fun create(savedState: EmployeeState): EmployeeComposeStore {
            return EmployeeComposeStore(
                workProcessor = workProcessor,
                stateCommunicator = StateCommunicator.Default(savedState),
                effectCommunicator = EffectCommunicator.Default(),
                coroutineManager = coroutineManager,
            )
        }
    }
}