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

import architecture.screenmodel.work.ActionResult
import architecture.screenmodel.work.EffectResult
import architecture.screenmodel.work.FlowWorkProcessor
import architecture.screenmodel.work.WorkCommand
import architecture.screenmodel.work.WorkResult
import extensions.startThisDay
import functional.UID
import functional.collectAndHandle
import functional.firstOrNullHandleAndGet
import functional.firstRightOrNull
import functional.handle
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.datetime.Instant
import managers.DateManager
import ru.aleshin.studyassistant.editor.impl.domain.interactors.HomeworkInteractor
import ru.aleshin.studyassistant.editor.impl.domain.interactors.LinkingClassInteractor
import ru.aleshin.studyassistant.editor.impl.domain.interactors.OrganizationInteractor
import ru.aleshin.studyassistant.editor.impl.domain.interactors.SubjectInteractor
import ru.aleshin.studyassistant.editor.impl.presentation.mappers.mapToDomain
import ru.aleshin.studyassistant.editor.impl.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.tasks.EditHomeworkUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.tasks.convertToBase
import ru.aleshin.studyassistant.editor.impl.presentation.models.tasks.convertToEdit
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.contarct.HomeworkEditorAction
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.contarct.HomeworkEditorEffect

/**
 * @author Stanislav Aleshin on 22.06.2024.
 */
