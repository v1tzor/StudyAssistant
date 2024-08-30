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

package ru.aleshin.studyassistant.auth.impl.presentation.ui.register.screenmodel

import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import ru.aleshin.studyassistant.auth.api.navigation.AuthScreen
import ru.aleshin.studyassistant.auth.impl.domain.interactors.AuthInteractor
import ru.aleshin.studyassistant.auth.impl.navigation.AuthScreenProvider
import ru.aleshin.studyassistant.auth.impl.presentation.mappers.mapToDomain
import ru.aleshin.studyassistant.auth.impl.presentation.models.credentials.RegisterCredentialsUi
import ru.aleshin.studyassistant.auth.impl.presentation.ui.register.contract.RegisterAction
import ru.aleshin.studyassistant.auth.impl.presentation.ui.register.contract.RegisterEffect
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.ActionResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.EffectResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.FlowWorkProcessor
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkCommand
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkResult
import ru.aleshin.studyassistant.core.common.functional.DeviceInfoProvider
import ru.aleshin.studyassistant.core.common.functional.handle
import ru.aleshin.studyassistant.core.domain.entities.users.UserDevice

/**
 * @author Stanislav Aleshin on 20.04.2024.
 */
internal interface RegisterWorkProcessor :
    FlowWorkProcessor<RegisterWorkCommand, RegisterAction, RegisterEffect> {

    class Base(
        private val authInteractor: AuthInteractor,
        private val screenProvider: AuthScreenProvider,
        private val deviceInfoProvider: DeviceInfoProvider,
    ) : RegisterWorkProcessor {

        override suspend fun work(command: RegisterWorkCommand) = when (command) {
            is RegisterWorkCommand.RegisterNewAccount -> registerNewAccountWork(command.credentials)
        }

        private fun registerNewAccountWork(credentials: RegisterCredentialsUi) = flow<RegisterWorkResult> {
            val device = UserDevice.specifyDevice(
                platform = deviceInfoProvider.fetchDevicePlatform(),
                deviceId = deviceInfoProvider.fetchDeviceId(),
                deviceName = deviceInfoProvider.fetchDeviceName(),
            )
            authInteractor.registerNewAccount(credentials.mapToDomain(), device).handle(
                onLeftAction = { emit(EffectResult(RegisterEffect.ShowError(it))) },
                onRightAction = {
                    val targetScreen = screenProvider.provideFeatureScreen(AuthScreen.Verification)
                    emit(EffectResult(RegisterEffect.ReplaceScreen(targetScreen)))
                },
            )
        }.onStart {
            emit(ActionResult(RegisterAction.UpdateLoading(true)))
        }.onCompletion {
            emit(ActionResult(RegisterAction.UpdateLoading(false)))
        }
    }
}

internal sealed class RegisterWorkCommand : WorkCommand {
    data class RegisterNewAccount(val credentials: RegisterCredentialsUi) : RegisterWorkCommand()
}

internal typealias RegisterWorkResult = WorkResult<RegisterAction, RegisterEffect>