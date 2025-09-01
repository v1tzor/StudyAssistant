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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.store

import ru.aleshin.studyassistant.core.common.architecture.store.BaseComposeStore
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.EffectCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.StateCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.ui.mappers.convertToInputFile
import ru.aleshin.studyassistant.core.ui.models.ActionWithAvatar
import ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.contract.OrganizationAction
import ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.contract.OrganizationEffect
import ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.contract.OrganizationEvent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.contract.OrganizationInput
import ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.contract.OrganizationOutput
import ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.contract.OrganizationState

/**
 * @author Stanislav Aleshin on 08.07.2024
 */
internal class OrganizationComposeStore(
    private val workProcessor: OrganizationWorkProcessor,
    stateCommunicator: StateCommunicator<OrganizationState>,
    effectCommunicator: EffectCommunicator<OrganizationEffect>,
    coroutineManager: CoroutineManager,
) : BaseComposeStore<OrganizationState, OrganizationEvent, OrganizationAction, OrganizationEffect, OrganizationInput, OrganizationOutput>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun initialize(input: OrganizationInput, isRestore: Boolean) {
        if (!isRestore) {
            dispatchEvent(OrganizationEvent.Started(input))
        }
    }

    override suspend fun WorkScope<OrganizationState, OrganizationAction, OrganizationEffect, OrganizationOutput>.handleEvent(
        event: OrganizationEvent,
    ) {
        when (event) {
            is OrganizationEvent.Started -> with(event) {
                launchBackgroundWork(BackgroundKey.LOAD_ORGANIZATION) {
                    val command = OrganizationWorkCommand.LoadEditModel(inputData.organizationId)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is OrganizationEvent.UpdateAvatar -> with(event) {
                val inputFile = image.convertToInputFile()
                sendAction(OrganizationAction.UpdateActionWithAvatar(ActionWithAvatar.Set(inputFile)))
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
                consumeOutput(OrganizationOutput.NavigateToBack)
            }
        }
    }

    override suspend fun reduce(
        action: OrganizationAction,
        currentState: OrganizationState,
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

    class Factory(
        private val workProcessor: OrganizationWorkProcessor,
        private val coroutineManager: CoroutineManager,
    ) : BaseComposeStore.Factory<OrganizationComposeStore, OrganizationState> {

        override fun create(savedState: OrganizationState): OrganizationComposeStore {
            return OrganizationComposeStore(
                workProcessor = workProcessor,
                stateCommunicator = StateCommunicator.Default(savedState),
                effectCommunicator = EffectCommunicator.Default(),
                coroutineManager = coroutineManager,
            )
        }
    }
}