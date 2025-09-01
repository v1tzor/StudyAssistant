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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.store

import ru.aleshin.studyassistant.core.common.architecture.store.BaseComposeStore
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.EffectCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.StateCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.editor.api.EditorFeatureComponent.EditorConfig
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.convertToBase
import ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.contract.SubjectAction
import ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.contract.SubjectEffect
import ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.contract.SubjectEvent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.contract.SubjectInput
import ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.contract.SubjectOutput
import ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.contract.SubjectState

/**
 * @author Stanislav Aleshin on 05.06.2024
 */
internal class SubjectComposeStore(
    private val workProcessor: SubjectWorkProcessor,
    stateCommunicator: StateCommunicator<SubjectState>,
    effectCommunicator: EffectCommunicator<SubjectEffect>,
    coroutineManager: CoroutineManager,
) : BaseComposeStore<SubjectState, SubjectEvent, SubjectAction, SubjectEffect, SubjectInput, SubjectOutput>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun initialize(input: SubjectInput, isRestore: Boolean) {
        dispatchEvent(SubjectEvent.Started(input, isRestore))
    }

    override suspend fun WorkScope<SubjectState, SubjectAction, SubjectEffect, SubjectOutput>.handleEvent(
        event: SubjectEvent,
    ) {
        when (event) {
            is SubjectEvent.Started -> with(event) {
                if (!isRestore) {
                    launchBackgroundWork(BackgroundKey.LOAD_SUBJECT) {
                        val command = SubjectWorkCommand.LoadEditModel(
                            subjectId = inputData.subjectId,
                            organizationId = inputData.organizationId,
                        )
                        workProcessor.work(command).collectAndHandleWork()
                    }
                    launchBackgroundWork(BackgroundKey.LOAD_EMPLOYEES) {
                        val command = SubjectWorkCommand.LoadEmployees(inputData.organizationId)
                        workProcessor.work(command).collectAndHandleWork()
                    }
                    launchBackgroundWork(BackgroundKey.LOAD_ORGANIZATION) {
                        val command = SubjectWorkCommand.LoadOrganization(inputData.organizationId)
                        workProcessor.work(command).collectAndHandleWork()
                    }
                } else {
                    val organizationId = state.editableSubject?.organizationId ?: return
                    launchBackgroundWork(BackgroundKey.LOAD_EMPLOYEES) {
                        val command = SubjectWorkCommand.LoadEmployees(organizationId)
                        workProcessor.work(command).collectAndHandleWork()
                    }
                    launchBackgroundWork(BackgroundKey.LOAD_ORGANIZATION) {
                        val command = SubjectWorkCommand.LoadOrganization(organizationId)
                        workProcessor.work(command).collectAndHandleWork()
                    }
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
                val config = EditorConfig.Employee(event.employeeId, organization.uid)
                consumeOutput(SubjectOutput.NavigateToEmployeeEditor(config))
            }
            is SubjectEvent.NavigateToBack -> {
                consumeOutput(SubjectOutput.NavigateToBack)
            }
        }
    }

    override suspend fun reduce(
        action: SubjectAction,
        currentState: SubjectState,
    ) = when (action) {
        is SubjectAction.SetupEditModel -> currentState.copy(
            editableSubject = action.editModel,
            isLoading = false,
        )
        is SubjectAction.UpdateOrganization -> currentState.copy(
            organization = action.organization,
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
        LOAD_SUBJECT, LOAD_ORGANIZATION, LOAD_EMPLOYEES, UPDATE_LOCATIONS, SAVE_SUBJECT
    }

    class Factory(
        private val workProcessor: SubjectWorkProcessor,
        private val coroutineManager: CoroutineManager,
    ) : BaseComposeStore.Factory<SubjectComposeStore, SubjectState> {

        override fun create(savedState: SubjectState): SubjectComposeStore {
            return SubjectComposeStore(
                workProcessor = workProcessor,
                stateCommunicator = StateCommunicator.Default(savedState),
                effectCommunicator = EffectCommunicator.Default(),
                coroutineManager = coroutineManager,
            )
        }
    }
}