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

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.BaseScreenModel
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.EmptyDeps
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.editor.api.navigation.EditorScreen
import ru.aleshin.studyassistant.info.api.navigation.InfoScreen
import ru.aleshin.studyassistant.info.impl.di.holder.InfoFeatureDIHolder
import ru.aleshin.studyassistant.info.impl.navigation.InfoScreenProvider
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.contract.OrganizationsAction
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.contract.OrganizationsEffect
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.contract.OrganizationsEvent
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.contract.OrganizationsViewState
import ru.aleshin.studyassistant.users.api.navigation.UsersScreen

/**
 * @author Stanislav Aleshin on 16.06.2024
 */
internal class OrganizationsScreenModel(
    private val workProcessor: OrganizationsWorkProcessor,
    private val screenProvider: InfoScreenProvider,
    stateCommunicator: OrganizationsStateCommunicator,
    effectCommunicator: OrganizationsEffectCommunicator,
    coroutineManager: CoroutineManager,
) : BaseScreenModel<OrganizationsViewState, OrganizationsEvent, OrganizationsAction, OrganizationsEffect, EmptyDeps>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun init(deps: EmptyDeps) {
        if (!isInitialize) {
            super.init(deps)
            dispatchEvent(OrganizationsEvent.Init)
        }
    }

    override suspend fun WorkScope<OrganizationsViewState, OrganizationsAction, OrganizationsEffect>.handleEvent(
        event: OrganizationsEvent,
    ) {
        when (event) {
            is OrganizationsEvent.Init -> {
                launchBackgroundWork(BackgroundKey.LOAD_SHORT_ORGANIZATIONS) {
                    val command = OrganizationsWorkCommand.LoadShortOrganizations
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is OrganizationsEvent.Refresh -> {
                launchBackgroundWork(BackgroundKey.LOAD_ORGANIZATION_DATA) {
                    val command = OrganizationsWorkCommand.LoadOrganizationData(event.organizationId)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is OrganizationsEvent.ChangeOrganization -> {
                launchBackgroundWork(BackgroundKey.LOAD_ORGANIZATION_DATA) {
                    val command = OrganizationsWorkCommand.LoadOrganizationData(event.organizationId)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is OrganizationsEvent.OpenEmployeeProfile -> {
                val featureScreen = UsersScreen.EmployeeProfile(event.employeeId)
                val screen = screenProvider.provideUsersScreen(featureScreen)
                sendEffect(OrganizationsEffect.NavigateToGlobal(screen))
            }
            is OrganizationsEvent.NavigateToEmployees -> {
                val featureScreen = InfoScreen.Employee(event.organizationId)
                val screen = screenProvider.provideFeatureScreen(featureScreen)
                sendEffect(OrganizationsEffect.NavigateToLocal(screen))
            }
            is OrganizationsEvent.NavigateToSubjects -> {
                val featureScreen = InfoScreen.Subjects(event.organizationId)
                val screen = screenProvider.provideFeatureScreen(featureScreen)
                sendEffect(OrganizationsEffect.NavigateToLocal(screen))
            }
            is OrganizationsEvent.NavigateToOrganizationEditor -> {
                val featureScreen = EditorScreen.Organization(event.organizationId)
                val screen = screenProvider.provideEditorScreen(featureScreen)
                sendEffect(OrganizationsEffect.NavigateToGlobal(screen))
            }
            is OrganizationsEvent.NavigateToSubjectEditor -> {
                val featureScreen = EditorScreen.Subject(event.subjectId, event.organizationId)
                val screen = screenProvider.provideEditorScreen(featureScreen)
                sendEffect(OrganizationsEffect.NavigateToGlobal(screen))
            }
        }
    }

    override suspend fun reduce(
        action: OrganizationsAction,
        currentState: OrganizationsViewState,
    ) = when (action) {
        is OrganizationsAction.UpdateShortOrganizations -> currentState.copy(
            shortOrganizations = action.organizations,
        )
        is OrganizationsAction.UpdateOrganizationData -> currentState.copy(
            organizationData = action.data,
            classesInfo = action.classesInfo,
            isLoading = false,
        )
        is OrganizationsAction.UpdateLoading -> currentState.copy(
            isLoading = action.isLoading,
        )
    }

    enum class BackgroundKey : BackgroundWorkKey {
        LOAD_SHORT_ORGANIZATIONS, LOAD_ORGANIZATION_DATA
    }
}

@Composable
internal fun Screen.rememberOrganizationsScreenModel(): OrganizationsScreenModel {
    val di = InfoFeatureDIHolder.fetchDI()
    return rememberScreenModel { di.instance<OrganizationsScreenModel>() }
}