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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.screenmodel

import kotlinx.coroutines.flow.flow
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.ActionResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.EffectResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.FlowWorkProcessor
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkCommand
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.firstOrNullHandleAndGet
import ru.aleshin.studyassistant.core.common.functional.handle
import ru.aleshin.studyassistant.core.common.functional.handleAndGet
import ru.aleshin.studyassistant.core.ui.mappers.mapToDomain
import ru.aleshin.studyassistant.core.ui.models.ActionWithAvatar
import ru.aleshin.studyassistant.editor.impl.domain.interactors.OrganizationInteractor
import ru.aleshin.studyassistant.editor.impl.presentation.mappers.mapToDomain
import ru.aleshin.studyassistant.editor.impl.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.orgnizations.EditOrganizationUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.orgnizations.convertToBase
import ru.aleshin.studyassistant.editor.impl.presentation.models.orgnizations.convertToEdit
import ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.contract.OrganizationAction
import ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.contract.OrganizationEffect

/**
 * @author Stanislav Aleshin on 08.07.2024.
 */
internal interface OrganizationWorkProcessor :
    FlowWorkProcessor<OrganizationWorkCommand, OrganizationAction, OrganizationEffect> {

    class Base(
        private val organizationInteractor: OrganizationInteractor,
    ) : OrganizationWorkProcessor {

        override suspend fun work(command: OrganizationWorkCommand) = when (command) {
            is OrganizationWorkCommand.LoadEditModel -> loadEditModelWork(command.organizationId)
            is OrganizationWorkCommand.SaveEditModel -> saveEditModelWork(command.editModel, command.actionWithAvatar)
            is OrganizationWorkCommand.HideOrganization -> hideOrganizationWork(command.editModel)
        }

        private fun loadEditModelWork(organizationId: UID?) = flow {
            val organization = if (!organizationId.isNullOrBlank()) {
                organizationInteractor.fetchOrganizationById(organizationId).firstOrNullHandleAndGet(
                    onLeftAction = { emit(EffectResult(OrganizationEffect.ShowError(it))).let { null } },
                    onRightAction = { organization -> organization.mapToUi() },
                )
            } else {
                null
            }
            val editModel = organization?.convertToEdit() ?: EditOrganizationUi.createEditModel(organizationId)
            emit(ActionResult(OrganizationAction.SetupEditModel(editModel)))
        }

        private fun saveEditModelWork(
            editModel: EditOrganizationUi,
            actionWithAvatar: ActionWithAvatar
        ) = flow {
            val uid = editModel.uid.takeIf { it.isNotBlank() } ?: randomUUID()

            val avatar = when (actionWithAvatar) {
                is ActionWithAvatar.Set -> {
                    organizationInteractor.uploadAvatar(uid, actionWithAvatar.file.mapToDomain()).handleAndGet(
                        onLeftAction = { emit(EffectResult(OrganizationEffect.ShowError(it))).let { null } },
                        onRightAction = { it },
                    )
                }
                is ActionWithAvatar.Delete -> {
                    organizationInteractor.deleteAvatar(uid).handleAndGet(
                        onLeftAction = { emit(EffectResult(OrganizationEffect.ShowError(it))).let { null } },
                        onRightAction = { null },
                    )
                }
                is ActionWithAvatar.None -> actionWithAvatar.uri
            }

            val updatedEditModel = editModel.copy(uid = uid, avatar = avatar)
            val organization = updatedEditModel.convertToBase().mapToDomain()

            organizationInteractor.addOrUpdateOrganization(organization).handle(
                onLeftAction = { emit(EffectResult(OrganizationEffect.ShowError(it))) },
                onRightAction = { emit(EffectResult(OrganizationEffect.NavigateToBack)) },
            )
        }

        private fun hideOrganizationWork(editModel: EditOrganizationUi) = flow {
            val updatedEditModel = editModel.copy(isHide = true)
            val organization = updatedEditModel.convertToBase().mapToDomain()
            organizationInteractor.addOrUpdateOrganization(organization).handle(
                onLeftAction = { emit(EffectResult(OrganizationEffect.ShowError(it))) },
                onRightAction = { emit(EffectResult(OrganizationEffect.NavigateToBack)) },
            )
        }
    }
}

internal sealed class OrganizationWorkCommand : WorkCommand {
    data class LoadEditModel(val organizationId: UID?) : OrganizationWorkCommand()

    data class SaveEditModel(
        val editModel: EditOrganizationUi,
        val actionWithAvatar: ActionWithAvatar,
    ) : OrganizationWorkCommand()

    data class HideOrganization(val editModel: EditOrganizationUi) : OrganizationWorkCommand()
}