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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.screenmodel

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.BaseScreenModel
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.schedule.impl.di.holder.ScheduleFeatureDIHolder
import ru.aleshin.studyassistant.schedule.impl.navigation.ScheduleScreenProvider
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.contract.ShareAction
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.contract.ShareDeps
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.contract.ShareEffect
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.contract.ShareEvent
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.contract.ShareViewState
import ru.aleshin.studyassistant.users.api.navigation.UsersScreen

/**
 * @author Stanislav Aleshin on 16.08.2024
 */
internal class ShareScreenModel(
    private val workProcessor: ShareWorkProcessor,
    private val dateManager: DateManager,
    private val screenProvider: ScheduleScreenProvider,
    stateCommunicator: ShareStateCommunicator,
    effectCommunicator: ShareEffectCommunicator,
    coroutineManager: CoroutineManager,
) : BaseScreenModel<ShareViewState, ShareEvent, ShareAction, ShareEffect, ShareDeps>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun init(deps: ShareDeps) {
        if (!isInitialize) {
            super.init(deps)
            dispatchEvent(ShareEvent.Init(deps.shareId))
        }
    }

    override suspend fun WorkScope<ShareViewState, ShareAction, ShareEffect>.handleEvent(
        event: ShareEvent,
    ) {
        when (event) {
            is ShareEvent.Init -> with(event) {
                sendAction(ShareAction.UpdateCurrentTime(dateManager.fetchCurrentInstant()))
                launchBackgroundWork(BackgroundKey.LOAD_SHARED_SCHEDULES) {
                    val command = ShareWorkCommand.LoadSharedSchedules(shareId)
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_ORGANIZATIONS) {
                    val command = ShareWorkCommand.LoadAllOrganizations
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ShareEvent.LinkOrganization -> with(state()) {
                launchBackgroundWork(BackgroundKey.LINK_ORGANIZATION) {
                    val receivedMediatedSchedule = checkNotNull(receivedMediatedSchedule)
                    val command = ShareWorkCommand.LinkOrganization(
                        allLinkData = organizationsLinkData,
                        sharedSchedules = receivedMediatedSchedule.schedules,
                        sharedOrganization = event.sharedOrganization,
                        targetOrganization = event.linkedOrganization,
                    )
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ShareEvent.UpdateLinkedSubjects -> with(state()) {
                launchBackgroundWork(BackgroundKey.LINK_DATA) {
                    val receivedMediatedSchedule = checkNotNull(receivedMediatedSchedule)
                    val command = ShareWorkCommand.UpdateLinkedSubjects(
                        allLinkData = organizationsLinkData,
                        sharedSchedules = receivedMediatedSchedule.schedules,
                        sharedOrganization = event.sharedOrganization,
                        subjects = event.subjects,
                    )
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ShareEvent.UpdateLinkedTeachers -> with(state()) {
                launchBackgroundWork(BackgroundKey.LINK_DATA) {
                    val receivedMediatedSchedule = checkNotNull(receivedMediatedSchedule)
                    val command = ShareWorkCommand.UpdateLinkedEmployees(
                        allLinkData = organizationsLinkData,
                        sharedSchedules = receivedMediatedSchedule.schedules,
                        sharedOrganization = event.sharedOrganization,
                        teachers = event.teachers,
                    )
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ShareEvent.AcceptSharedSchedule -> with(state()){
                launchBackgroundWork(BackgroundKey.SHARE_ACTION) {
                    val sharedSchedules = checkNotNull(receivedMediatedSchedule)
                    val command = ShareWorkCommand.AcceptSharedSchedule(
                        sharedSchedules = sharedSchedules,
                        organizationsLinkData = organizationsLinkData,
                        linkedSchedules = linkedSchedules,
                    )
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ShareEvent.RejectSharedSchedule -> with(state()){
                launchBackgroundWork(BackgroundKey.SHARE_ACTION) {
                    val sharedSchedules = checkNotNull(receivedMediatedSchedule)
                    val command = ShareWorkCommand.RejectSharedSchedule(sharedSchedules)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ShareEvent.NavigateToUserProfile -> with(event) {
                val featureScreen = UsersScreen.UserProfile(user.uid)
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
        is ShareAction.SetupSharedSchedules -> currentState.copy(
            receivedMediatedSchedule = action.receivedMediatedSchedule,
            organizationsLinkData = action.organizationsLinkData,
            linkedSchedules = action.linkedSchedules,
            isLoading = false,
        )
        is ShareAction.UpdateLinkData -> currentState.copy(
            organizationsLinkData = action.linkData,
            linkedSchedules = action.linkedSchedules,
        )
        is ShareAction.UpdateOrganizations -> currentState.copy(
            allOrganizations = action.organizations,
        )
        is ShareAction.UpdateCurrentTime -> currentState.copy(
            currentTime = action.time,
        )
        is ShareAction.UpdateLoading -> currentState.copy(
            isLoading = action.isLoading,
        )
        is ShareAction.UpdateLoadingAccept -> currentState.copy(
            isLoadingAccept = action.isLoading,
        )
        is ShareAction.UpdateLoadingLinkedOrganization -> currentState.copy(
            isLoadingLinkedOrganization = action.isLoading,
        )
    }

    enum class BackgroundKey : BackgroundWorkKey {
        LOAD_SHARED_SCHEDULES, LOAD_ORGANIZATIONS, LINK_ORGANIZATION, LINK_DATA, SHARE_ACTION
    }
}

@Composable
internal fun Screen.rememberShareScreenModel(): ShareScreenModel {
    val di = ScheduleFeatureDIHolder.fetchDI()
    return rememberScreenModel { di.instance<ShareScreenModel>() }
}