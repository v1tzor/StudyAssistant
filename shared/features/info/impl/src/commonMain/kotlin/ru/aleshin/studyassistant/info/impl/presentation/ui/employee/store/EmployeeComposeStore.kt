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

import ru.aleshin.studyassistant.core.common.architecture.store.BaseComposeStore
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.EffectCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.StateCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.editor.api.EditorFeatureComponent.EditorConfig
import ru.aleshin.studyassistant.info.impl.presentation.ui.employee.contract.EmployeeAction
import ru.aleshin.studyassistant.info.impl.presentation.ui.employee.contract.EmployeeEffect
import ru.aleshin.studyassistant.info.impl.presentation.ui.employee.contract.EmployeeEvent
import ru.aleshin.studyassistant.info.impl.presentation.ui.employee.contract.EmployeeInput
import ru.aleshin.studyassistant.info.impl.presentation.ui.employee.contract.EmployeeOutput
import ru.aleshin.studyassistant.info.impl.presentation.ui.employee.contract.EmployeeState
import ru.aleshin.studyassistant.users.api.UsersFeatureComponent.UsersConfig

/**
 * @author Stanislav Aleshin on 17.06.2024
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
        dispatchEvent(EmployeeEvent.Started(input))
    }

    override suspend fun WorkScope<EmployeeState, EmployeeAction, EmployeeEffect, EmployeeOutput>.handleEvent(
        event: EmployeeEvent,
    ) {
        when (event) {
            is EmployeeEvent.Started -> with(event) {
                launchBackgroundWork(BackgroundKey.LOAD_ORGANIZATIONS) {
                    val command = EmployeeWorkCommand.LoadOrganizations(inputData.organizationId)
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_EMPLOYEES) {
                    val command = EmployeeWorkCommand.LoadEmployees(inputData.organizationId)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is EmployeeEvent.SearchEmployee -> with(state()) {
                launchBackgroundWork(BackgroundKey.LOAD_EMPLOYEES) {
                    val organization = checkNotNull(selectedOrganization)
                    val command = EmployeeWorkCommand.SearchEmployees(event.query, organization)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is EmployeeEvent.SelectedOrganization -> {
                sendAction(EmployeeAction.UpdateSelectedOrganization(event.organization))
                launchBackgroundWork(BackgroundKey.LOAD_EMPLOYEES) {
                    val command = EmployeeWorkCommand.LoadEmployees(event.organization)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is EmployeeEvent.ClickDeleteEmployee -> {
                launchBackgroundWork(BackgroundKey.DELETE_EMPLOYEE) {
                    val command = EmployeeWorkCommand.DeleteEmployee(event.employeeId)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is EmployeeEvent.ClickEditEmployee -> with(state()) {
                val organization = checkNotNull(selectedOrganization)
                val config = EditorConfig.Employee(event.employeeId, organization)
                consumeOutput(EmployeeOutput.NavigateToEmployeeEditor(config))
            }
            is EmployeeEvent.ClickEmployeeProfile -> {
                val config = UsersConfig.EmployeeProfile(event.employeeId)
                consumeOutput(EmployeeOutput.NavigateToEmployeeProfile(config))
            }
            is EmployeeEvent.BackClick -> {
                consumeOutput(EmployeeOutput.NavigateToBack)
            }
        }
    }

    override suspend fun reduce(
        action: EmployeeAction,
        currentState: EmployeeState,
    ) = when (action) {
        is EmployeeAction.UpdateEmployees -> currentState.copy(
            employees = action.employees,
            isLoading = false,
        )
        is EmployeeAction.UpdateOrganizations -> currentState.copy(
            organizations = action.organizations,
            selectedOrganization = action.selectedOrganization,
        )
        is EmployeeAction.UpdateSelectedOrganization -> currentState.copy(
            selectedOrganization = action.organization,
        )
        is EmployeeAction.UpdateLoading -> currentState.copy(
            isLoading = action.isLoading,
        )
    }

    enum class BackgroundKey : BackgroundWorkKey {
        LOAD_ORGANIZATIONS, LOAD_EMPLOYEES, DELETE_EMPLOYEE
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