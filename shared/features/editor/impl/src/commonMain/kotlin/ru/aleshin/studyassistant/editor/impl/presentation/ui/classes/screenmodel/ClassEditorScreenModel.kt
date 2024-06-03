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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.screenmodel

import androidx.compose.runtime.Composable
import architecture.screenmodel.BaseScreenModel
import architecture.screenmodel.work.BackgroundWorkKey
import architecture.screenmodel.work.WorkScope
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import managers.CoroutineManager
import org.kodein.di.instance
import ru.aleshin.studyassistant.editor.impl.di.holder.EditorFeatureDIHolder
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.contract.ClassEditorAction
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.contract.ClassEditorDeps
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.contract.ClassEditorEffect
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.contract.ClassEditorEvent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.contract.ClassEditorViewState

/**
 * @author Stanislav Aleshin on 02.06.2024
 */
internal class ClassEditorScreenModel(
    private val workProcessor: ClassEditorWorkProcessor,
    stateCommunicator: ClassEditorStateCommunicator,
    effectCommunicator: ClassEditorEffectCommunicator,
    coroutineManager: CoroutineManager,
) : BaseScreenModel<ClassEditorViewState, ClassEditorEvent, ClassEditorAction, ClassEditorEffect, ClassEditorDeps>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun init(deps: ClassEditorDeps) {
        if (!isInitialize) {
            super.init(deps)
            dispatchEvent(ClassEditorEvent.Init(deps.classId, deps.scheduleId, deps.customSchedule, deps.weekDay))
        }
    }

    override suspend fun WorkScope<ClassEditorViewState, ClassEditorAction, ClassEditorEffect>.handleEvent(
        event: ClassEditorEvent,
    ) {
        when (event) {
            is ClassEditorEvent.Init -> {
                launchBackgroundWork(BackgroundKey.LOAD_CLASS) {
                    val command = ClassEditorWorkCommand.LoadEditModel(
                        classId = event.classId,
                        scheduleId = event.scheduleId,
                        isCustomSchedule = event.customSchedule,
                        weekDay = event.weekDay,
                    )
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_ORGANIZATIONS) {
                    val command = ClassEditorWorkCommand.LoadOrganizations
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ClassEditorEvent.SelectOrganization -> with(state()) {
                val action = ClassEditorAction.UpdateEditModel(
                    model = editModel?.copy(organization = event.organization),
                )
                sendAction(action)
                launchBackgroundWork(BackgroundKey.LOAD_ORGANIZATION_DATA) {
                    val command = ClassEditorWorkCommand.LoadOrganizationData(event.organization)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ClassEditorEvent.SelectSubject -> with(state()) {
                val action = ClassEditorAction.UpdateEditModel(
                    model = editModel?.copy(subject = event.subject, eventType = event.type),
                )
                sendAction(action)
            }
            is ClassEditorEvent.SelectTeacher -> with(state()) {
                val action = ClassEditorAction.UpdateEditModel(
                    model = editModel?.copy(teacher = event.teacher),
                )
                sendAction(action)
            }
            is ClassEditorEvent.SelectLocation -> with(state()) {
                val action = ClassEditorAction.UpdateEditModel(
                    model = editModel?.copy(location = event.location, office = event.office),
                )
                sendAction(action)
            }
            is ClassEditorEvent.SelectTime -> with(state()) {
                val action = ClassEditorAction.UpdateEditModel(
                    model = editModel?.copy(startTime = event.startTime, endTime = event.endTime),
                )
                sendAction(action)
            }
            is ClassEditorEvent.ChangeNotifyParams -> with(state()) {
                val action = ClassEditorAction.UpdateEditModel(
                    model = editModel?.copy(notification = event.notification),
                )
                sendAction(action)
            }
            is ClassEditorEvent.SaveClass -> with(state()) {
                launchBackgroundWork(BackgroundKey.SAVE_CLASS) {
                    if (editModel != null && weekDay != null) {
                        val command = ClassEditorWorkCommand.SaveEditModel(editModel, isCustomSchedule, weekDay)
                        workProcessor.work(command).collectAndHandleWork()
                    }
                }
            }
            is ClassEditorEvent.NavigateToBack -> {
                sendEffect(ClassEditorEffect.NavigateToBack)
            }
        }
    }

    override suspend fun reduce(
        action: ClassEditorAction,
        currentState: ClassEditorViewState,
    ) = when (action) {
        is ClassEditorAction.SetupEditModel -> currentState.copy(
            editModel = action.model,
            weekDay = action.weekDay,
            isCustomSchedule = action.customSchedule,
            classesTimeRanges = action.times,
        )
        is ClassEditorAction.UpdateEditModel -> currentState.copy(
            editModel = action.model,
        )
        is ClassEditorAction.UpdateOrganizations -> currentState.copy(
            organizations = action.organizations,
        )
        is ClassEditorAction.UpdateOrganizationData -> currentState.copy(
            subjects = action.subjects,
            teachers = action.employees,
            locations = action.locations,
            offices = action.offices,
        )
    }

    enum class BackgroundKey : BackgroundWorkKey {
        LOAD_CLASS, LOAD_ORGANIZATIONS, LOAD_ORGANIZATION_DATA, SAVE_CLASS
    }
}

@Composable
internal fun Screen.rememberClassEditorScreenModel(): ClassEditorScreenModel {
    val di = EditorFeatureDIHolder.fetchDI()
    return rememberScreenModel { di.instance<ClassEditorScreenModel>() }
}
