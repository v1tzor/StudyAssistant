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

package ru.aleshin.studyassistant.auth.impl.presentation.ui.login.store

import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import ru.aleshin.studyassistant.auth.impl.domain.interactors.AuthInteractor
import ru.aleshin.studyassistant.auth.impl.presentation.mappers.mapToDomain
import ru.aleshin.studyassistant.auth.impl.presentation.models.credentials.LoginCredentialsUi
import ru.aleshin.studyassistant.auth.impl.presentation.ui.login.contract.LoginAction
import ru.aleshin.studyassistant.auth.impl.presentation.ui.login.contract.LoginEffect
import ru.aleshin.studyassistant.auth.impl.presentation.ui.login.contract.LoginOutput
import ru.aleshin.studyassistant.core.common.architecture.store.work.ActionResult
import ru.aleshin.studyassistant.core.common.architecture.store.work.EffectResult
import ru.aleshin.studyassistant.core.common.architecture.store.work.FlowWorkProcessor
import ru.aleshin.studyassistant.core.common.architecture.store.work.OutputResult
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkCommand
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkResult
import ru.aleshin.studyassistant.core.common.functional.DeviceInfoProvider
import ru.aleshin.studyassistant.core.common.functional.handle
import ru.aleshin.studyassistant.core.common.platform.services.AppService
import ru.aleshin.studyassistant.core.domain.entities.users.UserDevice
import ru.aleshin.studyassistant.core.domain.entities.users.UserSession

/**
 * @author Stanislav Aleshin on 20.04.2024.
 */
internal interface LoginWorkProcessor : FlowWorkProcessor<LoginWorkCommand, LoginAction, LoginEffect, LoginOutput> {

    class Base(
        private val authInteractor: AuthInteractor,
        private val deviceInfoProvider: DeviceInfoProvider,
        private val appService: AppService,
    ) : LoginWorkProcessor {

        override suspend fun work(command: LoginWorkCommand) = when (command) {
            is LoginWorkCommand.CheckGoogleAvailable -> checkGoogleAvailableWork()
            is LoginWorkCommand.LoginWithEmail -> loginWithEmailWork(command.credentials)
            is LoginWorkCommand.SuccessOAuthLogin -> successOAuthLoginWork(command.session)
        }

        private fun checkGoogleAvailableWork() = flow {
            val isAvailable = appService.isAvailableServices
            emit(ActionResult(LoginAction.UpdateGoogleAvailability(isAvailable)))
        }

        private fun loginWithEmailWork(credentials: LoginCredentialsUi) = flow<LoginWorkResult> {
            val device = UserDevice.specifyDevice(
                platform = deviceInfoProvider.fetchDevicePlatform(),
                deviceId = deviceInfoProvider.fetchDeviceId(),
                deviceName = deviceInfoProvider.fetchDeviceName(),
            )
            authInteractor.loginWithEmail(credentials.mapToDomain(), device).handle(
                onLeftAction = { emit(EffectResult(LoginEffect.ShowError(it))) },
                onRightAction = { authUser ->
                    if (authUser.emailVerification) {
                        emit(OutputResult(LoginOutput.NavigateToRecovery))
                    } else {
                        emit(OutputResult(LoginOutput.NavigateToVerification))
                    }
                }
            )
        }.onStart {
            emit(ActionResult(LoginAction.UpdateLoading(true)))
        }.onCompletion {
            emit(ActionResult(LoginAction.UpdateLoading(false)))
        }

        private fun successOAuthLoginWork(session: UserSession) = flow<LoginWorkResult> {
            val device = UserDevice.specifyDevice(
                platform = deviceInfoProvider.fetchDevicePlatform(),
                deviceId = deviceInfoProvider.fetchDeviceId(),
                deviceName = deviceInfoProvider.fetchDeviceName(),
            )
            authInteractor.confirmOAuthLogin(session, device).handle(
                onLeftAction = { emit(EffectResult(LoginEffect.ShowError(it))) },
                onRightAction = { result ->
                    if (result.isNewUser) {
                        emit(OutputResult(LoginOutput.NavigateToFirstSetup))
                    } else if (!result.authUser.emailVerification) {
                        emit(OutputResult(LoginOutput.NavigateToVerification))
                    } else {
                        emit(OutputResult(LoginOutput.NavigateToApp))
                    }
                }
            )
        }.onStart {
            emit(ActionResult(LoginAction.UpdateLoading(true)))
        }.onCompletion {
            emit(ActionResult(LoginAction.UpdateLoading(false)))
        }
    }
}

internal sealed class LoginWorkCommand : WorkCommand {
    data object CheckGoogleAvailable : LoginWorkCommand()
    data class LoginWithEmail(val credentials: LoginCredentialsUi) : LoginWorkCommand()
    data class SuccessOAuthLogin(val session: UserSession) : LoginWorkCommand()
}

internal typealias LoginWorkResult = WorkResult<LoginAction, LoginEffect, LoginOutput>