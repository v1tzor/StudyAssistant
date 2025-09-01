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

package ru.aleshin.studyassistant.tasks.impl.presentation.ui.share.store

import ru.aleshin.studyassistant.core.common.architecture.component.EmptyInput
import ru.aleshin.studyassistant.core.common.architecture.store.BaseOnlyOutComposeStore
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.EffectCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.StateCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.editor.api.EditorFeatureComponent.EditorConfig
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.share.contract.ShareAction
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.share.contract.ShareEffect
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.share.contract.ShareEvent
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.share.contract.ShareOutput
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.share.contract.ShareState
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.share.store.ShareWorkCommand.LoadSubjects
import ru.aleshin.studyassistant.users.api.UsersFeatureComponent.UsersConfig

/**
 * @author Stanislav Aleshin on 18.07.2024
 */
internal class ShareComposeStore(
    private val workProcessor: ShareWorkProcessor,
    stateCommunicator: StateCommunicator<ShareState>,
    effectCommunicator: EffectCommunicator<ShareEffect>,
    coroutineManager: CoroutineManager,
) : BaseOnlyOutComposeStore<ShareState, ShareEvent, ShareAction, ShareEffect, ShareOutput>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun initialize(input: EmptyInput, isRestore: Boolean) {
        dispatchEvent(ShareEvent.Started)
    }

    override suspend fun WorkScope<ShareState, ShareAction, ShareEffect, ShareOutput>.handleEvent(
        event: ShareEvent,
    ) {
        when (event) {
            is ShareEvent.Started -> {
                launchBackgroundWork(BackgroundKey.LOAD_SHARED_HOMEWORKS) {
                    val command = ShareWorkCommand.LoadSharedHomeworks
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_ORGANIZATION) {
                    val command = ShareWorkCommand.LoadOrganizations
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_PAID_STATE) {
                    val command = ShareWorkCommand.LoadPaidUserState
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ShareEvent.LoadLinkData -> with(event) {
                launchBackgroundWork(BackgroundKey.LOAD_MATCHING_DATA) {
                    if (receivedHomeworks != null) {
                        val command = ShareWorkCommand.LoadLinkData(receivedHomeworks)
                        workProcessor.work(command).collectAndHandleWork()
                    } else {
                        sendAction(ShareAction.SetupLinkData(emptyList(), null))
                    }
                }
            }
            is ShareEvent.UpdateLinkData -> with(state()) {
                val updateLinkDataList = linkDataList.toMutableList().apply {
                    val targetIndex = linkDataList.indexOfFirst { it.homework.uid == event.linkData.homework.uid }
                    if (targetIndex != -1) set(targetIndex, event.linkData)
                }
                sendAction(ShareAction.UpdateLinkData(updateLinkDataList))
            }
            is ShareEvent.LoadLinkSubjects -> with(event) {
                launchBackgroundWork(BackgroundKey.LOAD_SUBJECTS) {
                    val command = LoadSubjects(organization)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ShareEvent.AcceptHomework -> with(event) {
                launchBackgroundWork(BackgroundKey.HOMEWORK_ACTION) {
                    val organizations = state().organizations
                    val command = ShareWorkCommand.AcceptHomework(receivedHomeworks, linkDataList, organizations)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ShareEvent.RejectHomework -> with(event) {
                launchBackgroundWork(BackgroundKey.HOMEWORK_ACTION) {
                    val command = ShareWorkCommand.RejectHomework(receivedHomeworks)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ShareEvent.CancelSendHomework -> with(event) {
                launchBackgroundWork(BackgroundKey.HOMEWORK_ACTION) {
                    val command = ShareWorkCommand.CancelSendHomework(sentHomeworks)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ShareEvent.ClickEditSubject -> with(event) {
                val config = EditorConfig.Subject(subjectId, organization)
                consumeOutput(ShareOutput.NavigateToSubjectEditor(config))
            }
            is ShareEvent.ClickUserProfile -> with(event) {
                val config = UsersConfig.UserProfile(userId)
                consumeOutput(ShareOutput.NavigateToUserProfile(config))
            }
            is ShareEvent.ClickPaidFunction -> {
                consumeOutput(ShareOutput.NavigateToBilling)
            }
            is ShareEvent.BackClick -> {
                consumeOutput(ShareOutput.NavigateToBack)
            }
        }
    }

    override suspend fun reduce(
        action: ShareAction,
        currentState: ShareState,
    ) = when (action) {
        is ShareAction.UpdateSharedHomeworks -> currentState.copy(
            sharedHomeworks = action.sharedHomeworks,
            isLoading = false,
        )
        is ShareAction.SetupLinkData -> currentState.copy(
            linkDataList = action.linkDataList,
            linkSchedule = action.linkSchedule,
            isLoadingLink = false,
        )
        is ShareAction.UpdateLinkData -> currentState.copy(
            linkDataList = action.linkDataList,
        )
        is ShareAction.UpdateSubjects -> currentState.copy(
            linkSubjects = action.subjects,
        )
        is ShareAction.UpdateOrganizations -> currentState.copy(
            organizations = action.organizations,
        )
        is ShareAction.UpdateUserPaidStatus -> currentState.copy(
            isPaidUser = action.isPaidUser,
        )
        is ShareAction.UpdateLoading -> currentState.copy(
            isLoading = action.isLoading,
        )
        is ShareAction.UpdateLinkLoading -> currentState.copy(
            isLoadingLink = action.isLoading,
        )
    }

    enum class BackgroundKey : BackgroundWorkKey {
        LOAD_SHARED_HOMEWORKS, LOAD_PAID_STATE, LOAD_ORGANIZATION, LOAD_SUBJECTS, LOAD_MATCHING_DATA, HOMEWORK_ACTION
    }

    class Factory(
        private val workProcessor: ShareWorkProcessor,
        private val coroutineManager: CoroutineManager,
    ) : BaseOnlyOutComposeStore.Factory<ShareComposeStore, ShareState> {

        override fun create(savedState: ShareState): ShareComposeStore {
            return ShareComposeStore(
                workProcessor = workProcessor,
                stateCommunicator = StateCommunicator.Default(savedState),
                effectCommunicator = EffectCommunicator.Default(),
                coroutineManager = coroutineManager,
            )
        }
    }
}