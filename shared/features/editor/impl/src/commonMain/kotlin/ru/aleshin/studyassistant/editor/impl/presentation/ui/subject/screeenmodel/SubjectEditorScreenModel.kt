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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.screeenmodel

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.BaseScreenModel
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.editor.api.navigation.EditorScreen
import ru.aleshin.studyassistant.editor.impl.di.holder.EditorFeatureDIHolder
import ru.aleshin.studyassistant.editor.impl.navigation.EditorScreenProvider
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.convertToBase
import ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.contract.SubjectEditorAction
import ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.contract.SubjectEditorDeps
import ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.contract.SubjectEditorEffect
import ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.contract.SubjectEditorEvent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.contract.SubjectEditorViewState

/**
 * @author Stanislav Aleshin on 05.06.2024
 */
internal class SubjectEditorScreenModel(
    private val workProcessor: SubjectEditorWorkProcessor,
    private val screenProvider: EditorScreenProvider,
    stateCommunicator: SubjectEditorStateCommunicator,
    effectCommunicator: SubjectEditorEffectCommunicator,
    coroutineManager: CoroutineManager,
) : BaseScreenModel<SubjectEditorViewState, SubjectEditorEvent, SubjectEditorAction, SubjectEditorEffect, SubjectEditorDeps>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun init(deps: SubjectEditorDeps) {
        if (!isInitialize) {
            super.init(deps)
            dispatchEvent(SubjectEditorEvent.Init(deps.subjectId, deps.organizationId))
        }
    }

    override suspend fun WorkScope<SubjectEditorViewState, SubjectEditorAction, SubjectEditorEffect>.handleEvent(
        event: SubjectEditorEvent,
    ) {
        when (event) {
            is SubjectEditorEvent.Init -> {
                launchBackgroundWork(BackgroundKey.LOAD_EMPLOYEES) {
                    val command = SubjectEditorWorkCommand.LoadEmployees(event.organizationId)
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_SUBJECT) {
                    val command = SubjectEditorWorkCommand.LoadEditModel(event.subjectId, event.organizationId)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is SubjectEditorEvent.SelectEventType -> with(state()) {
                val updatedSubject = editableSubject?.copy(eventType = event.type)
                sendAction(SubjectEditorAction.UpdateEditModel(updatedSubject))
            }
            is SubjectEditorEvent.EditName -> with(state()) {
                val updatedSubject = editableSubject?.copy(name = event.name)
                sendAction(SubjectEditorAction.UpdateEditModel(updatedSubject))
            }
            is SubjectEditorEvent.UpdateColor -> with(state()) {
                val updatedSubject = editableSubject?.copy(color = event.color)
                sendAction(SubjectEditorAction.UpdateEditModel(updatedSubject))
            }
            is SubjectEditorEvent.UpdateTeacher -> with(state()) {
                val updatedSubject = editableSubject?.copy(teacher = event.teacher?.convertToBase())
                sendAction(SubjectEditorAction.UpdateEditModel(updatedSubject))
            }
            is SubjectEditorEvent.UpdateLocation -> with(state()) {
                val updatedSubject = editableSubject?.copy(location = event.location, office = event.office)
                sendAction(SubjectEditorAction.UpdateEditModel(updatedSubject))
            }
            is SubjectEditorEvent.UpdateOrganizationOffices -> with(state()) {
                launchBackgroundWork(BackgroundKey.UPDATE_LOCATIONS) {
                    val organization = checkNotNull(organization)
                    val command = SubjectEditorWorkCommand.UpdateOrganizationOffices(organization, event.offices)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is SubjectEditorEvent.UpdateOrganizationLocations -> with(state()) {
                launchBackgroundWork(BackgroundKey.UPDATE_LOCATIONS) {
                    val organization = checkNotNull(organization)
                    val command = SubjectEditorWorkCommand.UpdateOrganizationLocations(organization, event.locations)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is SubjectEditorEvent.SaveSubject -> with(state()) {
                launchBackgroundWork(BackgroundKey.SAVE_SUBJECT) {
                    val subject = checkNotNull(editableSubject)
                    val command = SubjectEditorWorkCommand.SaveEditModel(subject)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is SubjectEditorEvent.NavigateToEmployeeEditor -> with(state()) {
                val organization = checkNotNull(organization)
                val featureScreen = EditorScreen.Employee(event.employeeId, organization.uid)
                val targetScreen = screenProvider.provideFeatureScreen(featureScreen)
                sendEffect(SubjectEditorEffect.NavigateToLocal(targetScreen))
            }
            is SubjectEditorEvent.NavigateToBack -> {
                sendEffect(SubjectEditorEffect.NavigateToBack)
            }
        }
    }

    override suspend fun reduce(
        action: SubjectEditorAction,
        currentState: SubjectEditorViewState,
    ) = when (action) {
        is SubjectEditorAction.SetupEditModel -> currentState.copy(
            editableSubject = action.editModel,
            organization = action.organization,
            isLoading = false,
        )
        is SubjectEditorAction.UpdateEditModel -> currentState.copy(
            editableSubject = action.editModel,
        )
        is SubjectEditorAction.UpdateEmployees -> currentState.copy(
            employees = action.employees,
        )
        is SubjectEditorAction.UpdateLoading -> currentState.copy(
            isLoading = action.isLoading,
        )
    }

    enum class BackgroundKey : BackgroundWorkKey {
        LOAD_SUBJECT, LOAD_EMPLOYEES, UPDATE_LOCATIONS, SAVE_SUBJECT
    }
}

@Composable
internal fun Screen.rememberSubjectEditorScreenModel(): SubjectEditorScreenModel {
    val di = EditorFeatureDIHolder.fetchDI()
    return rememberScreenModel { di.instance<SubjectEditorScreenModel>() }
}