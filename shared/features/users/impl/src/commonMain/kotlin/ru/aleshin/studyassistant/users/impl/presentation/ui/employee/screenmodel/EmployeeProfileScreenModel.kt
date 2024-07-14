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

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.BaseScreenModel
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.editor.api.navigation.EditorScreen
import ru.aleshin.studyassistant.users.impl.di.holder.UsersFeatureDIHolder
import ru.aleshin.studyassistant.users.impl.navigation.UsersScreenProvider
import ru.aleshin.studyassistant.users.impl.presentation.ui.employee.contract.EmployeeProfileAction
import ru.aleshin.studyassistant.users.impl.presentation.ui.employee.contract.EmployeeProfileDeps
import ru.aleshin.studyassistant.users.impl.presentation.ui.employee.contract.EmployeeProfileEffect
import ru.aleshin.studyassistant.users.impl.presentation.ui.employee.contract.EmployeeProfileEvent
import ru.aleshin.studyassistant.users.impl.presentation.ui.employee.contract.EmployeeProfileViewState

/**
 * @author Stanislav Aleshin on 10.07.2024
 */
internal class EmployeeProfileScreenModel(
    private val workProcessor: EmployeeProfileWorkProcessor,
    private val screenProvider: UsersScreenProvider,
    stateCommunicator: EmployeeProfileStateCommunicator,
    effectCommunicator: EmployeeProfileEffectCommunicator,
    coroutineManager: CoroutineManager,
) : BaseScreenModel<EmployeeProfileViewState, EmployeeProfileEvent, EmployeeProfileAction, EmployeeProfileEffect, EmployeeProfileDeps>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun init(deps: EmployeeProfileDeps) {
        if (!isInitialize) {
            super.init(deps)
            dispatchEvent(EmployeeProfileEvent.Init(deps.employeeId))
        }
    }

    override suspend fun WorkScope<EmployeeProfileViewState, EmployeeProfileAction, EmployeeProfileEffect>.handleEvent(
        event: EmployeeProfileEvent,
    ) {
        when (event) {
            is EmployeeProfileEvent.Init -> {
                launchBackgroundWork(BackgroundKey.LOAD_EMPLOYEE) {
                    val command = EmployeeProfileWorkCommand.LoadEmployee(event.employeeId)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is EmployeeProfileEvent.NavigateToEditor -> with(state()) {
                val employee = checkNotNull(employee)
                val featureScreen = EditorScreen.Employee(employee.uid, employee.organizationId)
                val screen = screenProvider.provideEditorScreen(featureScreen)
                sendEffect(EmployeeProfileEffect.NavigateToGlobal(screen))
            }
            is EmployeeProfileEvent.NavigateToBack -> {
                sendEffect(EmployeeProfileEffect.NavigateToBack)
            }
        }
    }

    override suspend fun reduce(
        action: EmployeeProfileAction,
        currentState: EmployeeProfileViewState,
    ) = when (action) {
        is EmployeeProfileAction.UpdateEmployee -> currentState.copy(
            employee = action.employee,
            isLoading = false,
        )
        is EmployeeProfileAction.UpdateLoading -> currentState.copy(
            isLoading = action.isLoading,
        )
    }

    enum class BackgroundKey : BackgroundWorkKey {
        LOAD_EMPLOYEE,
    }
}

@Composable
internal fun Screen.rememberEmployeeProfileScreenModel(): EmployeeProfileScreenModel {
    val di = UsersFeatureDIHolder.fetchDI()
    return rememberScreenModel { di.instance<EmployeeProfileScreenModel>() }
}