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

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import ru.aleshin.studyassistant.auth.api.navigation.AuthScreen
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.ActionResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.EffectResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.FlowWorkProcessor
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkCommand
import ru.aleshin.studyassistant.core.common.extensions.delayedAction
import ru.aleshin.studyassistant.core.common.functional.Constants.Delay.SPLASH_NAV
import ru.aleshin.studyassistant.core.common.functional.DeviceInfoProvider
import ru.aleshin.studyassistant.core.common.functional.Either
import ru.aleshin.studyassistant.core.common.functional.collectAndHandle
import ru.aleshin.studyassistant.core.common.functional.firstRightOrNull
import ru.aleshin.studyassistant.core.common.messages.PushServiceType
import ru.aleshin.studyassistant.core.domain.entities.users.UserDevice
import ru.aleshin.studyassistant.domain.interactors.AppUserInteractor
import ru.aleshin.studyassistant.domain.interactors.GeneralSettingsInteractor
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
        private val userInteractor: AppUserInteractor,
        private val settingsInteractor: GeneralSettingsInteractor,
        private val deviceInfoProvider: DeviceInfoProvider,
        private val screenProvider: GlobalScreenProvider,
    ) : MainWorkProcessor {

        override suspend fun work(command: MainWorkCommand) = when (command) {
            is MainWorkCommand.LoadThemeSettings -> loadThemeWork()
            is MainWorkCommand.InitialNavigation -> initialNavigationWork()
            is MainWorkCommand.UpdatePushToken -> updatePushTokenWork()
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
                val isAuthorized = userInteractor.checkIsAuthorized().let { checkEither ->
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

        private fun updatePushTokenWork() = flow {
            userInteractor.fetchAppUser().collectAndHandle(
                onLeftAction = { emit(EffectResult(MainEffect.ShowError(it))) },
                onRightAction = { appUser ->
                    val token = userInteractor.fetchAppToken().firstRightOrNull {
                        emit(EffectResult(MainEffect.ShowError(it)))
                    }
                    val deviceId = deviceInfoProvider.fetchDeviceId()
                    val currentDeviceInfo = appUser?.devices?.find { it.deviceId == deviceId }
                    val actualDeviceInfo = UserDevice(
                        platform = deviceInfoProvider.fetchDevicePlatform(),
                        deviceId = deviceId,
                        deviceName = deviceInfoProvider.fetchDeviceName(),
                        pushToken = token?.token,
                        pushServiceType = token?.service ?: PushServiceType.NONE,
                    )
                    if (appUser != null && currentDeviceInfo != null && actualDeviceInfo != currentDeviceInfo) {
                        val updatedUser = appUser.copy(
                            devices = buildList {
                                addAll(appUser.devices)
                                remove(currentDeviceInfo)
                                add(actualDeviceInfo)
                            }
                        )
                        userInteractor.updateUser(updatedUser)
                    }
                },
            )
        }
    }
}

sealed class MainWorkCommand : WorkCommand {
    data object LoadThemeSettings : MainWorkCommand()
    data object InitialNavigation : MainWorkCommand()
    data object UpdatePushToken : MainWorkCommand()
}