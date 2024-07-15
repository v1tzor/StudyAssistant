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
import ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.contract.SubjectAction
import ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.contract.SubjectDeps
import ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.contract.SubjectEffect
import ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.contract.SubjectEvent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.contract.SubjectViewState

/**
 * @author Stanislav Aleshin on 05.06.2024
 */
internal class SubjectScreenModel(
    private val workProcessor: SubjectWorkProcessor,
    private val screenProvider: EditorScreenProvider,
    stateCommunicator: SubjectStateCommunicator,
    effectCommunicator: SubjectEffectCommunicator,
    coroutineManager: CoroutineManager,
) : BaseScreenModel<SubjectViewState, SubjectEvent, SubjectAction, SubjectEffect, SubjectDeps>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun init(deps: SubjectDeps) {
        if (!isInitialize) {
            super.init(deps)
            dispatchEvent(SubjectEvent.Init(deps.subjectId, deps.organizationId))
        }
    }

    override suspend fun WorkScope<SubjectViewState, SubjectAction, SubjectEffect>.handleEvent(
        event: SubjectEvent,
    ) {
        when (event) {
            is SubjectEvent.Init -> {
                launchBackgroundWork(BackgroundKey.LOAD_EMPLOYEES) {
                    val command = SubjectWorkCommand.LoadEmployees(event.organizationId)
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_SUBJECT) {
                    val command = SubjectWorkCommand.LoadEditModel(event.subjectId, event.organizationId)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is SubjectEvent.SelectEventType -> with(state()) {
                val updatedSubject = editableSubject?.copy(eventType = event.type)
                sendAction(SubjectAction.UpdateEditModel(updatedSubject))
            }
            is SubjectEvent.EditName -> with(state()) {
                val updatedSubject = editableSubject?.copy(name = event.name)
                sendAction(SubjectAction.UpdateEditModel(updatedSubject))
            }
            is SubjectEvent.UpdateColor -> with(state()) {
                val updatedSubject = editableSubject?.copy(color = event.color)
                sendAction(SubjectAction.UpdateEditModel(updatedSubject))
            }
            is SubjectEvent.UpdateTeacher -> with(state()) {
                val updatedSubject = editableSubject?.copy(teacher = event.teacher?.convertToBase())
                sendAction(SubjectAction.UpdateEditModel(updatedSubject))
            }
            is SubjectEvent.UpdateLocation -> with(state()) {
                val updatedSubject = editableSubject?.copy(location = event.location, office = event.office)
                sendAction(SubjectAction.UpdateEditModel(updatedSubject))
            }
            is SubjectEvent.UpdateOrganizationOffices -> with(state()) {
                launchBackgroundWork(BackgroundKey.UPDATE_LOCATIONS) {
                    val organization = checkNotNull(organization)
                    val command = SubjectWorkCommand.UpdateOrganizationOffices(organization, event.offices)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is SubjectEvent.UpdateOrganizationLocations -> with(state()) {
                launchBackgroundWork(BackgroundKey.UPDATE_LOCATIONS) {
                    val organization = checkNotNull(organization)
                    val command = SubjectWorkCommand.UpdateOrganizationLocations(organization, event.locations)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is SubjectEvent.SaveSubject -> with(state()) {
                launchBackgroundWork(BackgroundKey.SAVE_SUBJECT) {
                    val subject = checkNotNull(editableSubject)
                    val command = SubjectWorkCommand.SaveEditModel(subject)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is SubjectEvent.NavigateToEmployeeEditor -> with(state()) {
                val organization = checkNotNull(organization)
                val featureScreen = EditorScreen.Employee(event.employeeId, organization.uid)
                val targetScreen = screenProvider.provideFeatureScreen(featureScreen)
                sendEffect(SubjectEffect.NavigateToLocal(targetScreen))
            }
            is SubjectEvent.NavigateToBack -> {
                sendEffect(SubjectEffect.NavigateToBack)
            }
        }
    }

    override suspend fun reduce(
        action: SubjectAction,
        currentState: SubjectViewState,
    ) = when (action) {
        is SubjectAction.SetupEditModel -> currentState.copy(
            editableSubject = action.editModel,
            organization = action.organization,
            isLoading = false,
        )
        is SubjectAction.UpdateEditModel -> currentState.copy(
            editableSubject = action.editModel,
        )
        is SubjectAction.UpdateEmployees -> currentState.copy(
            employees = action.employees,
        )
        is SubjectAction.UpdateLoading -> currentState.copy(
            isLoading = action.isLoading,
        )
    }

    enum class BackgroundKey : BackgroundWorkKey {
        LOAD_SUBJECT, LOAD_EMPLOYEES, UPDATE_LOCATIONS, SAVE_SUBJECT
    }
}

@Composable
internal fun Screen.rememberSubjectScreenModel(): SubjectScreenModel {
    val di = EditorFeatureDIHolder.fetchDI()
    return rememberScreenModel { di.instance<SubjectScreenModel>() }
}