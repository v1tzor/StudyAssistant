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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.screeenmodel

import architecture.screenmodel.work.EffectResult
import architecture.screenmodel.work.FlowWorkProcessor
import architecture.screenmodel.work.WorkCommand
import functional.UID
import functional.handle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import ru.aleshin.studyassistant.editor.impl.domain.interactors.OrganizationInteractor
import ru.aleshin.studyassistant.editor.impl.domain.interactors.SubjectInteractor
import ru.aleshin.studyassistant.editor.impl.presentation.mappers.mapToDomain
import ru.aleshin.studyassistant.editor.impl.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.orgnizations.OrganizationUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.orgnizations.convertToShort
import ru.aleshin.studyassistant.editor.impl.presentation.models.subjects.EditSubjectUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.subjects.convertToBase
import ru.aleshin.studyassistant.editor.impl.presentation.models.subjects.convertToEditModel
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.ContactInfoUi
import ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.contract.SubjectEditorAction
import ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.contract.SubjectEditorEffect

/**
 * @author Stanislav Aleshin on 05.06.2024.
 */
internal interface SubjectEditorWorkProcessor :
    FlowWorkProcessor<SubjectEditorWorkCommand, SubjectEditorAction, SubjectEditorEffect> {

    class Base(
        private val subjectInteractor: SubjectInteractor,
        private val organizationInteractor: OrganizationInteractor,
    ) : SubjectEditorWorkProcessor {

        override suspend fun work(command: SubjectEditorWorkCommand) = when (command) {
            is SubjectEditorWorkCommand.LoadEditModel -> loadEditModelWork(command.subjectId, command.organizationId)
            is SubjectEditorWorkCommand.SaveEditModel -> saveEditModelWork(command.editableSubject)
            is SubjectEditorWorkCommand.UpdateOffices -> updateOfficesWork(command.organization, command.offices)
            is SubjectEditorWorkCommand.UpdateLocations -> updateLocationsWork(command.organization, command.locations)
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        private fun loadEditModelWork(subjectId: UID?, organizationId: UID) = flow {
            val organizationsFlow = organizationInteractor.fetchOrganizationById(organizationId)
            val subjectFlow = subjectInteractor.fetchSubjectById(subjectId ?: "")

            subjectFlow.flatMapLatestWithResult(
                secondFlow = organizationsFlow,
                onError = { SubjectEditorEffect.ShowError(it) },
                onData = { subjectModel, organizationModel ->
                    val organization = organizationModel.mapToUi()
                    val subject = subjectModel?.mapToUi()
                    val editModel = subject?.convertToEditModel() ?: EditSubjectUi.createEditModel(
                        uid = subjectId,
                        organizationId = organizationId,
                    )
                    SubjectEditorAction.SetupEditModel(editModel, organization)
                },
            ).collect { result ->
                emit(result)
            }
        }

        private fun saveEditModelWork(editableSubject: EditSubjectUi) = flow {
            val subject = editableSubject.convertToBase().mapToDomain()
            subjectInteractor.addOrUpdateSubject(subject).handle(
                onLeftAction = { emit(EffectResult(SubjectEditorEffect.ShowError(it))) },
                onRightAction = { emit(EffectResult(SubjectEditorEffect.NavigateToBack)) },
            )
        }

        private fun updateOfficesWork(organization: OrganizationUi, offices: List<String>) = flow {
            val updatedOrganization = organization.convertToShort().copy(offices = offices)
            organizationInteractor.updateShortOrganization(updatedOrganization.mapToDomain()).handle(
                onLeftAction = { emit(EffectResult(SubjectEditorEffect.ShowError(it))) },
            )
        }

        private fun updateLocationsWork(organization: OrganizationUi, locations: List<ContactInfoUi>) = flow {
            val updatedOrganization = organization.convertToShort().copy(locations = locations)
            organizationInteractor.updateShortOrganization(updatedOrganization.mapToDomain()).handle(
                onLeftAction = { emit(EffectResult(SubjectEditorEffect.ShowError(it))) },
            )
        }
    }
}

internal sealed class SubjectEditorWorkCommand : WorkCommand {
    data class LoadEditModel(val subjectId: UID?, val organizationId: UID) : SubjectEditorWorkCommand()
    data class SaveEditModel(val editableSubject: EditSubjectUi) : SubjectEditorWorkCommand()
    data class UpdateOffices(val organization: OrganizationUi, val offices: List<String>) : SubjectEditorWorkCommand()
    data class UpdateLocations(
        val organization: OrganizationUi,
        val locations: List<ContactInfoUi>
    ) : SubjectEditorWorkCommand()
}
