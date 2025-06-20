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

package ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.screenmodel

import kotlinx.coroutines.flow.flow
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.ActionResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.EffectResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.FlowWorkProcessor
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkCommand
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkResult
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.collectAndHandle
import ru.aleshin.studyassistant.core.common.functional.handleAndGet
import ru.aleshin.studyassistant.info.impl.domain.interactors.AppUserInteractor
import ru.aleshin.studyassistant.info.impl.domain.interactors.ClassesInfoInteractor
import ru.aleshin.studyassistant.info.impl.domain.interactors.OrganizationsInteractor
import ru.aleshin.studyassistant.info.impl.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.contract.OrganizationsAction
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.contract.OrganizationsEffect

/**
 * @author Stanislav Aleshin on 16.06.2024.
 */
internal interface OrganizationsWorkProcessor :
    FlowWorkProcessor<OrganizationsWorkCommand, OrganizationsAction, OrganizationsEffect> {

    class Base(
        private val organizationsInteractor: OrganizationsInteractor,
        private val classesInfoInteractor: ClassesInfoInteractor,
        private val appUserInteractor: AppUserInteractor,
    ) : OrganizationsWorkProcessor {

        override suspend fun work(command: OrganizationsWorkCommand) = when (command) {
            is OrganizationsWorkCommand.LoadShortOrganizations -> loadShortOrganizationsWork()
            is OrganizationsWorkCommand.LoadPaidUserStatus -> loadPaidUserStatusWork()
            is OrganizationsWorkCommand.LoadOrganizationData -> loadOrganizationDataWork(command.organizationId)
        }

        private fun loadPaidUserStatusWork() = flow {
            appUserInteractor.fetchAppUserPaidStatus().collectAndHandle(
                onLeftAction = { emit(EffectResult(OrganizationsEffect.ShowError(it))) },
                onRightAction = {
                    emit(ActionResult(OrganizationsAction.UpdatePaidUserStatus(it)))
                }
            )
        }

        private fun loadShortOrganizationsWork() = flow {
            organizationsInteractor.fetchAllShortOrganizations().collectAndHandle(
                onLeftAction = { emit(EffectResult(OrganizationsEffect.ShowError(it))) },
                onRightAction = { organizationsList ->
                    val organizations = organizationsList.map { it.mapToUi() }
                    emit(ActionResult(OrganizationsAction.UpdateShortOrganizations(organizations)))
                }
            )
        }

        private fun loadOrganizationDataWork(organizationId: UID?) = flow<OrganizationWorkResult> {
            if (organizationId.isNullOrEmpty()) {
                return@flow emit(ActionResult(OrganizationsAction.UpdateOrganizationData(null, null)))
            } else {
                emit(ActionResult(OrganizationsAction.UpdateLoading(true)))
            }
            organizationsInteractor.fetchOrganizationById(organizationId).collectAndHandle(
                onLeftAction = { emit(EffectResult(OrganizationsEffect.ShowError(it))) },
                onRightAction = { organizationModel ->
                    val organization = organizationModel?.mapToUi()
                    val compactOrganization = organization?.copy(
                        subjects = organization.subjects.sortedBy { it.name },
                        employee = organization.employee.sortedBy { it.firstName },
                    )
                    val classesInfo = classesInfoInteractor.fetchClassesInfo(organizationId).handleAndGet(
                        onLeftAction = { emit(EffectResult(OrganizationsEffect.ShowError(it))).let { null } },
                        onRightAction = { classesInfo -> classesInfo.mapToUi() },
                    )
                    emit(ActionResult(OrganizationsAction.UpdateOrganizationData(compactOrganization, classesInfo)))
                },
            )
        }
    }
}

internal sealed class OrganizationsWorkCommand : WorkCommand {
    data object LoadShortOrganizations : OrganizationsWorkCommand()
    data object LoadPaidUserStatus : OrganizationsWorkCommand()
    data class LoadOrganizationData(val organizationId: UID?) : OrganizationsWorkCommand()
}

internal typealias OrganizationWorkResult = WorkResult<OrganizationsAction, OrganizationsEffect>