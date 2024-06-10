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

package ru.aleshin.studyassistant.presentation.ui.main.screenmodel

import architecture.screenmodel.work.ActionResult
import architecture.screenmodel.work.EffectResult
import architecture.screenmodel.work.FlowWorkProcessor
import architecture.screenmodel.work.WorkCommand
import extensions.delayedAction
import functional.Constants.Delay.SPLASH_NAV
import functional.Either
import functional.collectAndHandle
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import ru.aleshin.studyassistant.auth.api.navigation.AuthScreen
import ru.aleshin.studyassistant.domain.interactors.GeneralSettingsInteractor
import ru.aleshin.studyassistant.domain.interactors.UserCheckerInteractor
import ru.aleshin.studyassistant.navigation.GlobalScreenProvider
import ru.aleshin.studyassistant.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.presentation.ui.main.contract.MainAction
import ru.aleshin.studyassistant.presentation.ui.main.contract.MainEffect
import ru.aleshin.studyassistant.preview.api.navigation.PreviewScreen
import kotlin.time.ExperimentalTime

/**
 * @author Stanislav Aleshin on 19.04.2024.
 */
interface MainWorkProcessor : FlowWorkProcessor<MainWorkCommand, MainAction, MainEffect> {

    class Base(
        private val userCheckerInteractor: UserCheckerInteractor,
        private val settingsInteractor: GeneralSettingsInteractor,
        private val screenProvider: GlobalScreenProvider,
    ) : MainWorkProcessor {

        override suspend fun work(command: MainWorkCommand) = when (command) {
            MainWorkCommand.LoadThemeSettings -> loadThemeWork()
            MainWorkCommand.InitialNavigation -> initialNavigationWork()
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
                val settings = settingsInteractor.fetchSettings().first().let { settingsEither ->
                    when (settingsEither) {
                        is Either.Left -> return@delayedAction EffectResult(MainEffect.ShowError(settingsEither.data))
                        is Either.Right -> settingsEither.data
                    }
                }
                val isAuthorized = userCheckerInteractor.checkIsAuthorized().let { checkEither ->
                    when (checkEither) {
                        is Either.Left -> return@delayedAction EffectResult(MainEffect.ShowError(checkEither.data))
                        is Either.Right -> checkEither.data
                    }
                }
                val targetScreen = if (settings.isFirstStart) {
                    settingsInteractor.updateSettings(settings.copy(isFirstStart = false))
                    screenProvider.providePreviewScreen(PreviewScreen.Intro)
                } else {
                    if (isAuthorized) {
                        screenProvider.provideTabNavigationScreen()
                    } else {
                        screenProvider.provideAuthScreen(AuthScreen.Login)
                    }
                }
                return@delayedAction EffectResult(MainEffect.ReplaceGlobalScreen(targetScreen))
            }
            emit(result)
        }
    }
}

sealed class MainWorkCommand : WorkCommand {
    data object LoadThemeSettings : MainWorkCommand()
    data object InitialNavigation : MainWorkCommand()
}