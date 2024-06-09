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
import ru.aleshin.studyassistant.editor.api.navigation.EditorScreen
import ru.aleshin.studyassistant.editor.impl.di.holder.EditorFeatureDIHolder
import ru.aleshin.studyassistant.editor.impl.navigation.EditorScreenProvider
import ru.aleshin.studyassistant.editor.impl.presentation.models.orgnizations.convertToShort
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
    private val screenProvider: EditorScreenProvider,
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
                        isCustomSchedule = event.isCustomSchedule,
                        weekDay = event.weekDay,
                    )
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ClassEditorEvent.UpdateOffices -> with(state()) {
                launchBackgroundWork(BackgroundKey.UPDATE_LOCATIONS) {
                    val organization = editableClass?.organization
                    if (organization != null) {
                        val command = ClassEditorWorkCommand.UpdateOffices(organization, event.offices)
                        workProcessor.work(command).collectAndHandleWork()
                    }
                }
            }
            is ClassEditorEvent.UpdateLocations -> with(state()) {
                launchBackgroundWork(BackgroundKey.UPDATE_LOCATIONS) {
                    val organization = editableClass?.organization
                    if (organization != null) {
                        val command = ClassEditorWorkCommand.UpdateLocations(organization, event.locations)
                        workProcessor.work(command).collectAndHandleWork()
                    }
                }
            }
            is ClassEditorEvent.SelectOrganization -> with(state()) {
                val updatedClass = editableClass?.copy(
                    organization = event.organization?.convertToShort(),
                    eventType = null,
                    subject = null,
                    customData = null,
                    teacher = null,
                    office = null,
                    location = null,
                )
                sendAction(ClassEditorAction.UpdateEditModel(updatedClass))
            }
            is ClassEditorEvent.SelectSubject -> with(state()) {
                val updatedClass = editableClass?.copy(
                    subject = event.subject,
                    eventType = event.type,
                    teacher = event.subject?.teacher,
                    location = event.subject?.location,
                    office = event.subject?.office,
                )
                sendAction(ClassEditorAction.UpdateEditModel(updatedClass))
            }
            is ClassEditorEvent.SelectTeacher -> with(state()) {
                val updatedClass = editableClass?.copy(teacher = event.teacher)
                sendAction(ClassEditorAction.UpdateEditModel(updatedClass))
            }
            is ClassEditorEvent.SelectLocation -> with(state()) {
                val updatedClass = editableClass?.copy(location = event.location, office = event.office)
                sendAction(ClassEditorAction.UpdateEditModel(updatedClass))
            }
            is ClassEditorEvent.SelectTime -> with(state()) {
                val updatedClass = editableClass?.copy(startTime = event.startTime, endTime = event.endTime)
                sendAction(ClassEditorAction.UpdateEditModel(updatedClass))
            }
            is ClassEditorEvent.ChangeNotifyParams -> with(state()) {
                val updateClass = editableClass?.copy(notification = event.notification)
                sendAction(ClassEditorAction.UpdateEditModel(updateClass))
            }
            is ClassEditorEvent.NavigateToSubjectEditor -> with(state()) {
                if (editableClass?.organization != null) {
                    val featureScreen = EditorScreen.Subject(null, editableClass.organization.uid)
                    val targetScreen = screenProvider.provideFeatureScreen(featureScreen)
                    sendEffect(ClassEditorEffect.NavigateToLocal(targetScreen))
                }
            }
            is ClassEditorEvent.NavigateToEmployeeEditor -> with(state()) {
                if (editableClass?.organization != null) {
                    val featureScreen = EditorScreen.Employee(null, editableClass.organization.uid)
                    val targetScreen = screenProvider.provideFeatureScreen(featureScreen)
                    sendEffect(ClassEditorEffect.NavigateToLocal(targetScreen))
                }
            }
            is ClassEditorEvent.SaveClass -> with(state()) {
                launchBackgroundWork(BackgroundKey.SAVE_CLASS) {
                    if (editableClass != null && schedule != null && weekDay != null) {
                        val command = ClassEditorWorkCommand.SaveEditModel(editableClass, schedule, weekDay)
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
            editableClass = action.editableClass,
            schedule = action.schedule,
            freeClassTimeRanges = action.freeClassTimeRanges,
            weekDay = action.weekDay,
            organizations = action.organizations,
            isLoading = false,
        )
        is ClassEditorAction.UpdateEditModel -> currentState.copy(
            editableClass = action.model,
        )
        is ClassEditorAction.UpdateLoading -> currentState.copy(
            isLoading = action.isLoading,
        )
    }

    enum class BackgroundKey : BackgroundWorkKey {
        LOAD_CLASS, UPDATE_LOCATIONS, SAVE_CLASS
    }
}

@Composable
internal fun Screen.rememberClassEditorScreenModel(): ClassEditorScreenModel {
    val di = EditorFeatureDIHolder.fetchDI()
    return rememberScreenModel { di.instance<ClassEditorScreenModel>() }
}