internal interface HomeworkEditorWorkProcessor :
    FlowWorkProcessor<HomeworkEditorWorkCommand, HomeworkEditorAction, HomeworkEditorEffect> {

    class Base(
        private val homeworkInteractor: HomeworkInteractor,
        private val organizationInteractor: OrganizationInteractor,
        private val subjectInteractor: SubjectInteractor,
        private val linkingClassInteractor: LinkingClassInteractor,
        private val dateManager: DateManager,
    ) : HomeworkEditorWorkProcessor {

        override suspend fun work(command: HomeworkEditorWorkCommand) = when (command) {
            is HomeworkEditorWorkCommand.LoadEditModel -> loadEditModelWork(
                homeworkId = command.homeworkId,
                date = command.date,
                subjectId = command.subjectId,
                organizationId = command.organizationId,
            )
            is HomeworkEditorWorkCommand.LoadOrganizations -> loadOrganizationsWork()
            is HomeworkEditorWorkCommand.LoadSubjects -> loadSubjectsWork(
                organizationId = command.organizationId,
            )
            is HomeworkEditorWorkCommand.LoadClassesForLinked -> loadClassesForLinkedWork(
                subjectId = command.subjectId,
                date = command.date,
                currentHomework = command.currentHomework,
            )
            is HomeworkEditorWorkCommand.DeleteHomework -> deleteHomeworkWork(
                editModel = command.editModel,
            )
            is HomeworkEditorWorkCommand.SaveHomework -> saveHomeworkWork(
                editModel = command.editModel,
            )
        }

        private fun loadEditModelWork(
            homeworkId: UID?,
            date: Instant?,
            subjectId: UID?,
            organizationId: UID?
        ) = flow {
            val homework = homeworkInteractor.fetchHomeworkById(homeworkId ?: "").firstRightOrNull {
                emit(EffectResult(HomeworkEditorEffect.ShowError(it)))
            }

            val editModel = if (homework != null) {
                homework.mapToUi().convertToEdit()
            } else {
                val subject = subjectId?.let { subjectId ->
                    subjectInteractor.fetchSubjectById(subjectId).firstOrNullHandleAndGet(
                        onLeftAction = { emit(EffectResult(HomeworkEditorEffect.ShowError(it))).let { null } },
                        onRightAction = { subject -> subject?.mapToUi() },
                    )
                }
                val homeworkOrganization = organizationId?.let { organizationId ->
                    organizationInteractor.fetchShortOrganizationById(organizationId).firstOrNullHandleAndGet(
                        onLeftAction = { emit(EffectResult(HomeworkEditorEffect.ShowError(it))).let { null } },
                        onRightAction = { organization -> organization.mapToUi() },
                    )
                }

                EditHomeworkUi.createEditModel(
                    uid = homeworkId,
                    organization = homeworkOrganization,
                    date = date?.startThisDay(),
                    subject = subject,
                )
            }

            emit(ActionResult(HomeworkEditorAction.UpdateEditModel(editModel)))
        }

        private fun loadOrganizationsWork() = flow {
            val organizationsFlow = organizationInteractor.fetchAllShortOrganizations()

            organizationsFlow.collectAndHandle(
                onLeftAction = { emit(EffectResult(HomeworkEditorEffect.ShowError(it))) },
                onRightAction = { organizationList ->
                    val organizations = organizationList.map { it.mapToUi() }
                    emit(ActionResult(HomeworkEditorAction.UpdateOrganizations(organizations)))
                },
            )
        }

        private fun loadSubjectsWork(organizationId: UID?) = flow {
            if (organizationId == null) {
                return@flow emit(ActionResult(HomeworkEditorAction.UpdateSubjects(emptyList())))
            }
            subjectInteractor.fetchAllSubjectsByOrganization(organizationId).collectAndHandle(
                onLeftAction = { emit(EffectResult(HomeworkEditorEffect.ShowError(it))) },
                onRightAction = { subjectList ->
                    val subjects = subjectList.map { it.mapToUi() }
                    emit(ActionResult(HomeworkEditorAction.UpdateSubjects(subjects)))
                },
            )
        }

        private fun loadClassesForLinkedWork(
            subjectId: UID?,
            date: Instant?,
            currentHomework: UID?,
        ) = flow<HomeworkEditorWorkResult> {
            if (subjectId == null) {
                return@flow emit(ActionResult(HomeworkEditorAction.UpdateClassesForLinked(emptyMap())))
            }
            val targetDate = date ?: dateManager.fetchBeginningCurrentInstant()
            linkingClassInteractor.fetchFreeClassesForHomework(subjectId, targetDate, currentHomework).collectAndHandle(
                onLeftAction = { emit(EffectResult(HomeworkEditorEffect.NavigateToBack)) },
                onRightAction = { classes ->
                    val classesForLinked = classes.mapValues { entry ->
                        entry.value.map { it.second.mapToUi(number = it.first) }
                    }
                    emit(ActionResult(HomeworkEditorAction.UpdateClassesForLinked(classesForLinked)))
                },
            )
        }.onStart {
            emit(ActionResult(HomeworkEditorAction.UpdateClassesLoading(true)))
        }

        private fun deleteHomeworkWork(editModel: EditHomeworkUi) = flow {
            homeworkInteractor.deleteHomework(editModel.uid).handle(
                onLeftAction = { emit(EffectResult(HomeworkEditorEffect.ShowError(it))) },
                onRightAction = { emit(EffectResult(HomeworkEditorEffect.NavigateToBack)) },
            )
        }

        private fun saveHomeworkWork(editModel: EditHomeworkUi) = flow {
            val homework = editModel.convertToBase().mapToDomain()
            homeworkInteractor.addOrUpdateHomework(homework).handle(
                onLeftAction = { emit(EffectResult(HomeworkEditorEffect.ShowError(it))) },
                onRightAction = { emit(EffectResult(HomeworkEditorEffect.NavigateToBack)) },
            )
        }
    }
}

internal sealed class HomeworkEditorWorkCommand : WorkCommand {
    data class LoadEditModel(
        val homeworkId: UID?,
        val date: Instant?,
        val subjectId: UID?,
        val organizationId: UID?,
    ) : HomeworkEditorWorkCommand()

    data object LoadOrganizations : HomeworkEditorWorkCommand()

    data class LoadSubjects(val organizationId: UID?) : HomeworkEditorWorkCommand()

    data class LoadClassesForLinked(
        val subjectId: UID?,
        val date: Instant?,
        val currentHomework: UID?,
    ) : HomeworkEditorWorkCommand()

    data class DeleteHomework(val editModel: EditHomeworkUi) : HomeworkEditorWorkCommand()

    data class SaveHomework(val editModel: EditHomeworkUi) : HomeworkEditorWorkCommand()
}

internal typealias HomeworkEditorWorkResult = WorkResult<HomeworkEditorAction, HomeworkEditorEffect>