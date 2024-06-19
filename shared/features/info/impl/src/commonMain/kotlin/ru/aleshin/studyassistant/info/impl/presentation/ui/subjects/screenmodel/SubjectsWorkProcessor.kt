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

package ru.aleshin.studyassistant.info.impl.presentation.ui.subjects.screenmodel

import architecture.screenmodel.work.ActionResult
import architecture.screenmodel.work.EffectResult
import architecture.screenmodel.work.FlowWorkProcessor
import architecture.screenmodel.work.WorkCommand
import architecture.screenmodel.work.WorkResult
import functional.UID
import functional.collectAndHandle
import functional.handle
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import ru.aleshin.studyassistant.info.impl.domain.interactors.OrganizationsInteractor
import ru.aleshin.studyassistant.info.impl.domain.interactors.SubjectsInteractor
import ru.aleshin.studyassistant.info.impl.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.info.impl.presentation.models.subjects.SubjectSortedType
import ru.aleshin.studyassistant.info.impl.presentation.models.subjects.SubjectUi
import ru.aleshin.studyassistant.info.impl.presentation.ui.subjects.contract.SubjectsAction
import ru.aleshin.studyassistant.info.impl.presentation.ui.subjects.contract.SubjectsEffect

/**
 * @author Stanislav Aleshin on 17.06.2024.
 */
internal interface SubjectsWorkProcessor :
    FlowWorkProcessor<SubjectsWorkCommand, SubjectsAction, SubjectsEffect> {

    class Base(
        private val subjectsInteractor: SubjectsInteractor,
        private val organizationsInteractor: OrganizationsInteractor,
    ) : SubjectsWorkProcessor {
        override suspend fun work(command: SubjectsWorkCommand) = when (command) {
            is SubjectsWorkCommand.LoadOrganizations -> loadOrganizationsWork(command.organization)
            is SubjectsWorkCommand.LoadSubjects -> loadSubjectsWork(command.organization, command.sortedType)
            is SubjectsWorkCommand.SortSubjects -> sortSubjectsWork(command.subjects, command.sortedType)
            is SubjectsWorkCommand.SearchSubjects -> searchSubjectsWork(command.query, command.organization, command.sortedType)
            is SubjectsWorkCommand.DeleteSubject -> deleteSubjectWork(command.targetId)
        }

        private fun loadOrganizationsWork(selectedOrganization: UID) = flow {
            organizationsInteractor.fetchAllShortOrganizations().collectAndHandle(
                onLeftAction = { emit(EffectResult(SubjectsEffect.ShowError(it))) },
                onRightAction = { organizationList ->
                    val organizations = organizationList.map { it.mapToUi() }
                    emit(ActionResult(SubjectsAction.UpdateOrganizations(selectedOrganization, organizations)))
                },
            )
        }

        private fun loadSubjectsWork(organization: UID, sortedType: SubjectSortedType) = flow<SubjectsWorkResult> {
            subjectsInteractor.fetchSubjectsByOrganization(organization).collectAndHandle(
                onLeftAction = { emit(EffectResult(SubjectsEffect.ShowError(it))) },
                onRightAction = { subjectList ->
                    val subjects = subjectList.map { it.mapToUi() }
                    val sortedSubjects = subjects.sortSubjectsByType(sortedType)
                    emit(ActionResult(SubjectsAction.UpdateSubjects(sortedSubjects, sortedType)))
                },
            )
        }.onStart {
            emit(ActionResult(SubjectsAction.UpdateLoading(true)))
        }

        private fun sortSubjectsWork(subjects: List<SubjectUi>, sortedType: SubjectSortedType) = flow {
            val sortedSubjects = subjects.sortSubjectsByType(sortedType)
            emit(ActionResult(SubjectsAction.UpdateSubjects(sortedSubjects, sortedType)))
        }

        private fun searchSubjectsWork(query: String, organization: UID, sortedType: SubjectSortedType) = flow {
            subjectsInteractor.fetchSubjectsByOrganization(organization).collectAndHandle(
                onLeftAction = { emit(EffectResult(SubjectsEffect.ShowError(it))) },
                onRightAction = { subjectList ->
                    val subjects = subjectList.map { it.mapToUi() }
                    val searchedSubjects = subjects.filter { subject ->
                        if (query.isNotBlank()) subject.name.contains(query, true) else true
                    }
                    val sortedSubjects = searchedSubjects.sortSubjectsByType(sortedType)
                    emit(ActionResult(SubjectsAction.UpdateSubjects(sortedSubjects, sortedType)))
                },
            )
        }

        private fun deleteSubjectWork(targetId: UID) = flow {
            subjectsInteractor.deleteSubjectById(targetId).handle(
                onLeftAction = { emit(EffectResult(SubjectsEffect.ShowError(it))) },
            )
        }

        private fun List<SubjectUi>.sortSubjectsByType(type: SubjectSortedType) = sortedBy { subject ->
            when (type) {
                SubjectSortedType.ALPHABETIC -> subject.name
                SubjectSortedType.TEACHER -> subject.teacher?.uid
                SubjectSortedType.EVENT_TYPE -> subject.eventType.toString()
                SubjectSortedType.OFFICE -> subject.office
                SubjectSortedType.LOCATION -> subject.location?.value
            }
        }
    }
}

internal sealed class SubjectsWorkCommand : WorkCommand {
    data class LoadOrganizations(val organization: UID) : SubjectsWorkCommand()
    data class LoadSubjects(val organization: UID, val sortedType: SubjectSortedType) : SubjectsWorkCommand()
    data class SortSubjects(val subjects: List<SubjectUi>, val sortedType: SubjectSortedType) : SubjectsWorkCommand()
    data class SearchSubjects(val query: String, val organization: UID, val sortedType: SubjectSortedType) : SubjectsWorkCommand()
    data class DeleteSubject(val targetId: UID) : SubjectsWorkCommand()
}

internal typealias SubjectsWorkResult = WorkResult<SubjectsAction, SubjectsEffect>