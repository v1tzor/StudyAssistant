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
import architecture.screenmodel.BaseScreenModel
import architecture.screenmodel.work.BackgroundWorkKey
import architecture.screenmodel.work.WorkScope
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import managers.CoroutineManager
import org.kodein.di.instance
import ru.aleshin.studyassistant.editor.api.navigation.EditorScreen
import ru.aleshin.studyassistant.preview.impl.di.holder.PreviewFeatureDIHolder
import ru.aleshin.studyassistant.preview.impl.navigation.PreviewScreenProvider
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.contract.SetupAction
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.contract.SetupDeps
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
) : BaseScreenModel<SetupViewState, SetupEvent, SetupAction, SetupEffect, SetupDeps>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun init(deps: SetupDeps) {
        if (!isInitialize) {
            super.init(deps)
            dispatchEvent(SetupEvent.Init(deps.createdUserId))
        }
    }

    override suspend fun WorkScope<SetupViewState, SetupAction, SetupEffect>.handleEvent(
        event: SetupEvent,
    ) {
        when (event) {
            is SetupEvent.Init -> launchBackgroundWork(SetupWorkKey.FETCH_ALL_DATA) {
                val command = SetupWorkCommand.FetchAllData(event.createdUserId)
                workProcessor.work(command).collectAndHandleWork()
            }
            is SetupEvent.UpdateProfile -> {
                sendAction(SetupAction.UpdateProfileInfo(event.userProfile))
            }
            is SetupEvent.UpdateOrganization -> {
                sendAction(SetupAction.UpdateOrganizationInfo(event.organization))
            }
            is SetupEvent.UpdateCalendarSettings -> {
                sendAction(SetupAction.UpdateCalendarSettings(event.calendarSettings))
            }
            is SetupEvent.SaveProfileInfo -> {
                launchBackgroundWork(SetupWorkKey.SAVE_PROFILE) {
                    val profile = state().profile ?: return@launchBackgroundWork
                    val command = SetupWorkCommand.SaveProfileInfo(profile)
                    workProcessor.work(command).collectAndHandleWork()
                }
                sendAction(SetupAction.UpdatePage(SetupPage.ORGANIZATION))
            }
            is SetupEvent.SaveOrganizationInfo -> {
                launchBackgroundWork(SetupWorkKey.SAVE_ORGANIZATION) {
                    val organization = state().organization ?: return@launchBackgroundWork
                    val command = SetupWorkCommand.SaveOrganizationInfo(organization)
                    workProcessor.work(command).collectAndHandleWork()
                }
                sendAction(SetupAction.UpdatePage(SetupPage.CALENDAR))
            }
            is SetupEvent.SaveCalendarInfo -> {
                launchBackgroundWork(SetupWorkKey.SAVE_SETTINGS) {
                    val calendarSettings = state().calendarSettings ?: return@launchBackgroundWork
                    val command = SetupWorkCommand.SaveCalendarSettings(calendarSettings)
                    workProcessor.work(command).collectAndHandleWork()
                }
                sendAction(SetupAction.UpdatePage(SetupPage.SCHEDULE))
            }
            is SetupEvent.NavigateToScheduleEditor -> {
                val screen = screenProvider.provideEditorScreen(EditorScreen.Schedule)
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
            organization = action.organization,
            calendarSettings = action.calendarSettings,
        )
        is SetupAction.UpdateProfileInfo -> currentState.copy(
            profile = action.profile,
        )
        is SetupAction.UpdateOrganizationInfo -> currentState.copy(
            organization = action.organization,
        )
        is SetupAction.UpdateCalendarSettings -> currentState.copy(
            calendarSettings = action.calendarSettings,
        )
    }
}

internal enum class SetupWorkKey : BackgroundWorkKey {
    FETCH_ALL_DATA, SAVE_PROFILE, SAVE_ORGANIZATION, SAVE_SETTINGS
}

@Composable
internal fun Screen.rememberSetupScreenModel(): SetupScreenModel {
    val di = PreviewFeatureDIHolder.fetchDI()
    return rememberScreenModel { di.instance<SetupScreenModel>() }
}