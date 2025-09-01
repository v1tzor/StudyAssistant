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

package ru.aleshin.studyassistant.info.impl.presentation.ui.subjects.store

import ru.aleshin.studyassistant.core.common.architecture.store.BaseComposeStore
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.EffectCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.StateCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.editor.api.EditorFeatureComponent.EditorConfig
import ru.aleshin.studyassistant.info.impl.presentation.ui.subjects.contract.SubjectsAction
import ru.aleshin.studyassistant.info.impl.presentation.ui.subjects.contract.SubjectsEffect
import ru.aleshin.studyassistant.info.impl.presentation.ui.subjects.contract.SubjectsEvent
import ru.aleshin.studyassistant.info.impl.presentation.ui.subjects.contract.SubjectsInput
import ru.aleshin.studyassistant.info.impl.presentation.ui.subjects.contract.SubjectsOutput
import ru.aleshin.studyassistant.info.impl.presentation.ui.subjects.contract.SubjectsState

/**
 * @author Stanislav Aleshin on 17.06.2024
 */
internal class SubjectsComposeStore(
    private val workProcessor: SubjectsWorkProcessor,
    stateCommunicator: StateCommunicator<SubjectsState>,
    effectCommunicator: EffectCommunicator<SubjectsEffect>,
    coroutineManager: CoroutineManager,
) : BaseComposeStore<SubjectsState, SubjectsEvent, SubjectsAction, SubjectsEffect, SubjectsInput, SubjectsOutput>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun initialize(input: SubjectsInput, isRestore: Boolean) {
        dispatchEvent(SubjectsEvent.Started(input))
    }

    override suspend fun WorkScope<SubjectsState, SubjectsAction, SubjectsEffect, SubjectsOutput>.handleEvent(
        event: SubjectsEvent,
    ) {
        when (event) {
            is SubjectsEvent.Started -> with(event) {
                launchBackgroundWork(BackgroundKey.LOAD_ORGANIZATIONS) {
                    val command = SubjectsWorkCommand.LoadOrganizations(inputData.organizationId)
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_SUBJECTS) {
                    val command = SubjectsWorkCommand.LoadSubjects(inputData.organizationId, state.sortedType)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is SubjectsEvent.SearchSubjects -> with(state) {
                launchBackgroundWork(BackgroundKey.LOAD_SUBJECTS) {
                    val organization = checkNotNull(selectedOrganization)
                    val command = SubjectsWorkCommand.SearchSubjects(event.query, organization, sortedType)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is SubjectsEvent.SelectedOrganization -> with(state) {
                sendAction(SubjectsAction.UpdateSelectedOrganization(event.organization))
                launchBackgroundWork(BackgroundKey.LOAD_SUBJECTS) {
                    val command = SubjectsWorkCommand.LoadSubjects(event.organization, sortedType)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is SubjectsEvent.SelectedSortedType -> with(state) {
                launchBackgroundWork(BackgroundKey.LOAD_SUBJECTS) {
                    val selectedOrganization = checkNotNull(selectedOrganization)
                    val command = SubjectsWorkCommand.LoadSubjects(selectedOrganization, event.sortedType)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is SubjectsEvent.ClickDeleteSubject -> {
                launchBackgroundWork(BackgroundKey.DELETE_SUBJECT) {
                    val command = SubjectsWorkCommand.DeleteSubject(event.subjectId)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is SubjectsEvent.ClickEditSubject -> with(state) {
                val organization = checkNotNull(selectedOrganization)
                val config = EditorConfig.Subject(event.subjectId, organization)
                consumeOutput(SubjectsOutput.NavigateToSubjectEditor(config))
            }
            is SubjectsEvent.ClickBack -> {
                consumeOutput(SubjectsOutput.NavigateToBack)
            }
        }
    }

    override suspend fun reduce(
        action: SubjectsAction,
        currentState: SubjectsState,
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

    class Factory(
        private val workProcessor: SubjectsWorkProcessor,
        private val coroutineManager: CoroutineManager,
    ) : BaseComposeStore.Factory<SubjectsComposeStore, SubjectsState> {

        override fun create(savedState: SubjectsState): SubjectsComposeStore {
            return SubjectsComposeStore(
                workProcessor = workProcessor,
                stateCommunicator = StateCommunicator.Default(savedState),
                effectCommunicator = EffectCommunicator.Default(),
                coroutineManager = coroutineManager,
            )
        }
    }
}