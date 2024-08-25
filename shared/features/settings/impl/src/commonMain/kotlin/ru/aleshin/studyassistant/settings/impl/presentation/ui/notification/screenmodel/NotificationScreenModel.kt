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

package ru.aleshin.studyassistant.settings.impl.presentation.ui.notification.screenmodel

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.BaseScreenModel
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.EmptyDeps
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.settings.impl.di.holder.SettingsFeatureDIHolder
import ru.aleshin.studyassistant.settings.impl.presentation.ui.notification.contract.NotificationAction
import ru.aleshin.studyassistant.settings.impl.presentation.ui.notification.contract.NotificationEffect
import ru.aleshin.studyassistant.settings.impl.presentation.ui.notification.contract.NotificationEvent
import ru.aleshin.studyassistant.settings.impl.presentation.ui.notification.contract.NotificationViewState

/**
 * @author Stanislav Aleshin on 25.08.2024
 */
internal class NotificationScreenModel(
    private val workProcessor: NotificationWorkProcessor,
    stateCommunicator: NotificationStateCommunicator,
    effectCommunicator: NotificationEffectCommunicator,
    coroutineManager: CoroutineManager,
) : BaseScreenModel<NotificationViewState, NotificationEvent, NotificationAction, NotificationEffect, EmptyDeps>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun init(deps: EmptyDeps) {
        if (!isInitialize) {
            super.init(deps)
            dispatchEvent(NotificationEvent.Init)
        }
    }

    override suspend fun WorkScope<NotificationViewState, NotificationAction, NotificationEffect>.handleEvent(
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
            }
            is NotificationEvent.UpdateBeggingOfClassesNotify -> with(state()) {
                val settings = checkNotNull(settings)
                val updatedSettings = settings.copy(beginningOfClasses = event.beforeDelay)

                launchBackgroundWork(BackgroundKey.SETTINGS_ACTION) {
                    val command = NotificationWorkCommand.UpdateSettings(updatedSettings)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is NotificationEvent.UpdateBeggingOfClassesExceptions -> with(state()) {
                val settings = checkNotNull(settings)
                val updatedSettings = settings.copy(exceptionsForBeginningOfClasses = event.organizations)

                launchBackgroundWork(BackgroundKey.SETTINGS_ACTION) {
                    val command = NotificationWorkCommand.UpdateSettings(updatedSettings)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is NotificationEvent.UpdateEndOfClassesNotify -> with(state()) {
                val settings = checkNotNull(settings)
                val updatedSettings = settings.copy(endOfClasses = event.isNotify)

                launchBackgroundWork(BackgroundKey.SETTINGS_ACTION) {
                    val command = NotificationWorkCommand.UpdateSettings(updatedSettings)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is NotificationEvent.UpdateEndOfClassesExceptions -> with(state()) {
                val settings = checkNotNull(settings)
                val updatedSettings = settings.copy(exceptionsForEndOfClasses = event.organizations)

                launchBackgroundWork(BackgroundKey.SETTINGS_ACTION) {
                    val command = NotificationWorkCommand.UpdateSettings(updatedSettings)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is NotificationEvent.UpdateUnfinishedHomeworksNotify -> with(state()) {
                val settings = checkNotNull(settings)
                val updatedSettings = settings.copy(unfinishedHomeworks = event.time)

                launchBackgroundWork(BackgroundKey.SETTINGS_ACTION) {
                    val command = NotificationWorkCommand.UpdateSettings(updatedSettings)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is NotificationEvent.UpdateHighWorkloadWarningNotify -> with(state()) {
                val settings = checkNotNull(settings)
                val updatedSettings = settings.copy(highWorkload = event.maxRate)

                launchBackgroundWork(BackgroundKey.SETTINGS_ACTION) {
                    val command = NotificationWorkCommand.UpdateSettings(updatedSettings)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
        }
    }

    override suspend fun reduce(
        action: NotificationAction,
        currentState: NotificationViewState,
    ) = when (action) {
        is NotificationAction.UpdateSettings -> currentState.copy(
            settings = action.settings,
        )
        is NotificationAction.UpdateOrganizations -> currentState.copy(
            allOrganizations = action.organizations,
        )
    }

    enum class BackgroundKey : BackgroundWorkKey {
        LOAD_SETTINGS, LOAD_ORGANIZATIONS, SETTINGS_ACTION
    }
}

@Composable
internal fun Screen.rememberNotificationScreenModel(): NotificationScreenModel {
    val di = SettingsFeatureDIHolder.fetchDI()
    return rememberScreenModel { di.instance<NotificationScreenModel>() }
}