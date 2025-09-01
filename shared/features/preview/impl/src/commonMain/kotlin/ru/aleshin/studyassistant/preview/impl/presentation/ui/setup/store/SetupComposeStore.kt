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

package ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.store

import ru.aleshin.studyassistant.core.common.architecture.component.EmptyInput
import ru.aleshin.studyassistant.core.common.architecture.store.BaseOnlyOutComposeStore
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.EffectCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.StateCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.ui.mappers.convertToInputFile
import ru.aleshin.studyassistant.core.ui.models.ActionWithAvatar.Delete
import ru.aleshin.studyassistant.core.ui.models.ActionWithAvatar.None
import ru.aleshin.studyassistant.core.ui.models.ActionWithAvatar.Set
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.contract.SetupAction
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.contract.SetupEffect
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.contract.SetupEvent
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.contract.SetupOutput
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.contract.SetupState
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.views.SetupPage

/**
 * @author Stanislav Aleshin on 17.04.2024
 */
internal class SetupComposeStore(
    private val workProcessor: SetupWorkProcessor,
    stateCommunicator: StateCommunicator<SetupState>,
    effectCommunicator: EffectCommunicator<SetupEffect>,
    coroutineManager: CoroutineManager,
) : BaseOnlyOutComposeStore<SetupState, SetupEvent, SetupAction, SetupEffect, SetupOutput>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun initialize(input: EmptyInput, isRestore: Boolean) {
        dispatchEvent(SetupEvent.Started(isRestore))
    }

    override suspend fun WorkScope<SetupState, SetupAction, SetupEffect, SetupOutput>.handleEvent(
        event: SetupEvent,
    ) {
        when (event) {
            is SetupEvent.Started -> with(event) {
                if (!isRestore) {
                    launchBackgroundWork(BackgroundKey.LOAD_ALL_DATA) {
                        val command = SetupWorkCommand.LoadAllData
                        workProcessor.work(command).collectAndHandleWork()
                    }
                }
                launchBackgroundWork(BackgroundKey.LOAD_PAID_USER_STATUS) {
                    val command = SetupWorkCommand.LoadPaidUserStatus
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
            is SetupEvent.ClickSaveProfileInfo -> with(state()) {
                launchBackgroundWork(BackgroundKey.SAVE_PROFILE) {
                    val profile = checkNotNull(profile)
                    val command = SetupWorkCommand.UpdateUserProfile(profile, actionWithProfileAvatar)
                    workProcessor.work(command).collectAndHandleWork()
                }
                sendAction(SetupAction.UpdatePage(SetupPage.ORGANIZATION))
            }
            is SetupEvent.UpdateProfileAvatar -> with(event) {
                val inputFile = image.convertToInputFile()
                sendAction(SetupAction.UpdateActionWithProfileAvatar(Set(inputFile)))
            }
            is SetupEvent.DeleteProfileAvatar -> with(state()) {
                val action = if (profile?.avatar != null) {
                    Delete
                } else {
                    None(null)
                }
                sendAction(SetupAction.UpdateActionWithProfileAvatar(action))
            }
            is SetupEvent.ClickSaveOrganizationInfo -> with(state()) {
                launchBackgroundWork(BackgroundKey.SAVE_ORGANIZATION) {
                    val organization = checkNotNull(organization)
                    val command = SetupWorkCommand.UpdateOrganization(organization, actionWithOrganizationAvatar)
                    workProcessor.work(command).collectAndHandleWork()
                }
                sendAction(SetupAction.UpdatePage(SetupPage.CALENDAR))
            }
            is SetupEvent.UpdateOrganizationAvatar -> with(event) {
                val inputFile = image.convertToInputFile()
                sendAction(SetupAction.UpdateActionWithOrganizationAvatar(Set(inputFile)))
            }
            is SetupEvent.DeleteOrganizationAvatar -> with(state()) {
                val action = if (organization?.avatar != null) {
                    Delete
                } else {
                    None(null)
                }
                sendAction(SetupAction.UpdateActionWithOrganizationAvatar(action))
            }
            is SetupEvent.ClickSaveCalendarInfo -> with(state()) {
                launchBackgroundWork(BackgroundKey.SAVE_SETTINGS) {
                    val calendarSettings = checkNotNull(calendarSettings)
                    val command = SetupWorkCommand.UpdateCalendarSettings(calendarSettings)
                    workProcessor.work(command).collectAndHandleWork()
                }
                sendAction(SetupAction.UpdatePage(SetupPage.SCHEDULE))
            }
            is SetupEvent.ClickEditWeekSchedule -> {
                workProcessor.work(SetupWorkCommand.FinishSetup).collectAndHandleWork()
                consumeOutput(SetupOutput.NavigateToWeekScheduleEditor)
            }
            is SetupEvent.ClickGoToApp -> {
                workProcessor.work(SetupWorkCommand.FinishSetup).collectAndHandleWork()
                consumeOutput(SetupOutput.NavigateToApp)
            }
            is SetupEvent.ClickBackPage -> with(state()) {
                sendAction(SetupAction.UpdatePage(SetupPage.previousPage(currentPage)))
            }
            is SetupEvent.ClickPaidFunction -> {
                consumeOutput(SetupOutput.NavigateToBilling)
            }
            is SetupEvent.ClickBack -> {
                consumeOutput(SetupOutput.NavigateToBack)
            }
        }
    }

    override suspend fun reduce(
        action: SetupAction,
        currentState: SetupState,
    ) = when (action) {
        is SetupAction.UpdatePage -> currentState.copy(
            currentPage = action.page,
        )
        is SetupAction.UpdateAll -> currentState.copy(
            profile = action.profile,
            actionWithProfileAvatar = None(action.profile.avatar),
            organization = action.organization,
            actionWithOrganizationAvatar = None(action.organization.avatar),
            calendarSettings = action.calendarSettings,
        )
        is SetupAction.UpdateUserPaidStatus -> currentState.copy(
            isPaidUser = action.isPaid,
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
        LOAD_ALL_DATA, LOAD_PAID_USER_STATUS, SAVE_PROFILE, SAVE_ORGANIZATION, SAVE_SETTINGS
    }

    class Factory(
        private val workProcessor: SetupWorkProcessor,
        private val coroutineManager: CoroutineManager,
    ) : BaseOnlyOutComposeStore.Factory<SetupComposeStore, SetupState> {

        override fun create(savedState: SetupState): SetupComposeStore {
            return SetupComposeStore(
                workProcessor = workProcessor,
                stateCommunicator = StateCommunicator.Default(savedState),
                effectCommunicator = EffectCommunicator.Default(),
                coroutineManager = coroutineManager,
            )
        }
    }
}