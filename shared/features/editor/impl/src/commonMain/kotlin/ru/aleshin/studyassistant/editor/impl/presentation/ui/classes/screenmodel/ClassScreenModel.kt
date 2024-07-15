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
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.contract.ClassAction
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.contract.ClassDeps
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.contract.ClassEffect
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.contract.ClassEvent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.contract.ClassViewState

/**
 * @author Stanislav Aleshin on 02.06.2024
 */
internal class ClassScreenModel(
    private val workProcessor: ClassWorkProcessor,
    private val screenProvider: EditorScreenProvider,
    stateCommunicator: ClassStateCommunicator,
    effectCommunicator: ClassEffectCommunicator,
    coroutineManager: CoroutineManager,
) : BaseScreenModel<ClassViewState, ClassEvent, ClassAction, ClassEffect, ClassDeps>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun init(deps: ClassDeps) {
        if (!isInitialize) {
            super.init(deps)
            dispatchEvent(ClassEvent.Init(deps.classId, deps.scheduleId, deps.organizationId, deps.customSchedule, deps.weekDay))
        }
    }

    override suspend fun WorkScope<ClassViewState, ClassAction, ClassEffect>.handleEvent(
        event: ClassEvent,
    ) {
        when (event) {
            is ClassEvent.Init -> {
                launchBackgroundWork(BackgroundKey.LOAD_CLASS) {
                    val command = ClassWorkCommand.LoadEditModel(
                        classId = event.classId,
                        scheduleId = event.scheduleId,
                        organizationId = event.organizationId,
                        isCustomSchedule = event.isCustomSchedule,
                        weekDay = event.weekDay,
                    )
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_ORGANIZATIONS) {
                    val command = ClassWorkCommand.LoadOrganizations
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_SUBJECTS) {
                    val command = ClassWorkCommand.LoadSubjects(event.organizationId)
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_EMPLOYEES) {
                    val command = ClassWorkCommand.LoadEmployees(event.organizationId)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ClassEvent.UpdateOrganization -> with(state()) {
                val updatedClass = editableClass?.copy(
                    organization = event.organization,
                    eventType = null,
                    subject = null,
                    customData = null,
                    teacher = null,
                    office = null,
                    location = null,
                )
                sendAction(ClassAction.UpdateEditModel(updatedClass))
                launchBackgroundWork(BackgroundKey.LOAD_SUBJECTS) {
                    val command = ClassWorkCommand.LoadSubjects(event.organization?.uid)
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_FREE_CLASSES) {
                    val command = ClassWorkCommand.LoadFreeClasses(event.organization, schedule)
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_EMPLOYEES) {
                    val command = ClassWorkCommand.LoadEmployees(event.organization?.uid)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ClassEvent.UpdateSubject -> with(state()) {
                val updatedClass = editableClass?.copy(
                    subject = event.subject,
                    eventType = event.type,
                    teacher = event.subject?.teacher,
                    location = event.subject?.location,
                    office = event.subject?.office,
                )
                sendAction(ClassAction.UpdateEditModel(updatedClass))
            }
            is ClassEvent.UpdateTeacher -> with(state()) {
                val updatedClass = editableClass?.copy(teacher = event.teacher?.convertToBase())
                sendAction(ClassAction.UpdateEditModel(updatedClass))
            }
            is ClassEvent.UpdateLocation -> with(state()) {
                val updatedClass = editableClass?.copy(location = event.location, office = event.office)
                sendAction(ClassAction.UpdateEditModel(updatedClass))
            }
            is ClassEvent.UpdateTime -> with(state()) {
                val updatedClass = editableClass?.copy(startTime = event.startTime, endTime = event.endTime)
                sendAction(ClassAction.UpdateEditModel(updatedClass))
            }
            is ClassEvent.UpdateNotifyParams -> with(state()) {
                val updateClass = editableClass?.copy(notification = event.notification)
                sendAction(ClassAction.UpdateEditModel(updateClass))
            }
            is ClassEvent.UpdateOrganizationOffices -> with(state()) {
                launchBackgroundWork(BackgroundKey.UPDATE_LOCATIONS) {
                    val organization = editableClass?.organization
                    if (organization != null) {
                        val command = ClassWorkCommand.UpdateOffices(organization, event.offices)
                        workProcessor.work(command).collectAndHandleWork()
                    }
                }
            }
            is ClassEvent.UpdateOrganizationLocations -> with(state()) {
                launchBackgroundWork(BackgroundKey.UPDATE_LOCATIONS) {
                    val organization = editableClass?.organization
                    if (organization != null) {
                        val command = ClassWorkCommand.UpdateLocations(organization, event.locations)
                        workProcessor.work(command).collectAndHandleWork()
                    }
                }
            }
            is ClassEvent.SaveClass -> with(state()) {
                launchBackgroundWork(BackgroundKey.SAVE_CLASS) {
                    if (editableClass != null && schedule != null && weekDay != null) {
                        val command = ClassWorkCommand.SaveEditModel(editableClass, schedule, weekDay)
                        workProcessor.work(command).collectAndHandleWork()
                    }
                }
            }
            is ClassEvent.NavigateToOrganizationEditor -> {
                val featureScreen = EditorScreen.Organization(event.organizationId)
                val screen = screenProvider.provideFeatureScreen(featureScreen)
                sendEffect(ClassEffect.NavigateToLocal(screen))
            }
            is ClassEvent.NavigateToSubjectEditor -> with(state()) {
                val organization = checkNotNull(editableClass?.organization)
                val featureScreen = EditorScreen.Subject(event.subjectId, organization.uid)
                val targetScreen = screenProvider.provideFeatureScreen(featureScreen)
                sendEffect(ClassEffect.NavigateToLocal(targetScreen))
            }
            is ClassEvent.NavigateToEmployeeEditor -> with(state()) {
                val organization = checkNotNull(editableClass?.organization)
                val featureScreen = EditorScreen.Employee(event.employeeId, organization.uid)
                val targetScreen = screenProvider.provideFeatureScreen(featureScreen)
                sendEffect(ClassEffect.NavigateToLocal(targetScreen))
            }
            is ClassEvent.NavigateToBack -> {
                sendEffect(ClassEffect.NavigateToBack)
            }
        }
    }

    override suspend fun reduce(
        action: ClassAction,
        currentState: ClassViewState,
    ) = when (action) {
        is ClassAction.SetupEditModel -> currentState.copy(
            editableClass = action.editModel,
            schedule = action.schedule,
            freeClassTimeRanges = action.freeClassTimeRanges,
            weekDay = action.weekDay,
            isLoading = false,
        )
        is ClassAction.UpdateEditModel -> currentState.copy(
            editableClass = action.editModel,
        )
        is ClassAction.UpdateOrganizations -> currentState.copy(
            organizations = action.organizations,
        )
        is ClassAction.UpdateFreeClasses -> currentState.copy(
            freeClassTimeRanges = action.freeClassTimeRanges,
        )
        is ClassAction.UpdateSubjects -> currentState.copy(
            subjects = action.subjects,
        )
        is ClassAction.UpdateEmployees -> currentState.copy(
            employees = action.employees,
        )
        is ClassAction.UpdateLoading -> currentState.copy(
            isLoading = action.isLoading,
        )
    }

    enum class BackgroundKey : BackgroundWorkKey {
        LOAD_CLASS, LOAD_ORGANIZATIONS, LOAD_FREE_CLASSES, LOAD_EMPLOYEES, LOAD_SUBJECTS, UPDATE_LOCATIONS, SAVE_CLASS
    }
}

@Composable
internal fun Screen.rememberClassScreenModel(): ClassScreenModel {
    val di = EditorFeatureDIHolder.fetchDI()
    return rememberScreenModel { di.instance<ClassScreenModel>() }
}