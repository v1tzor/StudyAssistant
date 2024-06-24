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
import architecture.screenmodel.BaseScreenModel
import architecture.screenmodel.work.BackgroundWorkKey
import architecture.screenmodel.work.WorkScope
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import managers.CoroutineManager
import org.kodein.di.instance
import ru.aleshin.studyassistant.editor.impl.di.holder.EditorFeatureDIHolder
import ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.contract.EmployeeEditorAction
import ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.contract.EmployeeEditorDeps
import ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.contract.EmployeeEditorEffect
import ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.contract.EmployeeEditorEvent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.contract.EmployeeEditorViewState

/**
 * @author Stanislav Aleshin on 06.06.2024
 */
internal class EmployeeEditorScreenModel(
    private val workProcessor: EmployeeEditorWorkProcessor,
    stateCommunicator: EmployeeEditorStateCommunicator,
    effectCommunicator: EmployeeEditorEffectCommunicator,
    coroutineManager: CoroutineManager,
) : BaseScreenModel<EmployeeEditorViewState, EmployeeEditorEvent, EmployeeEditorAction, EmployeeEditorEffect, EmployeeEditorDeps>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun init(deps: EmployeeEditorDeps) {
        if (!isInitialize) {
            super.init(deps)
            dispatchEvent(EmployeeEditorEvent.Init(deps.employeeId, deps.organizationId))
        }
    }

    override suspend fun WorkScope<EmployeeEditorViewState, EmployeeEditorAction, EmployeeEditorEffect>.handleEvent(
        event: EmployeeEditorEvent,
    ) {
        when (event) {
            is EmployeeEditorEvent.Init -> {
                launchBackgroundWork(BackgroundKey.LOAD_EMPLOYEE) {
                    val command = EmployeeEditorWorkCommand.LoadEditModel(event.employeeId, event.organizationId)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is EmployeeEditorEvent.UpdateAvatar -> with(state()) {
                val updatedEmployee = editableEmployee?.copy(avatar = event.avatarUrl)
                sendAction(EmployeeEditorAction.UpdateEditModel(updatedEmployee))
            }
            is EmployeeEditorEvent.UpdateName -> with(state()) {
                val updatedEmployee = editableEmployee?.copy(
                    firstName = event.first,
                    secondName = event.second,
                    patronymic = event.patronymic,
                )
                sendAction(EmployeeEditorAction.UpdateEditModel(updatedEmployee))
            }
            is EmployeeEditorEvent.UpdatePost -> with(state()) {
                val updatedEmployee = editableEmployee?.copy(post = event.post)
                sendAction(EmployeeEditorAction.UpdateEditModel(updatedEmployee))
            }
            is EmployeeEditorEvent.UpdateWorkTime -> with(state()) {
                val updatedEmployee = editableEmployee?.copy(
                    workTimeStart = event.start,
                    workTimeEnd = event.end,
                )
                sendAction(EmployeeEditorAction.UpdateEditModel(updatedEmployee))
            }
            is EmployeeEditorEvent.UpdateBirthday -> with(state()) {
                val updatedEmployee = editableEmployee?.copy(birthday = event.date)
                sendAction(EmployeeEditorAction.UpdateEditModel(updatedEmployee))
            }
            is EmployeeEditorEvent.UpdateEmails -> with(state()) {
                val updatedEmployee = editableEmployee?.copy(emails = event.emails)
                sendAction(EmployeeEditorAction.UpdateEditModel(updatedEmployee))
            }
            is EmployeeEditorEvent.UpdatePhones -> with(state()) {
                val updatedEmployee = editableEmployee?.copy(phones = event.phones)
                sendAction(EmployeeEditorAction.UpdateEditModel(updatedEmployee))
            }
            is EmployeeEditorEvent.UpdateWebs -> with(state()) {
                val updatedEmployee = editableEmployee?.copy(webs = event.webs)
                sendAction(EmployeeEditorAction.UpdateEditModel(updatedEmployee))
            }
            is EmployeeEditorEvent.UpdateLocations -> with(state()) {
                val updatedEmployee = editableEmployee?.copy(locations = event.locations)
                sendAction(EmployeeEditorAction.UpdateEditModel(updatedEmployee))
            }
            is EmployeeEditorEvent.SaveEmployee -> with(state()) {
                launchBackgroundWork(BackgroundKey.SAVE_EMPLOYEE) {
                    val employee = checkNotNull(editableEmployee)
                    val command = EmployeeEditorWorkCommand.SaveEditModel(employee)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is EmployeeEditorEvent.NavigateToBack -> {
                sendEffect(EmployeeEditorEffect.NavigateToBack)
            }
        }
    }

    override suspend fun reduce(
        action: EmployeeEditorAction,
        currentState: EmployeeEditorViewState,
    ) = when (action) {
        is EmployeeEditorAction.SetupEditModel -> currentState.copy(
            editableEmployee = action.editModel,
            organization = action.organization,
            isLoading = false,
        )
        is EmployeeEditorAction.UpdateEditModel -> currentState.copy(
            editableEmployee = action.editModel,
        )
        is EmployeeEditorAction.UpdateLoading -> currentState.copy(
            isLoading = action.isLoading,
        )
    }

    enum class BackgroundKey : BackgroundWorkKey {
        LOAD_EMPLOYEE, SAVE_EMPLOYEE
    }
}

@Composable
internal fun Screen.rememberEmployeeEditorScreenModel(): EmployeeEditorScreenModel {
    val di = EditorFeatureDIHolder.fetchDI()
    return rememberScreenModel { di.instance<EmployeeEditorScreenModel>() }
}