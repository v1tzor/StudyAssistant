/*
 * Copyright 2026 Stanislav Aleshin
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

package ru.aleshin.studyassistant.presentation.ui.main.store

import kotlinx.coroutines.flow.flow
import ru.aleshin.studyassistant.core.common.architecture.store.work.ActionResult
import ru.aleshin.studyassistant.core.common.architecture.store.work.EffectResult
import ru.aleshin.studyassistant.core.common.architecture.store.work.FlowWorkProcessor
import ru.aleshin.studyassistant.core.common.architecture.store.work.OutputResult
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkCommand
import ru.aleshin.studyassistant.core.common.extensions.delayedAction
import ru.aleshin.studyassistant.core.common.functional.Constants.Delay.SPLASH_NAV
import ru.aleshin.studyassistant.core.common.functional.collectAndHandle
import ru.aleshin.studyassistant.core.common.functional.firstHandleAndGet
import ru.aleshin.studyassistant.core.common.functional.handle
import ru.aleshin.studyassistant.core.common.navigation.DeepLinkUrl
import ru.aleshin.studyassistant.domain.interactors.GeneralSettingsInteractor
import ru.aleshin.studyassistant.domain.interactors.ReminderInteractor
import ru.aleshin.studyassistant.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.presentation.ui.main.contract.MainAction
import ru.aleshin.studyassistant.presentation.ui.main.contract.MainEffect
import ru.aleshin.studyassistant.presentation.ui.main.contract.MainOutput
import ru.aleshin.studyassistant.preview.api.PreviewConfig
import kotlin.time.ExperimentalTime

/**
 * @author Stanislav Aleshin on 19.04.2024.
 */
interface MainWorkProcessor :
    FlowWorkProcessor<MainWorkCommand, MainAction, MainEffect, MainOutput> {

    class Base(
        private val settingsInteractor: GeneralSettingsInteractor,
        private val reminderInteractor: ReminderInteractor,
    ) : MainWorkProcessor {

        override suspend fun work(command: MainWorkCommand) = when (command) {
            is MainWorkCommand.LoadThemeSettings -> loadThemeWork()
            is MainWorkCommand.InitialNavigation -> initialNavigationWork()
            is MainWorkCommand.ProcessDeepLink -> processDeepLinkWork(command.deepLinkUrl)
            is MainWorkCommand.OpenApp -> openAppWork(command.pendingDeepLink)
            is MainWorkCommand.UpdateReminderServices -> updateReminderServicesWork()
        }

        private fun loadThemeWork() = flow {
            settingsInteractor.fetchSettings().collectAndHandle(
                onLeftAction = { emit(EffectResult(MainEffect.ShowError(it))) },
                onRightAction = { settings ->
                    emit(ActionResult(MainAction.UpdateSettings(settings.mapToUi())))
                }
            )
        }

        @OptIn(ExperimentalTime::class)
        private fun initialNavigationWork() = flow {
            val result = delayedAction(SPLASH_NAV) {
                settingsInteractor.fetchSettings().firstHandleAndGet(
                    onLeftAction = { failure ->
                        emit(EffectResult(MainEffect.ShowError(failure)))
                        OutputResult(MainOutput.NavigateToPreview(PreviewConfig.Intro))
                    },
                    onRightAction = { settings ->
                        val output = if (!settings.isSetup) {
                            val startConfig = if (settings.isFirstStart) {
                                PreviewConfig.Intro
                            } else {
                                PreviewConfig.Setup
                            }
                            MainOutput.NavigateToPreview(startConfig)
                        } else {
                            MainOutput.NavigateToApp
                        }
                        OutputResult(output)
                    },
                )
            }
            emit(result)
        }

        private fun processDeepLinkWork(deepLinkUrl: DeepLinkUrl) = flow {
            emit(ActionResult(MainAction.UpdatePendingDeepLink(deepLinkUrl)))
            settingsInteractor.fetchSettings().firstHandleAndGet(
                onLeftAction = { failure ->
                    emit(EffectResult(MainEffect.ShowError(failure)))
                },
                onRightAction = { settings ->
                    if (settings.isSetup) {
                        emit(ActionResult(MainAction.UpdatePendingDeepLink(null)))
                        emit(OutputResult(MainOutput.NavigateToDeepLink(deepLinkUrl)))
                    }
                },
            )
        }

        private fun openAppWork(pendingDeepLink: DeepLinkUrl?) = flow {
            if (pendingDeepLink != null) {
                emit(ActionResult(MainAction.UpdatePendingDeepLink(null)))
                emit(OutputResult(MainOutput.NavigateToDeepLink(pendingDeepLink)))
            } else {
                emit(OutputResult(MainOutput.NavigateToApp))
            }
        }

        private fun updateReminderServicesWork() = flow {
            reminderInteractor.startOrRetryAvailableReminders().handle(
                onLeftAction = { emit(EffectResult(MainEffect.ShowError(it))) },
            )
        }
    }
}

sealed class MainWorkCommand : WorkCommand {
    data object LoadThemeSettings : MainWorkCommand()
    data object InitialNavigation : MainWorkCommand()
    data class ProcessDeepLink(val deepLinkUrl: DeepLinkUrl) : MainWorkCommand()
    data class OpenApp(val pendingDeepLink: DeepLinkUrl?) : MainWorkCommand()
    data object UpdateReminderServices : MainWorkCommand()
}
