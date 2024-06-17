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

import architecture.screenmodel.work.ActionResult
import architecture.screenmodel.work.EffectResult
import architecture.screenmodel.work.FlowWorkProcessor
import architecture.screenmodel.work.WorkCommand
import architecture.screenmodel.work.WorkResult
import functional.UID
import functional.collectAndHandle
import functional.handleAndGet
import kotlinx.coroutines.flow.flow
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
    ) : OrganizationsWorkProcessor {

        override suspend fun work(command: OrganizationsWorkCommand) = when (command) {
            is OrganizationsWorkCommand.LoadShortOrganizations -> loadShortOrganizationsWork()
            is OrganizationsWorkCommand.LoadOrganizationData -> loadOrganizationDataWork(command.organizationId)
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
                    val classesInfo = classesInfoInteractor.fetchClassesInfo(organizationId).handleAndGet(
                        onLeftAction = { emit(EffectResult(OrganizationsEffect.ShowError(it))).let { null } },
                        onRightAction = { classesInfo -> classesInfo.mapToUi() },
                    )
                    emit(ActionResult(OrganizationsAction.UpdateOrganizationData(organization, classesInfo)))
                },
            )
        }
    }
}

internal sealed class OrganizationsWorkCommand : WorkCommand {
    data object LoadShortOrganizations : OrganizationsWorkCommand()
    data class LoadOrganizationData(val organizationId: UID?) : OrganizationsWorkCommand()
}

internal typealias OrganizationWorkResult = WorkResult<OrganizationsAction, OrganizationsEffect>