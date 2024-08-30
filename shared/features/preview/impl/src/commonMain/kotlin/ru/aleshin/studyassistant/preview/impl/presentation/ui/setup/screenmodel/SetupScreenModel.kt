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

package ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.screenmodel

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.BaseScreenModel
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.EmptyDeps
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.ui.models.ActionWithAvatar
import ru.aleshin.studyassistant.editor.api.navigation.EditorScreen
import ru.aleshin.studyassistant.preview.impl.di.holder.PreviewFeatureDIHolder
import ru.aleshin.studyassistant.preview.impl.navigation.PreviewScreenProvider
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.contract.SetupAction
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.contract.SetupEffect
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.contract.SetupEvent
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.contract.SetupViewState
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.views.SetupPage

/**
 * @author Stanislav Aleshin on 17.04.2024
 */
internal class SetupScreenModel(
    private val workProcessor: SetupWorkProcessor,
    private val screenProvider: PreviewScreenProvider,
    stateCommunicator: SetupStateCommunicator,
    effectCommunicator: SetupEffectCommunicator,
    coroutineManager: CoroutineManager,
) : BaseScreenModel<SetupViewState, SetupEvent, SetupAction, SetupEffect, EmptyDeps>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun init(deps: EmptyDeps) {
        if (!isInitialize) {
            super.init(deps)
            dispatchEvent(SetupEvent.Init)
        }
    }

    override suspend fun WorkScope<SetupViewState, SetupAction, SetupEffect>.handleEvent(
        event: SetupEvent,
    ) {
        when (event) {
            is SetupEvent.Init -> {
                launchBackgroundWork(BackgroundKey.LOAD_ALL_DATA) {
                    val command = SetupWorkCommand.LoadAllData
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is SetupEvent.UpdateProfile -> {
                sendAction(SetupAction.UpdateUserProfile(event.userProfile))
            }
            is SetupEvent.UpdateOrganization -> {
                sendAction(SetupAction.UpdateOrganization(event.organization))
            }
            is SetupEvent.UpdateCalendarSettings -> {
                sendAction(SetupAction.UpdateCalendarSettings(event.calendarSettings))
            }
            is SetupEvent.SaveProfileInfo -> with(state()) {
                launchBackgroundWork(BackgroundKey.SAVE_PROFILE) {
                    val profile = checkNotNull(profile)
                    val command = SetupWorkCommand.UpdateUserProfile(profile, actionWithProfileAvatar)
                    workProcessor.work(command).collectAndHandleWork()
                }
                sendAction(SetupAction.UpdatePage(SetupPage.ORGANIZATION))
            }
            is SetupEvent.UpdateProfileAvatar -> with(event) {
                sendAction(SetupAction.UpdateActionWithProfileAvatar(ActionWithAvatar.Set(imageUri)))
            }
            is SetupEvent.DeleteProfileAvatar -> with(state()) {
                val action = if (profile?.avatar != null) {
                    ActionWithAvatar.Delete
                } else {
                    ActionWithAvatar.None(null)
                }
                sendAction(SetupAction.UpdateActionWithProfileAvatar(action))
            }
            is SetupEvent.SaveOrganizationInfo -> with(state()) {
                launchBackgroundWork(BackgroundKey.SAVE_ORGANIZATION) {
                    val organization = checkNotNull(organization)
                    val command = SetupWorkCommand.UpdateOrganization(organization, actionWithOrganizationAvatar)
                    workProcessor.work(command).collectAndHandleWork()
                }
                sendAction(SetupAction.UpdatePage(SetupPage.CALENDAR))
            }
            is SetupEvent.UpdateOrganizationAvatar -> with(event) {
                sendAction(SetupAction.UpdateActionWithOrganizationAvatar(ActionWithAvatar.Set(imageUri)))
            }
            is SetupEvent.DeleteOrganizationAvatar -> with(state()) {
                val action = if (organization?.avatar != null) {
                    ActionWithAvatar.Delete
                } else {
                    ActionWithAvatar.None(null)
                }
                sendAction(SetupAction.UpdateActionWithOrganizationAvatar(action))
            }
            is SetupEvent.SaveCalendarInfo -> with(state()) {
                launchBackgroundWork(BackgroundKey.SAVE_SETTINGS) {
                    val calendarSettings = checkNotNull(calendarSettings)
                    val command = SetupWorkCommand.UpdateCalendarSettings(calendarSettings)
                    workProcessor.work(command).collectAndHandleWork()
                }
                sendAction(SetupAction.UpdatePage(SetupPage.SCHEDULE))
            }
            is SetupEvent.NavigateToWeekScheduleEditor -> {
                val screen = screenProvider.provideEditorScreen(EditorScreen.WeekSchedule())
                sendEffect(SetupEffect.NavigateToGlobalScreen(screen))
            }
            is SetupEvent.NavigateToSchedule -> {
                val screen = screenProvider.provideTabNavigationScreen()
                sendEffect(SetupEffect.ReplaceGlobalScreen(screen))
            }
            is SetupEvent.NavigateToBackPage -> with(state()) {
                sendAction(SetupAction.UpdatePage(SetupPage.previousPage(currentPage)))
            }
        }
    }

    override suspend fun reduce(
        action: SetupAction,
        currentState: SetupViewState,
    ) = when (action) {
        is SetupAction.UpdatePage -> currentState.copy(
            currentPage = action.page,
        )
        is SetupAction.UpdateAll -> currentState.copy(
            profile = action.profile,
            actionWithProfileAvatar = ActionWithAvatar.None(action.profile.avatar),
            organization = action.organization,
            actionWithOrganizationAvatar = ActionWithAvatar.None(action.organization.avatar),
            calendarSettings = action.calendarSettings,
        )
        is SetupAction.UpdateUserProfile -> currentState.copy(
            profile = action.profile,
        )
        is SetupAction.UpdateActionWithProfileAvatar -> currentState.copy(
            actionWithProfileAvatar = action.action,
        )
        is SetupAction.UpdateOrganization -> currentState.copy(
            organization = action.organization,
        )
        is SetupAction.UpdateActionWithOrganizationAvatar -> currentState.copy(
            actionWithOrganizationAvatar = action.action,
        )
        is SetupAction.UpdateCalendarSettings -> currentState.copy(
            calendarSettings = action.calendarSettings,
        )
    }

    enum class BackgroundKey : BackgroundWorkKey {
        LOAD_ALL_DATA, SAVE_PROFILE, SAVE_ORGANIZATION, SAVE_SETTINGS
    }
}

@Composable
internal fun Screen.rememberSetupScreenModel(): SetupScreenModel {
    val di = PreviewFeatureDIHolder.fetchDI()
    return rememberScreenModel { di.instance<SetupScreenModel>() }
}