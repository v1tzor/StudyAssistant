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
            dispatchEvent(ClassEditorEvent.Init(deps.classId, deps.scheduleId, deps.organizationId, deps.customSchedule, deps.weekDay))
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
                        organizationId = event.organizationId,
                        isCustomSchedule = event.isCustomSchedule,
                        weekDay = event.weekDay,
                    )
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_ORGANIZATIONS) {
                    val command = ClassEditorWorkCommand.LoadOrganizations
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_SUBJECTS) {
                    val command = ClassEditorWorkCommand.LoadSubjects(event.organizationId)
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_EMPLOYEES) {
                    val command = ClassEditorWorkCommand.LoadEmployees(event.organizationId)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ClassEditorEvent.UpdateOrganization -> with(state()) {
                val updatedClass = editableClass?.copy(
                    organization = event.organization,
                    eventType = null,
                    subject = null,
                    customData = null,
                    teacher = null,
                    office = null,
                    location = null,
                )
                sendAction(ClassEditorAction.UpdateEditModel(updatedClass))
                launchBackgroundWork(BackgroundKey.LOAD_SUBJECTS) {
                    val command = ClassEditorWorkCommand.LoadSubjects(event.organization?.uid)
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_FREE_CLASSES) {
                    val command = ClassEditorWorkCommand.LoadFreeClasses(event.organization, schedule)
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_EMPLOYEES) {
                    val command = ClassEditorWorkCommand.LoadEmployees(event.organization?.uid)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ClassEditorEvent.UpdateSubject -> with(state()) {
                val updatedClass = editableClass?.copy(
                    subject = event.subject,
                    eventType = event.type,
                    teacher = event.subject?.teacher,
                    location = event.subject?.location,
                    office = event.subject?.office,
                )
                sendAction(ClassEditorAction.UpdateEditModel(updatedClass))
            }
            is ClassEditorEvent.UpdateTeacher -> with(state()) {
                val updatedClass = editableClass?.copy(teacher = event.teacher?.convertToBase())
                sendAction(ClassEditorAction.UpdateEditModel(updatedClass))
            }
            is ClassEditorEvent.UpdateLocation -> with(state()) {
                val updatedClass = editableClass?.copy(location = event.location, office = event.office)
                sendAction(ClassEditorAction.UpdateEditModel(updatedClass))
            }
            is ClassEditorEvent.UpdateTime -> with(state()) {
                val updatedClass = editableClass?.copy(startTime = event.startTime, endTime = event.endTime)
                sendAction(ClassEditorAction.UpdateEditModel(updatedClass))
            }
            is ClassEditorEvent.UpdateNotifyParams -> with(state()) {
                val updateClass = editableClass?.copy(notification = event.notification)
                sendAction(ClassEditorAction.UpdateEditModel(updateClass))
            }
            is ClassEditorEvent.UpdateOrganizationOffices -> with(state()) {
                launchBackgroundWork(BackgroundKey.UPDATE_LOCATIONS) {
                    val organization = editableClass?.organization
                    if (organization != null) {
                        val command = ClassEditorWorkCommand.UpdateOffices(organization, event.offices)
                        workProcessor.work(command).collectAndHandleWork()
                    }
                }
            }
            is ClassEditorEvent.UpdateOrganizationLocations -> with(state()) {
                launchBackgroundWork(BackgroundKey.UPDATE_LOCATIONS) {
                    val organization = editableClass?.organization
                    if (organization != null) {
                        val command = ClassEditorWorkCommand.UpdateLocations(organization, event.locations)
                        workProcessor.work(command).collectAndHandleWork()
                    }
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
            is ClassEditorEvent.NavigateToOrganizationEditor -> {
                val featureScreen = EditorScreen.Organization(event.organizationId)
                val screen = screenProvider.provideFeatureScreen(featureScreen)
                sendEffect(ClassEditorEffect.NavigateToLocal(screen))
            }
            is ClassEditorEvent.NavigateToSubjectEditor -> with(state()) {
                val organization = checkNotNull(editableClass?.organization)
                val featureScreen = EditorScreen.Subject(event.subjectId, organization.uid)
                val targetScreen = screenProvider.provideFeatureScreen(featureScreen)
                sendEffect(ClassEditorEffect.NavigateToLocal(targetScreen))
            }
            is ClassEditorEvent.NavigateToEmployeeEditor -> with(state()) {
                val organization = checkNotNull(editableClass?.organization)
                val featureScreen = EditorScreen.Employee(event.employeeId, organization.uid)
                val targetScreen = screenProvider.provideFeatureScreen(featureScreen)
                sendEffect(ClassEditorEffect.NavigateToLocal(targetScreen))
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
            editableClass = action.editModel,
            schedule = action.schedule,
            freeClassTimeRanges = action.freeClassTimeRanges,
            weekDay = action.weekDay,
            isLoading = false,
        )
        is ClassEditorAction.UpdateEditModel -> currentState.copy(
            editableClass = action.editModel,
        )
        is ClassEditorAction.UpdateOrganizations -> currentState.copy(
            organizations = action.organizations,
        )
        is ClassEditorAction.UpdateFreeClasses -> currentState.copy(
            freeClassTimeRanges = action.freeClassTimeRanges,
        )
        is ClassEditorAction.UpdateSubjects -> currentState.copy(
            subjects = action.subjects,
        )
        is ClassEditorAction.UpdateEmployees -> currentState.copy(
            employees = action.employees,
        )
        is ClassEditorAction.UpdateLoading -> currentState.copy(
            isLoading = action.isLoading,
        )
    }

    enum class BackgroundKey : BackgroundWorkKey {
        LOAD_CLASS, LOAD_ORGANIZATIONS, LOAD_FREE_CLASSES, LOAD_EMPLOYEES, LOAD_SUBJECTS, UPDATE_LOCATIONS, SAVE_CLASS
    }
}

@Composable
internal fun Screen.rememberClassEditorScreenModel(): ClassEditorScreenModel {
    val di = EditorFeatureDIHolder.fetchDI()
    return rememberScreenModel { di.instance<ClassEditorScreenModel>() }
}