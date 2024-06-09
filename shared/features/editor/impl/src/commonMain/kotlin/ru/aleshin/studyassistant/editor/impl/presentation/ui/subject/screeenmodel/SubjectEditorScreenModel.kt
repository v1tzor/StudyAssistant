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
import architecture.screenmodel.BaseScreenModel
import architecture.screenmodel.work.BackgroundWorkKey
import architecture.screenmodel.work.WorkScope
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import managers.CoroutineManager
import org.kodein.di.instance
import ru.aleshin.studyassistant.editor.api.navigation.EditorScreen
import ru.aleshin.studyassistant.editor.impl.di.holder.EditorFeatureDIHolder
import ru.aleshin.studyassistant.editor.impl.navigation.EditorScreenProvider
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
            is SubjectEditorEvent.Init -> launchBackgroundWork(BackgroundKey.LOAD_SUBJECT) {
                val command = SubjectEditorWorkCommand.LoadEditModel(event.subjectId, event.organizationId)
                workProcessor.work(command).collectAndHandleWork()
            }
            is SubjectEditorEvent.UpdateOffices -> with(state()) {
                launchBackgroundWork(BackgroundKey.UPDATE_LOCATIONS) {
                    if (organization != null) {
                        val command = SubjectEditorWorkCommand.UpdateOffices(organization, event.offices)
                        workProcessor.work(command).collectAndHandleWork()
                    }
                }
            }
            is SubjectEditorEvent.UpdateLocations -> with(state()) {
                launchBackgroundWork(BackgroundKey.UPDATE_LOCATIONS) {
                    if (organization != null) {
                        val command = SubjectEditorWorkCommand.UpdateLocations(organization, event.locations)
                        workProcessor.work(command).collectAndHandleWork()
                    }
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
            is SubjectEditorEvent.PickColor -> with(state()) {
                val updatedSubject = editableSubject?.copy(color = event.color)
                sendAction(SubjectEditorAction.UpdateEditModel(updatedSubject))
            }
            is SubjectEditorEvent.SelectTeacher -> with(state()) {
                val updatedSubject = editableSubject?.copy(teacher = event.teacher)
                sendAction(SubjectEditorAction.UpdateEditModel(updatedSubject))
            }
            is SubjectEditorEvent.SelectLocation -> with(state()) {
                val updatedSubject = editableSubject?.copy(location = event.location, office = event.office)
                sendAction(SubjectEditorAction.UpdateEditModel(updatedSubject))
            }
            is SubjectEditorEvent.SaveSubject -> with(state()) {
                launchBackgroundWork(BackgroundKey.SAVE_SUBJECT) {
                    if (editableSubject != null) {
                        val command = SubjectEditorWorkCommand.SaveEditModel(editableSubject)
                        workProcessor.work(command).collectAndHandleWork()
                    }
                }
            }
            is SubjectEditorEvent.NavigateToEmployeeEditor -> with(state()) {
                if (editableSubject?.organizationId != null) {
                    val featureScreen = EditorScreen.Employee(null, editableSubject.organizationId)
                    val targetScreen = screenProvider.provideFeatureScreen(featureScreen)
                    sendEffect(SubjectEditorEffect.NavigateToLocal(targetScreen))
                }
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
            editableSubject = action.editableSubject,
            organization = action.organization,
            isLoading = false,
        )
        is SubjectEditorAction.UpdateLoading -> currentState.copy(
            isLoading = action.isLoading,
        )
        is SubjectEditorAction.UpdateEditModel -> currentState.copy(
            editableSubject = action.editableSubject,
        )
    }

    enum class BackgroundKey : BackgroundWorkKey {
        LOAD_SUBJECT, UPDATE_LOCATIONS, SAVE_SUBJECT
    }
}

@Composable
internal fun Screen.rememberSubjectEditorScreenModel(): SubjectEditorScreenModel {
    val di = EditorFeatureDIHolder.fetchDI()
    return rememberScreenModel { di.instance<SubjectEditorScreenModel>() }
}
