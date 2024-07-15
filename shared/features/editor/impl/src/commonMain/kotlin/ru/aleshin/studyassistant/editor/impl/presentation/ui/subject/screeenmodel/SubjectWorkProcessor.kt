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

import kotlinx.coroutines.flow.flow
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.ActionResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.EffectResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.FlowWorkProcessor
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkCommand
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.collectAndHandle
import ru.aleshin.studyassistant.core.common.functional.firstOrNullHandleAndGet
import ru.aleshin.studyassistant.core.common.functional.handle
import ru.aleshin.studyassistant.editor.impl.domain.interactors.EmployeeInteractor
import ru.aleshin.studyassistant.editor.impl.domain.interactors.OrganizationInteractor
import ru.aleshin.studyassistant.editor.impl.domain.interactors.SubjectInteractor
import ru.aleshin.studyassistant.editor.impl.presentation.mappers.mapToDomain
import ru.aleshin.studyassistant.editor.impl.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.orgnizations.OrganizationShortUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.subjects.EditSubjectUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.subjects.convertToBase
import ru.aleshin.studyassistant.editor.impl.presentation.models.subjects.convertToEditModel
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.ContactInfoUi
import ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.contract.SubjectAction
import ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.contract.SubjectEffect

/**
 * @author Stanislav Aleshin on 05.06.2024.
 */
internal interface SubjectWorkProcessor :
    FlowWorkProcessor<SubjectWorkCommand, SubjectAction, SubjectEffect> {

    class Base(
        private val subjectInteractor: SubjectInteractor,
        private val employeeInteractor: EmployeeInteractor,
        private val organizationInteractor: OrganizationInteractor,
    ) : SubjectWorkProcessor {

        override suspend fun work(command: SubjectWorkCommand) = when (command) {
            is SubjectWorkCommand.LoadEditModel -> loadEditModelWork(
                organizationId = command.organizationId,
                subjectId = command.subjectId,
            )
            is SubjectWorkCommand.LoadOrganization -> loadOrganizationWork(
                organizationId = command.organizationId,
            )
            is SubjectWorkCommand.LoadEmployees -> loadEmployeesWork(
                organizationId = command.organizationId
            )
            is SubjectWorkCommand.UpdateOrganizationOffices -> updateOfficesWork(
                organization = command.organization,
                offices = command.offices,
            )
            is SubjectWorkCommand.UpdateOrganizationLocations -> updateLocationsWork(
                organization = command.organization,
                locations = command.locations,
            )
            is SubjectWorkCommand.SaveEditModel -> saveEditModelWork(
                editableSubject = command.editableSubject,
            )
        }

        private fun loadEditModelWork(subjectId: UID?, organizationId: UID) = flow {
            val subject = subjectInteractor.fetchSubjectById(subjectId ?: "").firstOrNullHandleAndGet(
                onLeftAction = { emit(EffectResult(SubjectEffect.ShowError(it))).let { null } },
                onRightAction = { subject -> subject?.mapToUi() }
            )
            val editModel = subject?.convertToEditModel() ?: EditSubjectUi.createEditModel(
                uid = subjectId,
                organizationId = organizationId,
            )
            emit(ActionResult(SubjectAction.SetupEditModel(editModel)))
        }

        private fun loadOrganizationWork(organizationId: UID) = flow {
            organizationInteractor.fetchShortOrganizationById(organizationId).collectAndHandle(
                onLeftAction = { emit(EffectResult(SubjectEffect.ShowError(it))) },
                onRightAction = { organization ->
                    emit(ActionResult(SubjectAction.UpdateOrganization(organization.mapToUi())))
                },
            )
        }

        private fun loadEmployeesWork(organizationId: UID) = flow {
            employeeInteractor.fetchAllDetailsEmployee(organizationId).collectAndHandle(
                onLeftAction = { emit(EffectResult(SubjectEffect.ShowError(it))) },
                onRightAction = { employeeList ->
                    val employees = employeeList.map { it.mapToUi() }
                    emit(ActionResult(SubjectAction.UpdateEmployees(employees)))
                },
            )
        }

        private fun updateOfficesWork(organization: OrganizationShortUi, offices: List<String>) = flow {
            val updatedOrganization = organization.copy(offices = offices)
            organizationInteractor.updateShortOrganization(updatedOrganization.mapToDomain()).handle(
                onLeftAction = { emit(EffectResult(SubjectEffect.ShowError(it))) },
            )
        }

        private fun updateLocationsWork(organization: OrganizationShortUi, locations: List<ContactInfoUi>) = flow {
            val updatedOrganization = organization.copy(locations = locations)
            organizationInteractor.updateShortOrganization(updatedOrganization.mapToDomain()).handle(
                onLeftAction = { emit(EffectResult(SubjectEffect.ShowError(it))) },
            )
        }

        private fun saveEditModelWork(editableSubject: EditSubjectUi) = flow {
            val subject = editableSubject.convertToBase().mapToDomain()
            subjectInteractor.addOrUpdateSubject(subject).handle(
                onLeftAction = { emit(EffectResult(SubjectEffect.ShowError(it))) },
                onRightAction = { emit(EffectResult(SubjectEffect.NavigateToBack)) },
            )
        }
    }
}

internal sealed class SubjectWorkCommand : WorkCommand {
    data class LoadEditModel(val subjectId: UID?, val organizationId: UID) : SubjectWorkCommand()
    data class LoadOrganization(val organizationId: UID) : SubjectWorkCommand()
    data class LoadEmployees(val organizationId: UID) : SubjectWorkCommand()
    data class SaveEditModel(val editableSubject: EditSubjectUi) : SubjectWorkCommand()

    data class UpdateOrganizationOffices(
        val organization: OrganizationShortUi,
        val offices: List<String>
    ) : SubjectWorkCommand()

    data class UpdateOrganizationLocations(
        val organization: OrganizationShortUi,
        val locations: List<ContactInfoUi>
    ) : SubjectWorkCommand()
}