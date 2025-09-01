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

package ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.store

import ru.aleshin.studyassistant.core.common.architecture.component.EmptyInput
import ru.aleshin.studyassistant.core.common.architecture.store.BaseOnlyOutComposeStore
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.EffectCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.StateCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.editor.api.EditorFeatureComponent.EditorConfig
import ru.aleshin.studyassistant.info.api.InfoFeatureComponent.InfoConfig
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.contract.OrganizationsAction
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.contract.OrganizationsEffect
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.contract.OrganizationsEvent
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.contract.OrganizationsOutput
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.contract.OrganizationsState
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.store.OrganizationsWorkCommand.LoadOrganizationData
import ru.aleshin.studyassistant.users.api.UsersFeatureComponent.UsersConfig

/**
 * @author Stanislav Aleshin on 16.06.2024
 */
internal class OrganizationsComposeStore(
    private val workProcessor: OrganizationsWorkProcessor,
    stateCommunicator: StateCommunicator<OrganizationsState>,
    effectCommunicator: EffectCommunicator<OrganizationsEffect>,
    coroutineManager: CoroutineManager,
) : BaseOnlyOutComposeStore<OrganizationsState, OrganizationsEvent, OrganizationsAction, OrganizationsEffect, OrganizationsOutput>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun initialize(input: EmptyInput, isRestore: Boolean) {
        dispatchEvent(OrganizationsEvent.Started)
    }

    override suspend fun WorkScope<OrganizationsState, OrganizationsAction, OrganizationsEffect, OrganizationsOutput>.handleEvent(
        event: OrganizationsEvent,
    ) {
        when (event) {
            is OrganizationsEvent.Started -> {
                launchBackgroundWork(BackgroundKey.LOAD_SHORT_ORGANIZATIONS) {
                    val command = OrganizationsWorkCommand.LoadShortOrganizations
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_PAID_USER_STATE) {
                    val command = OrganizationsWorkCommand.LoadPaidUserStatus
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is OrganizationsEvent.Refresh -> {
                launchBackgroundWork(BackgroundKey.LOAD_ORGANIZATION_DATA) {
                    val command = LoadOrganizationData(event.organizationId)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is OrganizationsEvent.ChangeOrganization -> {
                launchBackgroundWork(BackgroundKey.LOAD_ORGANIZATION_DATA) {
                    val command = LoadOrganizationData(event.organizationId)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is OrganizationsEvent.ClickEmployee -> {
                val config = UsersConfig.EmployeeProfile(event.employeeId)
                consumeOutput(OrganizationsOutput.NavigateToEmployeeProfile(config))
            }
            is OrganizationsEvent.ClickShowAllEmployees -> {
                val config = InfoConfig.Employee(event.organizationId)
                consumeOutput(OrganizationsOutput.NavigateToEmployees(config))
            }
            is OrganizationsEvent.ClickShowAllSubjects -> {
                val config = InfoConfig.Subjects(event.organizationId)
                consumeOutput(OrganizationsOutput.NavigateToSubjects(config))
            }
            is OrganizationsEvent.ClickEditOrganization -> {
                val config = EditorConfig.Organization(event.organizationId)
                consumeOutput(OrganizationsOutput.NavigateToOrganizationEditor(config))
            }
            is OrganizationsEvent.ClickEditSubject -> {
                val config = EditorConfig.Subject(event.subjectId, event.organizationId)
                consumeOutput(OrganizationsOutput.NavigateToSubjectEditor(config))
            }
            is OrganizationsEvent.ClickPaidFunction -> {
                consumeOutput(OrganizationsOutput.NavigateToBilling)
            }
        }
    }

    override suspend fun reduce(
        action: OrganizationsAction,
        currentState: OrganizationsState,
    ) = when (action) {
        is OrganizationsAction.UpdateShortOrganizations -> currentState.copy(
            shortOrganizations = action.organizations,
        )
        is OrganizationsAction.UpdateOrganizationData -> currentState.copy(
            organizationData = action.data,
            classesInfo = action.classesInfo,
            isLoading = false,
        )
        is OrganizationsAction.UpdatePaidUserStatus -> currentState.copy(
            isPaidUser = action.isPaidUser,
        )
        is OrganizationsAction.UpdateLoading -> currentState.copy(
            isLoading = action.isLoading,
        )
    }

    enum class BackgroundKey : BackgroundWorkKey {
        LOAD_SHORT_ORGANIZATIONS, LOAD_ORGANIZATION_DATA, LOAD_PAID_USER_STATE
    }

    class Factory(
        private val workProcessor: OrganizationsWorkProcessor,
        private val coroutineManager: CoroutineManager,
    ) : BaseOnlyOutComposeStore.Factory<OrganizationsComposeStore, OrganizationsState> {

        override fun create(savedState: OrganizationsState): OrganizationsComposeStore {
            return OrganizationsComposeStore(
                workProcessor = workProcessor,
                stateCommunicator = StateCommunicator.Default(savedState),
                effectCommunicator = EffectCommunicator.Default(),
                coroutineManager = coroutineManager,
            )
        }
    }
}