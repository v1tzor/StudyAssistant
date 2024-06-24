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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.screenmodel

import androidx.compose.runtime.Composable
import architecture.screenmodel.BaseScreenModel
import architecture.screenmodel.work.BackgroundWorkKey
import architecture.screenmodel.work.WorkScope
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import extensions.mapEpochTimeToInstant
import managers.CoroutineManager
import org.kodein.di.instance
import ru.aleshin.studyassistant.editor.api.navigation.EditorScreen
import ru.aleshin.studyassistant.editor.impl.di.holder.EditorFeatureDIHolder
import ru.aleshin.studyassistant.editor.impl.navigation.EditorScreenProvider
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.contarct.HomeworkEditorAction
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.contarct.HomeworkEditorDeps
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.contarct.HomeworkEditorEffect
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.contarct.HomeworkEditorEvent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.contarct.HomeworkEditorViewState

/**
 * @author Stanislav Aleshin on 22.06.2024
 */
internal class HomeworkEditorScreenModel(
    private val workProcessor: HomeworkEditorWorkProcessor,
    private val screenProvider: EditorScreenProvider,
    stateCommunicator: HomeworkEditorStateCommunicator,
    effectCommunicator: HomeworkEditorEffectCommunicator,
    coroutineManager: CoroutineManager,
) : BaseScreenModel<HomeworkEditorViewState, HomeworkEditorEvent, HomeworkEditorAction, HomeworkEditorEffect, HomeworkEditorDeps>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun init(deps: HomeworkEditorDeps) {
        if (!isInitialize) {
            super.init(deps)
            dispatchEvent(HomeworkEditorEvent.Init(deps.homeworkId, deps.date, deps.subjectId, deps.organizationId))
        }
    }

    override suspend fun WorkScope<HomeworkEditorViewState, HomeworkEditorAction, HomeworkEditorEffect>.handleEvent(
        event: HomeworkEditorEvent,
    ) {
        when (event) {
            is HomeworkEditorEvent.Init -> with(event) {
                val instant = date?.mapEpochTimeToInstant()
                launchBackgroundWork(BackgroundKey.LOAD_HOMEWORK) {
                    val command = HomeworkEditorWorkCommand.LoadEditModel(
                        homeworkId = homeworkId,
                        date = instant,
                        subjectId = subjectId,
                        organizationId = organizationId,
                    )
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_ORGANIZATIONS) {
                    val command = HomeworkEditorWorkCommand.LoadOrganizations
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_SUBJECTS) {
                    val command = HomeworkEditorWorkCommand.LoadSubjects(event.organizationId)
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_CLASSES) {
                    val command = HomeworkEditorWorkCommand.LoadClassesForLinked(subjectId, instant, homeworkId)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is HomeworkEditorEvent.UpdateOrganization -> with(state()) {
                val updatedClass = editableHomework?.copy(
                    organization = event.organization,
                    classId = null,
                    subject = null,
                )
                sendAction(HomeworkEditorAction.UpdateEditModel(updatedClass))
                launchBackgroundWork(BackgroundKey.LOAD_SUBJECTS) {
                    val command = HomeworkEditorWorkCommand.LoadSubjects(event.organization?.uid)
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_CLASSES) {
                    val currentHomework = editableHomework?.uid?.ifEmpty { null }
                    val command = HomeworkEditorWorkCommand.LoadClassesForLinked(null, null, currentHomework)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is HomeworkEditorEvent.UpdateSubject -> with(state()) {
                val updatedClass = editableHomework?.copy(
                    subject = event.subject,
                    classId = null,
                )
                sendAction(HomeworkEditorAction.UpdateEditModel(updatedClass))
                launchBackgroundWork(BackgroundKey.LOAD_CLASSES) {
                    val subjectId = event.subject?.uid
                    val instant = editableHomework?.date
                    val currentHomework = editableHomework?.uid?.ifEmpty { null }
                    val command = HomeworkEditorWorkCommand.LoadClassesForLinked(subjectId, instant, currentHomework)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is HomeworkEditorEvent.UpdateDate -> with(state()) {
                val updatedClass = editableHomework?.copy(
                    date = event.date,
                    classId = null,
                )
                sendAction(HomeworkEditorAction.UpdateEditModel(updatedClass))
                launchBackgroundWork(BackgroundKey.LOAD_CLASSES) {
                    val subjectId = editableHomework?.subject?.uid
                    val instant = event.date
                    val currentHomework = editableHomework?.uid?.ifEmpty { null }
                    val command = HomeworkEditorWorkCommand.LoadClassesForLinked(subjectId, instant, currentHomework)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is HomeworkEditorEvent.UpdateLinkedClass -> with(state()) {
                val updatedClass = editableHomework?.copy(
                    classId = event.classId,
                    date = event.date,
                )
                sendAction(HomeworkEditorAction.UpdateEditModel(updatedClass))
            }
            is HomeworkEditorEvent.UpdateTask -> with(state()) {
                val updatedClass = editableHomework?.copy(
                    theoreticalTasks = event.theory,
                    practicalTasks = event.practice,
                    presentationTasks = event.presentations,
                )
                sendAction(HomeworkEditorAction.UpdateEditModel(updatedClass))
            }
            is HomeworkEditorEvent.UpdateTestTopic -> with(state()) {
                val updatedClass = editableHomework?.copy(
                    isTest = event.isTest,
                    testTopic = event.topic,
                )
                sendAction(HomeworkEditorAction.UpdateEditModel(updatedClass))
            }
            is HomeworkEditorEvent.UpdatePriority -> with(state()) {
                val updatedClass = editableHomework?.copy(
                    priority = event.priority,
                )
                sendAction(HomeworkEditorAction.UpdateEditModel(updatedClass))
            }
            is HomeworkEditorEvent.DeleteHomework -> with(state()) {
                launchBackgroundWork(BackgroundKey.HOMEWORK_ACTION) {
                    val homework = checkNotNull(editableHomework)
                    val command = HomeworkEditorWorkCommand.DeleteHomework(homework)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is HomeworkEditorEvent.SaveHomework -> with(state()) {
                launchBackgroundWork(BackgroundKey.HOMEWORK_ACTION) {
                    val homework = checkNotNull(editableHomework)
                    val command = HomeworkEditorWorkCommand.SaveHomework(homework)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is HomeworkEditorEvent.NavigateToOrganizationEditor -> {
                val featureScreen = EditorScreen.Organization(event.organizationId)
                val screen = screenProvider.provideFeatureScreen(featureScreen)
                sendEffect(HomeworkEditorEffect.NavigateToLocal(screen))
            }
            is HomeworkEditorEvent.NavigateToSubjectEditor -> with(state()) {
                val organizationId = checkNotNull(editableHomework?.organization).uid
                val featureScreen = EditorScreen.Subject(event.subjectId, organizationId)
                val screen = screenProvider.provideFeatureScreen(featureScreen)
                sendEffect(HomeworkEditorEffect.NavigateToLocal(screen))
            }
            is HomeworkEditorEvent.NavigateToBack -> {
                sendEffect(HomeworkEditorEffect.NavigateToBack)
            }
        }
    }

    override suspend fun reduce(
        action: HomeworkEditorAction,
        currentState: HomeworkEditorViewState,
    ) = when (action) {
        is HomeworkEditorAction.SetupEditModel -> currentState.copy(
            editableHomework = action.editModel,
            isLoading = false,
        )
        is HomeworkEditorAction.UpdateEditModel -> currentState.copy(
            editableHomework = action.editModel,
        )
        is HomeworkEditorAction.UpdateOrganizations -> currentState.copy(
            organizations = action.organizations,
        )
        is HomeworkEditorAction.UpdateSubjects -> currentState.copy(
            subjects = action.subjects,
        )
        is HomeworkEditorAction.UpdateClassesForLinked -> currentState.copy(
            classesForLinking = action.classes,
            isClassesLoading = false,
        )
        is HomeworkEditorAction.UpdateLoading -> currentState.copy(
            isLoading = action.isLoading,
        )
        is HomeworkEditorAction.UpdateClassesLoading -> currentState.copy(
            isClassesLoading = action.isLoading,
        )
    }

    enum class BackgroundKey : BackgroundWorkKey {
        LOAD_HOMEWORK, LOAD_ORGANIZATIONS, LOAD_SUBJECTS, LOAD_CLASSES, HOMEWORK_ACTION
    }
}

@Composable
internal fun Screen.rememberHomeworkEditorScreenModel(): HomeworkEditorScreenModel {
    val di = EditorFeatureDIHolder.fetchDI()
    return rememberScreenModel { di.instance<HomeworkEditorScreenModel>() }
}