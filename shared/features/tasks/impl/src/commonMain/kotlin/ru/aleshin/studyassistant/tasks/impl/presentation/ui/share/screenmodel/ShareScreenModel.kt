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

package ru.aleshin.studyassistant.tasks.impl.presentation.ui.share.screenmodel

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
import ru.aleshin.studyassistant.tasks.impl.di.holder.TasksFeatureDIHolder
import ru.aleshin.studyassistant.tasks.impl.navigation.TasksScreenProvider
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.share.contract.ShareAction
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.share.contract.ShareEffect
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.share.contract.ShareEvent
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.share.contract.ShareViewState
import ru.aleshin.studyassistant.users.api.navigation.UsersScreen

/**
 * @author Stanislav Aleshin on 18.07.2024
 */
internal class ShareScreenModel(
    private val workProcessor: ShareWorkProcessor,
    private val screenProvider: TasksScreenProvider,
    stateCommunicator: ShareStateCommunicator,
    effectCommunicator: ShareEffectCommunicator,
    coroutineManager: CoroutineManager,
) : BaseScreenModel<ShareViewState, ShareEvent, ShareAction, ShareEffect, EmptyDeps>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun init(deps: EmptyDeps) {
        if (!isInitialize) {
            super.init(deps)
            dispatchEvent(ShareEvent.Init)
        }
    }

    override suspend fun WorkScope<ShareViewState, ShareAction, ShareEffect>.handleEvent(
        event: ShareEvent,
    ) {
        when (event) {
            is ShareEvent.Init -> {
                launchBackgroundWork(BackgroundKey.LOAD_SHARED_HOMEWORKS) {
                    val command = ShareWorkCommand.LoadSharedHomeworks
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_ORGANIZATION) {
                    val command = ShareWorkCommand.LoadOrganizations
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
                    val command = ShareWorkCommand.LoadSubjects(organization)
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
            is ShareEvent.NavigateToSubjectEditor -> with(event) {
                val featureScreen = EditorScreen.Subject(subjectId, organization)
                val screen = screenProvider.provideEditorScreen(featureScreen)
                sendEffect(ShareEffect.NavigateToGlobal(screen))
            }
            is ShareEvent.NavigateToUserProfile -> with(event) {
                val featureScreen = UsersScreen.UserProfile(userId)
                val screen = screenProvider.provideUsersScreen(featureScreen)
                sendEffect(ShareEffect.NavigateToGlobal(screen))
            }
            is ShareEvent.NavigateToBack -> {
                sendEffect(ShareEffect.NavigateToBack)
            }
        }
    }

    override suspend fun reduce(
        action: ShareAction,
        currentState: ShareViewState,
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
        is ShareAction.UpdateLoading -> currentState.copy(
            isLoading = action.isLoading,
        )
        is ShareAction.UpdateLinkLoading -> currentState.copy(
            isLoadingLink = action.isLoading,
        )
    }

    enum class BackgroundKey : BackgroundWorkKey {
        LOAD_SHARED_HOMEWORKS, LOAD_ORGANIZATION, LOAD_SUBJECTS, LOAD_MATCHING_DATA, HOMEWORK_ACTION
    }
}

@Composable
internal fun Screen.rememberShareScreenModel(): ShareScreenModel {
    val di = TasksFeatureDIHolder.fetchDI()
    return rememberScreenModel { di.instance<ShareScreenModel>() }
}