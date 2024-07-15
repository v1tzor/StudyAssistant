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

import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.ActionResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.EffectResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.FlowWorkProcessor
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkCommand
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkResult
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.collectAndHandle
import ru.aleshin.studyassistant.core.common.functional.firstOrNullHandleAndGet
import ru.aleshin.studyassistant.core.common.functional.firstRightOrNull
import ru.aleshin.studyassistant.core.common.functional.handle
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.editor.impl.domain.interactors.HomeworkInteractor
import ru.aleshin.studyassistant.editor.impl.domain.interactors.LinkingClassInteractor
import ru.aleshin.studyassistant.editor.impl.domain.interactors.OrganizationInteractor
import ru.aleshin.studyassistant.editor.impl.domain.interactors.SubjectInteractor
import ru.aleshin.studyassistant.editor.impl.presentation.mappers.mapToDomain
import ru.aleshin.studyassistant.editor.impl.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.tasks.EditHomeworkUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.tasks.convertToBase
import ru.aleshin.studyassistant.editor.impl.presentation.models.tasks.convertToEdit
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.contarct.HomeworkAction
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.contarct.HomeworkEffect

/**
 * @author Stanislav Aleshin on 22.06.2024.
 */
internal interface HomeworkWorkProcessor :
    FlowWorkProcessor<HomeworkWorkCommand, HomeworkAction, HomeworkEffect> {

    class Base(
        private val homeworkInteractor: HomeworkInteractor,
        private val organizationInteractor: OrganizationInteractor,
        private val subjectInteractor: SubjectInteractor,
        private val linkingClassInteractor: LinkingClassInteractor,
        private val dateManager: DateManager,
    ) : HomeworkWorkProcessor {

        override suspend fun work(command: HomeworkWorkCommand) = when (command) {
            is HomeworkWorkCommand.LoadEditModel -> loadEditModelWork(
                homeworkId = command.homeworkId,
                date = command.date,
                subjectId = command.subjectId,
                organizationId = command.organizationId,
            )
            is HomeworkWorkCommand.LoadOrganizations -> loadOrganizationsWork()
            is HomeworkWorkCommand.LoadSubjects -> loadSubjectsWork(
                organizationId = command.organizationId,
            )
            is HomeworkWorkCommand.LoadClassesForLinked -> loadClassesForLinkedWork(
                subjectId = command.subjectId,
                date = command.date,
                currentHomework = command.currentHomework,
            )
            is HomeworkWorkCommand.DeleteHomework -> deleteHomeworkWork(
                editModel = command.editModel,
            )
            is HomeworkWorkCommand.SaveHomework -> saveHomeworkWork(
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
                emit(EffectResult(HomeworkEffect.ShowError(it)))
            }

            val editModel = if (homework != null) {
                homework.mapToUi().convertToEdit()
            } else {
                val subject = subjectId?.let { subjectId ->
                    subjectInteractor.fetchSubjectById(subjectId).firstOrNullHandleAndGet(
                        onLeftAction = { emit(EffectResult(HomeworkEffect.ShowError(it))).let { null } },
                        onRightAction = { subject -> subject?.mapToUi() },
                    )
                }
                val homeworkOrganization = organizationId?.let { organizationId ->
                    organizationInteractor.fetchShortOrganizationById(organizationId).firstOrNullHandleAndGet(
                        onLeftAction = { emit(EffectResult(HomeworkEffect.ShowError(it))).let { null } },
                        onRightAction = { organization -> organization.mapToUi() },
                    )
                }

                EditHomeworkUi.createEditModel(
                    uid = homeworkId,
                    organization = homeworkOrganization,
                    deadline = date?.startThisDay(),
                    subject = subject,
                )
            }
            emit(ActionResult(HomeworkAction.SetupEditModel(editModel)))
        }

        private fun loadOrganizationsWork() = flow {
            organizationInteractor.fetchAllShortOrganizations().collectAndHandle(
                onLeftAction = { emit(EffectResult(HomeworkEffect.ShowError(it))) },
                onRightAction = { organizationList ->
                    val organizations = organizationList.map { it.mapToUi() }
                    emit(ActionResult(HomeworkAction.UpdateOrganizations(organizations)))
                },
            )
        }

        private fun loadSubjectsWork(organizationId: UID?) = flow {
            if (organizationId == null) {
                return@flow emit(ActionResult(HomeworkAction.UpdateSubjects(emptyList())))
            }
            subjectInteractor.fetchAllSubjectsByOrganization(organizationId).collectAndHandle(
                onLeftAction = { emit(EffectResult(HomeworkEffect.ShowError(it))) },
                onRightAction = { subjectList ->
                    val subjects = subjectList.map { it.mapToUi() }
                    emit(ActionResult(HomeworkAction.UpdateSubjects(subjects)))
                },
            )
        }

        private fun loadClassesForLinkedWork(
            subjectId: UID?,
            date: Instant?,
            currentHomework: UID?,
        ) = flow<HomeworkWorkResult> {
            if (subjectId == null) {
                return@flow emit(ActionResult(HomeworkAction.UpdateClassesForLinked(emptyMap())))
            }
            val targetDate = date ?: dateManager.fetchBeginningCurrentInstant()
            linkingClassInteractor.fetchFreeClassesForHomework(subjectId, targetDate, currentHomework).collectAndHandle(
                onLeftAction = { emit(EffectResult(HomeworkEffect.NavigateToBack)) },
                onRightAction = { classes ->
                    val classesForLinked = classes.mapValues { entry ->
                        entry.value.map { it.second.mapToUi(number = it.first) }
                    }
                    emit(ActionResult(HomeworkAction.UpdateClassesForLinked(classesForLinked)))
                },
            )
        }.onStart {
            emit(ActionResult(HomeworkAction.UpdateClassesLoading(true)))
        }

        private fun deleteHomeworkWork(editModel: EditHomeworkUi) = flow {
            homeworkInteractor.deleteHomework(editModel.uid).handle(
                onLeftAction = { emit(EffectResult(HomeworkEffect.ShowError(it))) },
                onRightAction = { emit(EffectResult(HomeworkEffect.NavigateToBack)) },
            )
        }

        private fun saveHomeworkWork(editModel: EditHomeworkUi) = flow {
            val homework = editModel.convertToBase().mapToDomain()
            homeworkInteractor.addOrUpdateHomework(homework).handle(
                onLeftAction = { emit(EffectResult(HomeworkEffect.ShowError(it))) },
                onRightAction = { emit(EffectResult(HomeworkEffect.NavigateToBack)) },
            )
        }
    }
}

internal sealed class HomeworkWorkCommand : WorkCommand {
    data class LoadEditModel(
        val homeworkId: UID?,
        val date: Instant?,
        val subjectId: UID?,
        val organizationId: UID?,
    ) : HomeworkWorkCommand()

    data object LoadOrganizations : HomeworkWorkCommand()

    data class LoadSubjects(val organizationId: UID?) : HomeworkWorkCommand()

    data class LoadClassesForLinked(
        val subjectId: UID?,
        val date: Instant?,
        val currentHomework: UID?,
    ) : HomeworkWorkCommand()

    data class DeleteHomework(val editModel: EditHomeworkUi) : HomeworkWorkCommand()

    data class SaveHomework(val editModel: EditHomeworkUi) : HomeworkWorkCommand()
}

internal typealias HomeworkWorkResult = WorkResult<HomeworkAction, HomeworkEffect>