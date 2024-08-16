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

package ru.aleshin.studyassistant.profile.impl.presentation.ui.screenmodel

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.BaseScreenModel
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.EmptyDeps
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.editor.api.navigation.EditorScreen
import ru.aleshin.studyassistant.profile.impl.di.holder.ProfileFeatureDIHolder
import ru.aleshin.studyassistant.profile.impl.navigation.ProfileScreenProvider
import ru.aleshin.studyassistant.profile.impl.presentation.ui.contract.ProfileAction
import ru.aleshin.studyassistant.profile.impl.presentation.ui.contract.ProfileEffect
import ru.aleshin.studyassistant.profile.impl.presentation.ui.contract.ProfileEvent
import ru.aleshin.studyassistant.profile.impl.presentation.ui.contract.ProfileViewState
import ru.aleshin.studyassistant.schedule.api.navigation.ScheduleScreen
import ru.aleshin.studyassistant.settings.api.navigation.SettingsScreen
import ru.aleshin.studyassistant.users.api.navigation.UsersScreen

/**
 * @author Stanislav Aleshin on 21.04.2024
 */
internal class ProfileScreenModel(
    private val workProcessor: ProfileWorkProcessor,
    private val screenProvider: ProfileScreenProvider,
    private val dateManager: DateManager,
    stateCommunicator: ProfileStateCommunicator,
    effectCommunicator: ProfileEffectCommunicator,
    coroutineManager: CoroutineManager,
) : BaseScreenModel<ProfileViewState, ProfileEvent, ProfileAction, ProfileEffect, EmptyDeps>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun init(deps: EmptyDeps) {
        if (!isInitialize) {
            super.init(deps)
            dispatchEvent(ProfileEvent.Init)
        }
    }

    override suspend fun WorkScope<ProfileViewState, ProfileAction, ProfileEffect>.handleEvent(
        event: ProfileEvent,
    ) {
        when (event) {
            is ProfileEvent.Init -> {
                sendAction(ProfileAction.UpdateCurrentTime(dateManager.fetchCurrentInstant()))
                launchBackgroundWork(BackgroundKey.LOAD_PROFILE_INFO) {
                    val command = ProfileWorkCommand.LoadProfileInfo
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_SHARED_SCHEDULES) {
                    val command = ProfileWorkCommand.LoadSharedSchedules
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_FRIENDS) {
                    val command = ProfileWorkCommand.LoadFriends
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ProfileEvent.SendSharedSchedule -> {
                launchBackgroundWork(BackgroundKey.SEND_SCHEDULE) {
                    val command = ProfileWorkCommand.SendSharedSchedule(event.sendData)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ProfileEvent.CancelSentSchedule -> {
                launchBackgroundWork(BackgroundKey.DATA_ACTION) {
                    val command = ProfileWorkCommand.CancelSentSharedSchedule(event.schedule)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ProfileEvent.SignOut -> {
                launchBackgroundWork(BackgroundKey.DATA_ACTION) {
                    val command = ProfileWorkCommand.SignOut
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ProfileEvent.NavigateToShareScheduleViewer -> {
                val screen = screenProvider.provideScheduleScreen(ScheduleScreen.Share(event.shareId))
                sendEffect(ProfileEffect.PushGlobalScreen(screen))
            }
            is ProfileEvent.NavigateToPrivacySettings -> {
            }
            is ProfileEvent.NavigateToFriends -> {
                val screen = screenProvider.provideUsersScreen(UsersScreen.Friends)
                sendEffect(ProfileEffect.PushGlobalScreen(screen))
            }
            is ProfileEvent.NavigateToGeneralSettings -> {
                val screen = screenProvider.provideSettingsScreen(SettingsScreen.General)
                sendEffect(ProfileEffect.PushGlobalScreen(screen))
            }
            is ProfileEvent.NavigateToNotifySettings -> {
                val screen = screenProvider.provideSettingsScreen(SettingsScreen.Notification)
                sendEffect(ProfileEffect.PushGlobalScreen(screen))
            }
            is ProfileEvent.NavigateToCalendarSettings -> {
                val screen = screenProvider.provideSettingsScreen(SettingsScreen.Calendar)
                sendEffect(ProfileEffect.PushGlobalScreen(screen))
            }
            is ProfileEvent.NavigateToPaymentsSettings -> {
                val screen = screenProvider.provideSettingsScreen(SettingsScreen.Subscription)
                sendEffect(ProfileEffect.PushGlobalScreen(screen))
            }
            is ProfileEvent.NavigateToProfileEditor -> {
                val screen = screenProvider.provideEditorScreen(EditorScreen.Profile)
                sendEffect(ProfileEffect.PushGlobalScreen(screen))
            }
        }
    }

    override suspend fun reduce(
        action: ProfileAction,
        currentState: ProfileViewState,
    ) = when (action) {
        is ProfileAction.UpdateProfileInfo -> currentState.copy(
            appUserProfile = action.profile,
            friendRequest = action.requests,
            isLoading = false,
        )
        is ProfileAction.UpdateSharedSchedules -> currentState.copy(
            sharedSchedules = action.schedules,
            allOrganizations = action.organizations,
            isLoadingShare = false,
        )
        is ProfileAction.UpdateFriends -> currentState.copy(
            allFriends = action.friends,
        )
        is ProfileAction.UpdateCurrentTime -> currentState.copy(
            currentTime = action.time,
        )
        is ProfileAction.UpdateLoading -> currentState.copy(
            isLoading = action.isLoading,
        )
        is ProfileAction.UpdateLoadingSend -> currentState.copy(
            isLoadingSend = action.isLoading,
        )
        is ProfileAction.UpdateLoadingShared -> currentState.copy(
            isLoadingShare = action.isLoading,
        )
    }

    override fun onDispose() {
        super.onDispose()
        ProfileFeatureDIHolder.clear()
    }

    enum class BackgroundKey : BackgroundWorkKey {
        LOAD_PROFILE_INFO, LOAD_SHARED_SCHEDULES, LOAD_FRIENDS, SEND_SCHEDULE, DATA_ACTION
    }
}

@Composable
internal fun Screen.rememberProfileScreenModel(): ProfileScreenModel {
    val di = ProfileFeatureDIHolder.fetchDI()
    return rememberScreenModel { di.instance<ProfileScreenModel>() }
}