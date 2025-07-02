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

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.BaseScreenModel
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.ui.models.ActionWithAvatar
import ru.aleshin.studyassistant.editor.impl.di.holder.EditorFeatureDIHolder
import ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.contract.OrganizationAction
import ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.contract.OrganizationDeps
import ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.contract.OrganizationEffect
import ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.contract.OrganizationEvent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.contract.OrganizationViewState

/**
 * @author Stanislav Aleshin on 08.07.2024
 */
internal class OrganizationScreenModel(
    private val workProcessor: OrganizationWorkProcessor,
    stateCommunicator: OrganizationStateCommunicator,
    effectCommunicator: OrganizationEffectCommunicator,
    coroutineManager: CoroutineManager,
) : BaseScreenModel<OrganizationViewState, OrganizationEvent, OrganizationAction, OrganizationEffect, OrganizationDeps>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun init(deps: OrganizationDeps) {
        if (!isInitialize) {
            super.init(deps)
            dispatchEvent(OrganizationEvent.Init(deps.organizationId))
        }
    }

    override suspend fun WorkScope<OrganizationViewState, OrganizationAction, OrganizationEffect>.handleEvent(
        event: OrganizationEvent,
    ) {
        when (event) {
            is OrganizationEvent.Init -> {
                launchBackgroundWork(BackgroundKey.LOAD_ORGANIZATION) {
                    val command = OrganizationWorkCommand.LoadEditModel(event.organizationId)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is OrganizationEvent.UpdateAvatar -> with(event) {
                sendAction(OrganizationAction.UpdateActionWithAvatar(ActionWithAvatar.Set(image)))
            }
            is OrganizationEvent.DeleteAvatar -> with(state()) {
                val action = if (editableOrganization?.avatar != null) {
                    ActionWithAvatar.Delete
                } else {
                    ActionWithAvatar.None(null)
                }
                sendAction(OrganizationAction.UpdateActionWithAvatar(action))
            }
            is OrganizationEvent.UpdateType -> with(state()) {
                val updatedOrganization = editableOrganization?.copy(type = event.organizationType)
                sendAction(OrganizationAction.UpdateEditModel(updatedOrganization))
            }
            is OrganizationEvent.UpdateName -> with(state()) {
                val updatedOrganization = editableOrganization?.copy(
                    shortName = event.shortName,
                    fullName = event.fullName,
                )
                sendAction(OrganizationAction.UpdateEditModel(updatedOrganization))
            }
            is OrganizationEvent.UpdateEmails -> with(state()) {
                val updatedOrganization = editableOrganization?.copy(emails = event.emails)
                sendAction(OrganizationAction.UpdateEditModel(updatedOrganization))
            }
            is OrganizationEvent.UpdateLocations -> with(state()) {
                val updatedOrganization = editableOrganization?.copy(locations = event.locations)
                sendAction(OrganizationAction.UpdateEditModel(updatedOrganization))
            }
            is OrganizationEvent.UpdatePhones -> with(state()) {
                val updatedOrganization = editableOrganization?.copy(phones = event.phones)
                sendAction(OrganizationAction.UpdateEditModel(updatedOrganization))
            }
            is OrganizationEvent.UpdateWebs -> with(state()) {
                val updatedOrganization = editableOrganization?.copy(webs = event.webs)
                sendAction(OrganizationAction.UpdateEditModel(updatedOrganization))
            }
            is OrganizationEvent.UpdateStatus -> with(state()) {
                val updatedOrganization = editableOrganization?.copy(isMain = event.isMain)
                sendAction(OrganizationAction.UpdateEditModel(updatedOrganization))
            }
            is OrganizationEvent.SaveOrganization -> with(state()) {
                launchBackgroundWork(BackgroundKey.SAVE_ORGANIZATION) {
                    val editModel = checkNotNull(editableOrganization)
                    val command = OrganizationWorkCommand.SaveEditModel(editModel, actionWithAvatar)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is OrganizationEvent.HideOrganization -> with(state()) {
                launchBackgroundWork(BackgroundKey.HIDE_ORGANIZATION) {
                    val editModel = checkNotNull(editableOrganization)
                    val command = OrganizationWorkCommand.HideOrganization(editModel)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is OrganizationEvent.NavigateToBack -> {
                sendEffect(OrganizationEffect.NavigateToBack)
            }
        }
    }

    override suspend fun reduce(
        action: OrganizationAction,
        currentState: OrganizationViewState,
    ) = when (action) {
        is OrganizationAction.SetupEditModel -> currentState.copy(
            editableOrganization = action.editModel,
            actionWithAvatar = ActionWithAvatar.None(action.editModel.avatar),
            isLoading = false,
        )
        is OrganizationAction.UpdateEditModel -> currentState.copy(
            editableOrganization = action.editModel,
        )
        is OrganizationAction.UpdateActionWithAvatar -> currentState.copy(
            actionWithAvatar = action.action,
        )
        is OrganizationAction.UpdateLoading -> currentState.copy(
            isLoading = action.isLoading,
        )
    }

    enum class BackgroundKey : BackgroundWorkKey {
        LOAD_ORGANIZATION, SAVE_ORGANIZATION, HIDE_ORGANIZATION
    }
}

@Composable
internal fun Screen.rememberOrganizationScreenModel(): OrganizationScreenModel {
    val di = EditorFeatureDIHolder.fetchDI()
    return rememberScreenModel { di.instance<OrganizationScreenModel>() }
}