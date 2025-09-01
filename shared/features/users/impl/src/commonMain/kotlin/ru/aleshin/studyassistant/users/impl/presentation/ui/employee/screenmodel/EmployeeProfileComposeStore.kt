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

import ru.aleshin.studyassistant.core.common.architecture.store.BaseComposeStore
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.EffectCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.StateCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.editor.api.EditorFeatureComponent.EditorConfig
import ru.aleshin.studyassistant.users.impl.presentation.ui.employee.contract.EmployeeProfileAction
import ru.aleshin.studyassistant.users.impl.presentation.ui.employee.contract.EmployeeProfileEffect
import ru.aleshin.studyassistant.users.impl.presentation.ui.employee.contract.EmployeeProfileEvent
import ru.aleshin.studyassistant.users.impl.presentation.ui.employee.contract.EmployeeProfileInput
import ru.aleshin.studyassistant.users.impl.presentation.ui.employee.contract.EmployeeProfileOutput
import ru.aleshin.studyassistant.users.impl.presentation.ui.employee.contract.EmployeeProfileState

/**
 * @author Stanislav Aleshin on 10.07.2024
 */
internal class EmployeeProfileComposeStore(
    private val workProcessor: EmployeeProfileWorkProcessor,
    stateCommunicator: StateCommunicator<EmployeeProfileState>,
    effectCommunicator: EffectCommunicator<EmployeeProfileEffect>,
    coroutineManager: CoroutineManager,
) : BaseComposeStore<EmployeeProfileState, EmployeeProfileEvent, EmployeeProfileAction, EmployeeProfileEffect, EmployeeProfileInput, EmployeeProfileOutput>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun initialize(input: EmployeeProfileInput, isRestore: Boolean) {
        dispatchEvent(EmployeeProfileEvent.Started(input.employeeId))
    }

    override suspend fun WorkScope<EmployeeProfileState, EmployeeProfileAction, EmployeeProfileEffect, EmployeeProfileOutput>.handleEvent(
        event: EmployeeProfileEvent,
    ) {
        when (event) {
            is EmployeeProfileEvent.Started -> {
                launchBackgroundWork(BackgroundKey.LOAD_EMPLOYEE) {
                    val command = EmployeeProfileWorkCommand.LoadEmployee(event.employeeId)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is EmployeeProfileEvent.ClickEdit -> with(state()) {
                val employee = checkNotNull(employee)
                val config = EditorConfig.Employee(employee.uid, employee.organizationId)
                consumeOutput(EmployeeProfileOutput.NavigateToEmployeeEditor(config))
            }
            is EmployeeProfileEvent.ClickBack -> {
                consumeOutput(EmployeeProfileOutput.NavigateToBack)
            }
        }
    }

    override suspend fun reduce(
        action: EmployeeProfileAction,
        currentState: EmployeeProfileState,
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

    class Factory(
        private val workProcessor: EmployeeProfileWorkProcessor,
        private val coroutineManager: CoroutineManager,
    ) : BaseComposeStore.Factory<EmployeeProfileComposeStore, EmployeeProfileState> {

        override fun create(savedState: EmployeeProfileState): EmployeeProfileComposeStore {
            return EmployeeProfileComposeStore(
                workProcessor = workProcessor,
                stateCommunicator = StateCommunicator.Default(savedState),
                effectCommunicator = EffectCommunicator.Default(),
                coroutineManager = coroutineManager,
            )
        }
    }
}