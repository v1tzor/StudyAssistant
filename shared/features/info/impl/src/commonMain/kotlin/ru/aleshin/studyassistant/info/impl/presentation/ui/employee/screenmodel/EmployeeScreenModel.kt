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

package ru.aleshin.studyassistant.info.impl.presentation.ui.employee.screenmodel

import androidx.compose.runtime.Composable
import architecture.screenmodel.BaseScreenModel
import architecture.screenmodel.work.BackgroundWorkKey
import architecture.screenmodel.work.WorkScope
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import managers.CoroutineManager
import org.kodein.di.instance
import ru.aleshin.studyassistant.editor.api.navigation.EditorScreen
import ru.aleshin.studyassistant.info.impl.di.holder.InfoFeatureDIHolder
import ru.aleshin.studyassistant.info.impl.navigation.InfoScreenProvider
import ru.aleshin.studyassistant.info.impl.presentation.ui.employee.contract.EmployeeAction
import ru.aleshin.studyassistant.info.impl.presentation.ui.employee.contract.EmployeeDeps
import ru.aleshin.studyassistant.info.impl.presentation.ui.employee.contract.EmployeeEffect
import ru.aleshin.studyassistant.info.impl.presentation.ui.employee.contract.EmployeeEvent
import ru.aleshin.studyassistant.info.impl.presentation.ui.employee.contract.EmployeeViewState

/**
 * @author Stanislav Aleshin on 17.06.2024
 */
internal class EmployeeScreenModel(
    private val workProcessor: EmployeeWorkProcessor,
    private val screenProvider: InfoScreenProvider,
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
            dispatchEvent(EmployeeEvent.Init(deps.organizationId))
        }
    }

    override suspend fun WorkScope<EmployeeViewState, EmployeeAction, EmployeeEffect>.handleEvent(
        event: EmployeeEvent,
    ) {
        when (event) {
            is EmployeeEvent.Init -> {
                launchBackgroundWork(BackgroundKey.LOAD_ORGANIZATIONS) {
                    val command = EmployeeWorkCommand.LoadOrganizations(event.organizationId)
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_EMPLOYEES) {
                    val command = EmployeeWorkCommand.LoadEmployees(event.organizationId)
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
            is EmployeeEvent.DeleteEmployee -> {
                launchBackgroundWork(BackgroundKey.DELETE_EMPLOYEE) {
                    val command = EmployeeWorkCommand.DeleteEmployee(event.employeeId)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is EmployeeEvent.NavigateToEditor -> with(state()) {
                val organization = checkNotNull(selectedOrganization)
                val featureScreen = EditorScreen.Employee(event.employeeId, organization)
                val screen = screenProvider.provideEditorScreen(featureScreen)
                sendEffect(EmployeeEffect.NavigateToGlobal(screen))
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
        is EmployeeAction.UpdateOrganizations -> currentState.copy(
            organizations = action.organizations,
            selectedOrganization = action.selectedOrganization,
        )
        is EmployeeAction.UpdateEmployees -> currentState.copy(
            employees = action.employees,
            isLoading = false,
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
}

@Composable
internal fun Screen.rememberEmployeeScreenModel(): EmployeeScreenModel {
    val di = InfoFeatureDIHolder.fetchDI()
    return rememberScreenModel { di.instance<EmployeeScreenModel>() }
}