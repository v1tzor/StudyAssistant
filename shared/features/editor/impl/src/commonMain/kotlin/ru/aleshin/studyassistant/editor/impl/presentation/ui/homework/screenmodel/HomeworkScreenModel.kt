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
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.BaseScreenModel
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkScope
import ru.aleshin.studyassistant.core.common.extensions.mapEpochTimeToInstant
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.editor.api.navigation.EditorScreen
import ru.aleshin.studyassistant.editor.impl.di.holder.EditorFeatureDIHolder
import ru.aleshin.studyassistant.editor.impl.navigation.EditorScreenProvider
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.contarct.HomeworkAction
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.contarct.HomeworkDeps
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.contarct.HomeworkEffect
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.contarct.HomeworkEvent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.contarct.HomeworkViewState

/**
 * @author Stanislav Aleshin on 22.06.2024
 */
internal class HomeworkScreenModel(
    private val workProcessor: HomeworkWorkProcessor,
    private val screenProvider: EditorScreenProvider,
    stateCommunicator: HomeworkStateCommunicator,
    effectCommunicator: HomeworkEffectCommunicator,
    coroutineManager: CoroutineManager,
) : BaseScreenModel<HomeworkViewState, HomeworkEvent, HomeworkAction, HomeworkEffect, HomeworkDeps>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun init(deps: HomeworkDeps) {
        if (!isInitialize) {
            super.init(deps)
            dispatchEvent(HomeworkEvent.Init(deps.homeworkId, deps.date, deps.subjectId, deps.organizationId))
        }
    }

    override suspend fun WorkScope<HomeworkViewState, HomeworkAction, HomeworkEffect>.handleEvent(
        event: HomeworkEvent,
    ) {
        when (event) {
            is HomeworkEvent.Init -> with(event) {
                val date = date?.mapEpochTimeToInstant()
                launchBackgroundWork(BackgroundKey.LOAD_HOMEWORK) {
                    val command = HomeworkWorkCommand.LoadEditModel(
                        homeworkId = homeworkId,
                        date = date,
                        subjectId = subjectId,
                        organizationId = organizationId,
                    )
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_ORGANIZATIONS) {
                    val command = HomeworkWorkCommand.LoadOrganizations
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_SUBJECTS) {
                    val command = HomeworkWorkCommand.LoadSubjects(event.organizationId)
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_CLASSES) {
                    val command = HomeworkWorkCommand.LoadClassesForLinked(subjectId, date, homeworkId)
                    workProcessor.work(command).collectAndHandleWork()
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
                    val currentHomework = editableHomework?.uid?.ifEmpty { null }
                    val command = HomeworkWorkCommand.LoadClassesForLinked(null, null, currentHomework)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is HomeworkEvent.UpdateSubject -> with(state()) {
                val updatedHomework = editableHomework?.copy(
                    subject = event.subject,
                    classId = null,
                )
                sendAction(HomeworkAction.UpdateEditModel(updatedHomework))
                launchBackgroundWork(BackgroundKey.LOAD_CLASSES) {
                    val subjectId = event.subject?.uid
                    val instant = editableHomework?.deadline
                    val currentHomework = editableHomework?.uid?.ifEmpty { null }
                    val command = HomeworkWorkCommand.LoadClassesForLinked(subjectId, instant, currentHomework)
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
                    val currentHomework = editableHomework?.uid?.ifEmpty { null }
                    val command = HomeworkWorkCommand.LoadClassesForLinked(subjectId, instant, currentHomework)
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
                val featureScreen = EditorScreen.Organization(event.organizationId)
                val screen = screenProvider.provideFeatureScreen(featureScreen)
                sendEffect(HomeworkEffect.NavigateToLocal(screen))
            }
            is HomeworkEvent.NavigateToSubjectEditor -> with(state()) {
                val organizationId = checkNotNull(editableHomework?.organization).uid
                val featureScreen = EditorScreen.Subject(event.subjectId, organizationId)
                val screen = screenProvider.provideFeatureScreen(featureScreen)
                sendEffect(HomeworkEffect.NavigateToLocal(screen))
            }
            is HomeworkEvent.NavigateToBack -> {
                sendEffect(HomeworkEffect.NavigateToBack)
            }
        }
    }

    override suspend fun reduce(
        action: HomeworkAction,
        currentState: HomeworkViewState,
    ) = when (action) {
        is HomeworkAction.SetupEditModel -> currentState.copy(
            editableHomework = action.editModel,
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
        is HomeworkAction.UpdateLoading -> currentState.copy(
            isLoading = action.isLoading,
        )
        is HomeworkAction.UpdateClassesLoading -> currentState.copy(
            isClassesLoading = action.isLoading,
        )
    }

    enum class BackgroundKey : BackgroundWorkKey {
        LOAD_HOMEWORK, LOAD_ORGANIZATIONS, LOAD_SUBJECTS, LOAD_CLASSES, HOMEWORK_ACTION
    }
}

@Composable
internal fun Screen.rememberHomeworkScreenModel(): HomeworkScreenModel {
    val di = EditorFeatureDIHolder.fetchDI()
    return rememberScreenModel { di.instance<HomeworkScreenModel>() }
}