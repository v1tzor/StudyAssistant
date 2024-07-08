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
import ru.aleshin.studyassistant.editor.impl.di.holder.EditorFeatureDIHolder
import ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.contract.OrganizationEditorAction
import ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.contract.OrganizationEditorDeps
import ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.contract.OrganizationEditorEffect
import ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.contract.OrganizationEditorEvent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.contract.OrganizationEditorViewState

/**
 * @author Stanislav Aleshin on 08.07.2024
 */
internal class OrganizationEditorScreenModel(
    private val workProcessor: OrganizationEditorWorkProcessor,
    stateCommunicator: OrganizationEditorStateCommunicator,
    effectCommunicator: OrganizationEditorEffectCommunicator,
    coroutineManager: CoroutineManager,
) : BaseScreenModel<
    OrganizationEditorViewState,
    OrganizationEditorEvent,
    OrganizationEditorAction,
    OrganizationEditorEffect,
    OrganizationEditorDeps
    >(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun init(deps: OrganizationEditorDeps) {
        if (!isInitialize) {
            super.init(deps)
            dispatchEvent(OrganizationEditorEvent.Init(deps.organizationId))
        }
    }

    override suspend fun WorkScope<OrganizationEditorViewState, OrganizationEditorAction, OrganizationEditorEffect>.handleEvent(
        event: OrganizationEditorEvent,
    ) {
        when (event) {
            is OrganizationEditorEvent.Init -> {
                launchBackgroundWork(BackgroundKey.LOAD_ORGANIZATION) {
                    val command = OrganizationEditorWorkCommand.LoadEditModel(event.organizationId)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is OrganizationEditorEvent.UpdateAvatar -> with(state()) {
                val updatedOrganization = editableOrganization?.copy(avatar = event.avatarUrl)
                sendAction(OrganizationEditorAction.UpdateEditModel(updatedOrganization))
            }
            is OrganizationEditorEvent.UpdateType -> with(state()) {
                val updatedOrganization = editableOrganization?.copy(type = event.organizationType)
                sendAction(OrganizationEditorAction.UpdateEditModel(updatedOrganization))
            }
            is OrganizationEditorEvent.UpdateName -> with(state()) {
                val updatedOrganization = editableOrganization?.copy(
                    shortName = event.shortName,
                    fullName = event.fullName,
                )
                sendAction(OrganizationEditorAction.UpdateEditModel(updatedOrganization))
            }
            is OrganizationEditorEvent.UpdateEmails -> with(state()) {
                val updatedOrganization = editableOrganization?.copy(emails = event.emails)
                sendAction(OrganizationEditorAction.UpdateEditModel(updatedOrganization))
            }
            is OrganizationEditorEvent.UpdateLocations -> with(state()) {
                val updatedOrganization = editableOrganization?.copy(locations = event.locations)
                sendAction(OrganizationEditorAction.UpdateEditModel(updatedOrganization))
            }
            is OrganizationEditorEvent.UpdatePhones -> with(state()) {
                val updatedOrganization = editableOrganization?.copy(phones = event.phones)
                sendAction(OrganizationEditorAction.UpdateEditModel(updatedOrganization))
            }
            is OrganizationEditorEvent.UpdateWebs -> with(state()) {
                val updatedOrganization = editableOrganization?.copy(webs = event.webs)
                sendAction(OrganizationEditorAction.UpdateEditModel(updatedOrganization))
            }
            is OrganizationEditorEvent.UpdateStatus -> with(state()) {
                val updatedOrganization = editableOrganization?.copy(isMain = event.isMain)
                sendAction(OrganizationEditorAction.UpdateEditModel(updatedOrganization))
            }
            is OrganizationEditorEvent.SaveOrganization -> with(state()) {
                launchBackgroundWork(BackgroundKey.SAVE_ORGANIZATION) {
                    val editModel = checkNotNull(editableOrganization)
                    val command = OrganizationEditorWorkCommand.SaveEditModel(editModel)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is OrganizationEditorEvent.NavigateToBack -> {
                sendEffect(OrganizationEditorEffect.NavigateToBack)
            }
        }
    }

    override suspend fun reduce(
        action: OrganizationEditorAction,
        currentState: OrganizationEditorViewState,
    ) = when (action) {
        is OrganizationEditorAction.SetupEditModel -> currentState.copy(
            editableOrganization = action.editModel,
            isLoading = false,
        )
        is OrganizationEditorAction.UpdateEditModel -> currentState.copy(
            editableOrganization = action.editModel,
        )
        is OrganizationEditorAction.UpdateLoading -> currentState.copy(
            isLoading = action.isLoading,
        )
    }

    enum class BackgroundKey : BackgroundWorkKey {
        LOAD_ORGANIZATION, SAVE_ORGANIZATION
    }
}

@Composable
internal fun Screen.rememberOrganizationEditorScreenModel(): OrganizationEditorScreenModel {
    val di = EditorFeatureDIHolder.fetchDI()
    return rememberScreenModel { di.instance<OrganizationEditorScreenModel>() }
}