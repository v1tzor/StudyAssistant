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

package ru.aleshin.studyassistant.info.impl.presentation.ui.subjects.screenmodel

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.BaseScreenModel
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.editor.api.navigation.EditorScreen
import ru.aleshin.studyassistant.info.impl.di.holder.InfoFeatureDIHolder
import ru.aleshin.studyassistant.info.impl.navigation.InfoScreenProvider
import ru.aleshin.studyassistant.info.impl.presentation.ui.subjects.contract.SubjectsAction
import ru.aleshin.studyassistant.info.impl.presentation.ui.subjects.contract.SubjectsDeps
import ru.aleshin.studyassistant.info.impl.presentation.ui.subjects.contract.SubjectsEffect
import ru.aleshin.studyassistant.info.impl.presentation.ui.subjects.contract.SubjectsEvent
import ru.aleshin.studyassistant.info.impl.presentation.ui.subjects.contract.SubjectsViewState

/**
 * @author Stanislav Aleshin on 17.06.2024
 */
internal class SubjectsScreenModel(
    private val workProcessor: SubjectsWorkProcessor,
    private val screenProvider: InfoScreenProvider,
    stateCommunicator: SubjectsStateCommunicator,
    effectCommunicator: SubjectsEffectCommunicator,
    coroutineManager: CoroutineManager,
) : BaseScreenModel<SubjectsViewState, SubjectsEvent, SubjectsAction, SubjectsEffect, SubjectsDeps>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun init(deps: SubjectsDeps) {
        if (!isInitialize) {
            super.init(deps)
            dispatchEvent(SubjectsEvent.Init(deps.organizationId))
        }
    }

    override suspend fun WorkScope<SubjectsViewState, SubjectsAction, SubjectsEffect>.handleEvent(
        event: SubjectsEvent,
    ) {
        when (event) {
            is SubjectsEvent.Init -> with(state()) {
                launchBackgroundWork(BackgroundKey.LOAD_ORGANIZATIONS) {
                    val command = SubjectsWorkCommand.LoadOrganizations(event.organizationId)
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_SUBJECTS) {
                    val command = SubjectsWorkCommand.LoadSubjects(event.organizationId, sortedType)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is SubjectsEvent.SearchSubjects -> with(state()) {
                launchBackgroundWork(BackgroundKey.LOAD_SUBJECTS) {
                    val organization = checkNotNull(selectedOrganization)
                    val command = SubjectsWorkCommand.SearchSubjects(event.query, organization, sortedType)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is SubjectsEvent.SelectedOrganization -> with(state()) {
                sendAction(SubjectsAction.UpdateSelectedOrganization(event.organization))
                launchBackgroundWork(BackgroundKey.LOAD_SUBJECTS) {
                    val command = SubjectsWorkCommand.LoadSubjects(event.organization, sortedType)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is SubjectsEvent.SelectedSortedType -> with(state()) {
                launchBackgroundWork(BackgroundKey.LOAD_SUBJECTS) {
                    val selectedOrganization = checkNotNull(selectedOrganization)
                    val command = SubjectsWorkCommand.LoadSubjects(selectedOrganization, event.sortedType)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is SubjectsEvent.DeleteSubject -> {
                launchBackgroundWork(BackgroundKey.DELETE_SUBJECT) {
                    val command = SubjectsWorkCommand.DeleteSubject(event.subjectId)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is SubjectsEvent.NavigateToEditor -> with(state()) {
                val organization = checkNotNull(selectedOrganization)
                val featureScreen = EditorScreen.Subject(event.subjectId, organization)
                val screen = screenProvider.provideEditorScreen(featureScreen)
                sendEffect(SubjectsEffect.NavigateToGlobal(screen))
            }
            is SubjectsEvent.NavigateToBack -> {
                sendEffect(SubjectsEffect.NavigateToBack)
            }
        }
    }

    override suspend fun reduce(
        action: SubjectsAction,
        currentState: SubjectsViewState,
    ) = when (action) {
        is SubjectsAction.UpdateSubjects -> currentState.copy(
            subjects = action.subjects,
            sortedType = action.sortedType,
            isLoading = false,
        )
        is SubjectsAction.UpdateOrganizations -> currentState.copy(
            organizations = action.organizations,
            selectedOrganization = action.selectedOrganization,
        )
        is SubjectsAction.UpdateSelectedOrganization -> currentState.copy(
            selectedOrganization = action.organization,
        )
        is SubjectsAction.UpdateLoading -> currentState.copy(
            isLoading = action.isLoading,
        )
    }

    enum class BackgroundKey : BackgroundWorkKey {
        LOAD_ORGANIZATIONS, LOAD_SUBJECTS, DELETE_SUBJECT
    }
}

@Composable
internal fun Screen.rememberSubjectsScreenModel(): SubjectsScreenModel {
    val di = InfoFeatureDIHolder.fetchDI()
    return rememberScreenModel { di.instance<SubjectsScreenModel>() }
}