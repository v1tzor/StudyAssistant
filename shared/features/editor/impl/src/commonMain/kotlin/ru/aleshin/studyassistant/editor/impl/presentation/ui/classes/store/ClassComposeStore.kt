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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.store

import ru.aleshin.studyassistant.core.common.architecture.store.BaseComposeStore
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.EffectCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.StateCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.editor.api.EditorFeatureComponent.EditorConfig
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.convertToBase
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.contract.ClassAction
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.contract.ClassEffect
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.contract.ClassEvent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.contract.ClassInput
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.contract.ClassOutput
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.contract.ClassState

/**
 * @author Stanislav Aleshin on 02.06.2024
 */
internal class ClassComposeStore(
    private val workProcessor: ClassWorkProcessor,
    stateCommunicator: StateCommunicator<ClassState>,
    effectCommunicator: EffectCommunicator<ClassEffect>,
    coroutineManager: CoroutineManager,
) : BaseComposeStore<ClassState, ClassEvent, ClassAction, ClassEffect, ClassInput, ClassOutput>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun initialize(input: ClassInput, isRestore: Boolean) {
        dispatchEvent(ClassEvent.Started(input, isRestore))
    }

    override suspend fun WorkScope<ClassState, ClassAction, ClassEffect, ClassOutput>.handleEvent(
        event: ClassEvent,
    ) {
        when (event) {
            is ClassEvent.Started -> with(event) {
                launchBackgroundWork(BackgroundKey.LOAD_ORGANIZATIONS) {
                    val command = ClassWorkCommand.LoadOrganizations
                    workProcessor.work(command).collectAndHandleWork()
                }
                if (!isRestore) {
                    launchBackgroundWork(BackgroundKey.LOAD_CLASS) {
                        val command = ClassWorkCommand.LoadEditModel(
                            classId = inputData.classId,
                            scheduleId = inputData.scheduleId,
                            organizationId = inputData.organizationId,
                            isCustomSchedule = inputData.customSchedule,
                            weekDay = inputData.weekDay,
                        )
                        workProcessor.work(command).collectAndHandleWork()
                    }
                    launchBackgroundWork(BackgroundKey.LOAD_SUBJECTS) {
                        val command = ClassWorkCommand.LoadSubjects(inputData.organizationId)
                        workProcessor.work(command).collectAndHandleWork()
                    }
                    launchBackgroundWork(BackgroundKey.LOAD_EMPLOYEES) {
                        val command = ClassWorkCommand.LoadEmployees(inputData.organizationId)
                        workProcessor.work(command).collectAndHandleWork()
                    }
                } else {
                    val organizationId = state.editableClass?.organization?.uid ?: return
                    launchBackgroundWork(BackgroundKey.LOAD_SUBJECTS) {
                        val command = ClassWorkCommand.LoadSubjects(organizationId)
                        workProcessor.work(command).collectAndHandleWork()
                    }
                    launchBackgroundWork(BackgroundKey.LOAD_EMPLOYEES) {
                        val command = ClassWorkCommand.LoadEmployees(organizationId)
                        workProcessor.work(command).collectAndHandleWork()
                    }
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
                    startTime = null,
                    endTime = null,
                )
                sendAction(ClassAction.UpdateEditModel(updatedClass))
                sendAction(ClassAction.UpdateFreeClasses(null))
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
                val config = EditorConfig.Organization(event.organizationId)
                consumeOutput(ClassOutput.NavigateToOrganizationEditor(config))
            }
            is ClassEvent.NavigateToSubjectEditor -> with(state()) {
                val organization = checkNotNull(editableClass?.organization)
                val config = EditorConfig.Subject(event.subjectId, organization.uid)
                consumeOutput(ClassOutput.NavigateToSubjectEditor(config))
            }
            is ClassEvent.NavigateToEmployeeEditor -> with(state()) {
                val organization = checkNotNull(editableClass?.organization)
                val config = EditorConfig.Employee(event.employeeId, organization.uid)
                consumeOutput(ClassOutput.NavigateToEmployeeEditor(config))
            }
            is ClassEvent.NavigateToBack -> {
                consumeOutput(ClassOutput.NavigateToBack)
            }
        }
    }

    override suspend fun reduce(
        action: ClassAction,
        currentState: ClassState,
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

    class Factory(
        private val workProcessor: ClassWorkProcessor,
        private val coroutineManager: CoroutineManager,
    ) : BaseComposeStore.Factory<ClassComposeStore, ClassState> {

        override fun create(savedState: ClassState): ClassComposeStore {
            return ClassComposeStore(
                workProcessor = workProcessor,
                stateCommunicator = StateCommunicator.Default(savedState),
                effectCommunicator = EffectCommunicator.Default(),
                coroutineManager = coroutineManager,
            )
        }
    }
}