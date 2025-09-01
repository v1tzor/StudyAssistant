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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.store

import ru.aleshin.studyassistant.core.common.architecture.store.BaseComposeStore
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.EffectCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.StateCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkScope
import ru.aleshin.studyassistant.core.common.extensions.mapEpochTimeToInstant
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.editor.api.EditorFeatureComponent.EditorConfig
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.contarct.HomeworkAction
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.contarct.HomeworkEffect
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.contarct.HomeworkEvent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.contarct.HomeworkInput
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.contarct.HomeworkOutput
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.contarct.HomeworkState

/**
 * @author Stanislav Aleshin on 22.06.2024
 */
internal class HomeworkComposeStore(
    private val workProcessor: HomeworkWorkProcessor,
    private val dateManager: DateManager,
    stateCommunicator: StateCommunicator<HomeworkState>,
    effectCommunicator: EffectCommunicator<HomeworkEffect>,
    coroutineManager: CoroutineManager,
) : BaseComposeStore<HomeworkState, HomeworkEvent, HomeworkAction, HomeworkEffect, HomeworkInput, HomeworkOutput>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun initialize(input: HomeworkInput, isRestore: Boolean) {
        dispatchEvent(HomeworkEvent.Started(input, isRestore))
    }

    override suspend fun WorkScope<HomeworkState, HomeworkAction, HomeworkEffect, HomeworkOutput>.handleEvent(
        event: HomeworkEvent,
    ) {
        when (event) {
            is HomeworkEvent.Started -> with(event) {
                sendAction(HomeworkAction.UpdateCurrentDate(dateManager.fetchBeginningCurrentInstant()))
                launchBackgroundWork(BackgroundKey.LOAD_ORGANIZATIONS) {
                    val command = HomeworkWorkCommand.LoadOrganizations
                    workProcessor.work(command).collectAndHandleWork()
                }
                if (!isRestore) {
                    val date = inputData.date?.mapEpochTimeToInstant()
                    launchBackgroundWork(BackgroundKey.LOAD_HOMEWORK) {
                        val command = HomeworkWorkCommand.LoadEditModel(
                            homeworkId = inputData.homeworkId,
                            date = date,
                            subjectId = inputData.subjectId,
                            organizationId = inputData.organizationId,
                        )
                        workProcessor.work(command).collectAndHandleWork()
                    }
                    launchBackgroundWork(BackgroundKey.LOAD_SUBJECTS) {
                        val command = HomeworkWorkCommand.LoadSubjects(inputData.organizationId)
                        workProcessor.work(command).collectAndHandleWork()
                    }
                    launchBackgroundWork(BackgroundKey.LOAD_CLASSES) {
                        val command = HomeworkWorkCommand.LoadClassesForLinked(inputData.subjectId, date)
                        workProcessor.work(command).collectAndHandleWork()
                    }
                } else {
                    val editModel = state.editableHomework ?: return
                    launchBackgroundWork(BackgroundKey.LOAD_SUBJECTS) {
                        val command = HomeworkWorkCommand.LoadSubjects(editModel.organization?.uid)
                        workProcessor.work(command).collectAndHandleWork()
                    }
                    launchBackgroundWork(BackgroundKey.LOAD_CLASSES) {
                        val command = HomeworkWorkCommand.LoadClassesForLinked(
                            subjectId = editModel.subject?.uid,
                            date = editModel.deadline,
                        )
                        workProcessor.work(command).collectAndHandleWork()
                    }
                }
            }
            is HomeworkEvent.UpdateOrganization -> with(state()) {
                val updatedHomework = editableHomework?.copy(
                    organization = event.organization,
                    classId = null,
                    subject = null,
                )
                sendAction(HomeworkAction.UpdateEditModel(updatedHomework))
                launchBackgroundWork(BackgroundKey.LOAD_SUBJECTS) {
                    val command = HomeworkWorkCommand.LoadSubjects(event.organization?.uid)
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_CLASSES) {
                    val command = HomeworkWorkCommand.LoadClassesForLinked(null, null)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is HomeworkEvent.UpdateSubject -> with(state()) {
                val updatedHomework = editableHomework?.copy(
                    subject = event.subject,
                    classId = null,
                    deadline = null,
                )
                sendAction(HomeworkAction.UpdateEditModel(updatedHomework))
                launchBackgroundWork(BackgroundKey.LOAD_CLASSES) {
                    val subjectId = event.subject?.uid
                    val command = HomeworkWorkCommand.LoadClassesForLinked(subjectId, currentDate)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is HomeworkEvent.UpdateDate -> with(state()) {
                val updatedHomework = editableHomework?.copy(
                    deadline = event.date,
                    classId = null,
                )
                sendAction(HomeworkAction.UpdateEditModel(updatedHomework))
                launchBackgroundWork(BackgroundKey.LOAD_CLASSES) {
                    val subjectId = editableHomework?.subject?.uid
                    val instant = event.date
                    val command = HomeworkWorkCommand.LoadClassesForLinked(subjectId, instant)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is HomeworkEvent.UpdateLinkedClass -> with(state()) {
                val updatedHomework = editableHomework?.copy(
                    classId = event.classId,
                    deadline = event.date,
                )
                sendAction(HomeworkAction.UpdateEditModel(updatedHomework))
            }
            is HomeworkEvent.UpdateTask -> with(state()) {
                val updatedHomework = editableHomework?.copy(
                    theoreticalTasks = event.theory,
                    practicalTasks = event.practice,
                    presentationTasks = event.presentations,
                )
                sendAction(HomeworkAction.UpdateEditModel(updatedHomework))
            }
            is HomeworkEvent.UpdateTestTopic -> with(state()) {
                val updatedHomework = editableHomework?.copy(
                    isTest = event.isTest,
                    testTopic = event.topic,
                )
                sendAction(HomeworkAction.UpdateEditModel(updatedHomework))
            }
            is HomeworkEvent.UpdatePriority -> with(state()) {
                val updatedHomework = editableHomework?.copy(
                    priority = event.priority,
                )
                sendAction(HomeworkAction.UpdateEditModel(updatedHomework))
            }
            is HomeworkEvent.DeleteHomework -> with(state()) {
                launchBackgroundWork(BackgroundKey.HOMEWORK_ACTION) {
                    val homework = checkNotNull(editableHomework)
                    val command = HomeworkWorkCommand.DeleteHomework(homework)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is HomeworkEvent.SaveHomework -> with(state()) {
                launchBackgroundWork(BackgroundKey.HOMEWORK_ACTION) {
                    val homework = checkNotNull(editableHomework)
                    val command = HomeworkWorkCommand.SaveHomework(homework)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is HomeworkEvent.NavigateToOrganizationEditor -> {
                val config = EditorConfig.Organization(event.organizationId)
                consumeOutput(HomeworkOutput.NavigateToOrganizationEditor(config))
            }
            is HomeworkEvent.NavigateToSubjectEditor -> with(state()) {
                val organizationId = checkNotNull(editableHomework?.organization).uid
                val config = EditorConfig.Subject(event.subjectId, organizationId)
                consumeOutput(HomeworkOutput.NavigateToSubjectEditor(config))
            }
            is HomeworkEvent.NavigateToBack -> {
                consumeOutput(HomeworkOutput.NavigateToBack)
            }
        }
    }

    override suspend fun reduce(
        action: HomeworkAction,
        currentState: HomeworkState,
    ) = when (action) {
        is HomeworkAction.SetupEditModel -> currentState.copy(
            editableHomework = action.editModel,
            showDeleteAction = action.showDeleteAction,
            isLoading = false,
        )
        is HomeworkAction.UpdateEditModel -> currentState.copy(
            editableHomework = action.editModel,
        )
        is HomeworkAction.UpdateOrganizations -> currentState.copy(
            organizations = action.organizations,
        )
        is HomeworkAction.UpdateSubjects -> currentState.copy(
            subjects = action.subjects,
        )
        is HomeworkAction.UpdateClassesForLinked -> currentState.copy(
            classesForLinking = action.classes,
            isClassesLoading = false,
        )
        is HomeworkAction.UpdateCurrentDate -> currentState.copy(
            currentDate = action.date,
        )
        is HomeworkAction.UpdateLoading -> currentState.copy(
            isLoading = action.isLoading,
        )
        is HomeworkAction.UpdateLoadingSave -> currentState.copy(
            isLoadingSave = action.isLoading,
        )
        is HomeworkAction.UpdateClassesLoading -> currentState.copy(
            isClassesLoading = action.isLoading,
        )
    }

    enum class BackgroundKey : BackgroundWorkKey {
        LOAD_HOMEWORK, LOAD_ORGANIZATIONS, LOAD_SUBJECTS, LOAD_CLASSES, HOMEWORK_ACTION
    }

    class Factory(
        private val workProcessor: HomeworkWorkProcessor,
        private val dateManager: DateManager,
        private val coroutineManager: CoroutineManager,
    ) : BaseComposeStore.Factory<HomeworkComposeStore, HomeworkState> {

        override fun create(savedState: HomeworkState): HomeworkComposeStore {
            return HomeworkComposeStore(
                workProcessor = workProcessor,
                dateManager = dateManager,
                stateCommunicator = StateCommunicator.Default(savedState),
                effectCommunicator = EffectCommunicator.Default(),
                coroutineManager = coroutineManager,
            )
        }
    }
}