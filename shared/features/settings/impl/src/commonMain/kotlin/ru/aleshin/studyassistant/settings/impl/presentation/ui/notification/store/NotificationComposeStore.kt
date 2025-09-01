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

package ru.aleshin.studyassistant.settings.impl.presentation.ui.notification.store

import ru.aleshin.studyassistant.core.common.architecture.component.EmptyInput
import ru.aleshin.studyassistant.core.common.architecture.store.BaseOnlyOutComposeStore
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.EffectCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.StateCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.settings.impl.presentation.ui.notification.contract.NotificationAction
import ru.aleshin.studyassistant.settings.impl.presentation.ui.notification.contract.NotificationEffect
import ru.aleshin.studyassistant.settings.impl.presentation.ui.notification.contract.NotificationEvent
import ru.aleshin.studyassistant.settings.impl.presentation.ui.notification.contract.NotificationOutput
import ru.aleshin.studyassistant.settings.impl.presentation.ui.notification.contract.NotificationState
import ru.aleshin.studyassistant.settings.impl.presentation.ui.notification.store.NotificationWorkCommand.UpdateSettings

/**
 * @author Stanislav Aleshin on 25.08.2024
 */
internal class NotificationComposeStore(
    private val workProcessor: NotificationWorkProcessor,
    stateCommunicator: StateCommunicator<NotificationState>,
    effectCommunicator: EffectCommunicator<NotificationEffect>,
    coroutineManager: CoroutineManager,
) : BaseOnlyOutComposeStore<NotificationState, NotificationEvent, NotificationAction, NotificationEffect, NotificationOutput>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun initialize(input: EmptyInput, isRestore: Boolean) {
        dispatchEvent(NotificationEvent.Init)
    }

    override suspend fun WorkScope<NotificationState, NotificationAction, NotificationEffect, NotificationOutput>.handleEvent(
        event: NotificationEvent,
    ) {
        when (event) {
            is NotificationEvent.Init -> {
                launchBackgroundWork(BackgroundKey.LOAD_SETTINGS) {
                    val command = NotificationWorkCommand.LoadSettings
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_ORGANIZATIONS) {
                    val command = NotificationWorkCommand.LoadOrganizations
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_PAID_USER_STATUS) {
                    val command = NotificationWorkCommand.LoadPaidUserStatus
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is NotificationEvent.UpdateBeggingOfClassesNotify -> with(state()) {
                val settings = checkNotNull(settings)
                val updatedSettings = settings.copy(beginningOfClasses = event.beforeDelay)

                launchBackgroundWork(BackgroundKey.SETTINGS_ACTION) {
                    val command = UpdateSettings(updatedSettings)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is NotificationEvent.UpdateBeggingOfClassesExceptions -> with(state()) {
                val settings = checkNotNull(settings)
                val updatedSettings = settings.copy(exceptionsForBeginningOfClasses = event.organizations)

                launchBackgroundWork(BackgroundKey.SETTINGS_ACTION) {
                    val command = UpdateSettings(updatedSettings)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is NotificationEvent.UpdateEndOfClassesNotify -> with(state()) {
                val settings = checkNotNull(settings)
                val updatedSettings = settings.copy(endOfClasses = event.isNotify)

                launchBackgroundWork(BackgroundKey.SETTINGS_ACTION) {
                    val command = UpdateSettings(updatedSettings)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is NotificationEvent.UpdateEndOfClassesExceptions -> with(state()) {
                val settings = checkNotNull(settings)
                val updatedSettings = settings.copy(exceptionsForEndOfClasses = event.organizations)

                launchBackgroundWork(BackgroundKey.SETTINGS_ACTION) {
                    val command = UpdateSettings(updatedSettings)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is NotificationEvent.UpdateUnfinishedHomeworksNotify -> with(state()) {
                val settings = checkNotNull(settings)
                val updatedSettings = settings.copy(unfinishedHomeworks = event.time)

                launchBackgroundWork(BackgroundKey.SETTINGS_ACTION) {
                    val command = UpdateSettings(updatedSettings)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is NotificationEvent.UpdateHighWorkloadWarningNotify -> with(state()) {
                val settings = checkNotNull(settings)
                val updatedSettings = settings.copy(highWorkload = event.maxRate)

                launchBackgroundWork(BackgroundKey.SETTINGS_ACTION) {
                    val command = UpdateSettings(updatedSettings)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is NotificationEvent.NavigateToBilling -> {
                consumeOutput(NotificationOutput.NavigateToBilling)
            }
        }
    }

    override suspend fun reduce(
        action: NotificationAction,
        currentState: NotificationState,
    ) = when (action) {
        is NotificationAction.UpdateSettings -> currentState.copy(
            settings = action.settings,
        )
        is NotificationAction.UpdateOrganizations -> currentState.copy(
            allOrganizations = action.organizations,
        )
        is NotificationAction.UpdatePaidUserStatus -> currentState.copy(
            isPaidUser = action.isPaidUser,
        )
    }

    enum class BackgroundKey : BackgroundWorkKey {
        LOAD_SETTINGS, LOAD_PAID_USER_STATUS, LOAD_ORGANIZATIONS, SETTINGS_ACTION
    }

    class Factory(
        private val workProcessor: NotificationWorkProcessor,
        private val coroutineManager: CoroutineManager,
    ) : BaseOnlyOutComposeStore.Factory<NotificationComposeStore, NotificationState> {

        override fun create(savedState: NotificationState): NotificationComposeStore {
            return NotificationComposeStore(
                workProcessor = workProcessor,
                stateCommunicator = StateCommunicator.Default(savedState),
                effectCommunicator = EffectCommunicator.Default(),
                coroutineManager = coroutineManager,
            )
        }
    }
}