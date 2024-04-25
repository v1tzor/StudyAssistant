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

import architecture.screenmodel.work.ActionResult
import architecture.screenmodel.work.EffectResult
import architecture.screenmodel.work.FlowWorkProcessor
import architecture.screenmodel.work.WorkCommand
import functional.handle
import kotlinx.coroutines.flow.flow
import ru.aleshin.studyassistant.auth.impl.domain.interactors.AuthInteractor
import ru.aleshin.studyassistant.auth.impl.navigation.AuthScreenProvider
import ru.aleshin.studyassistant.auth.impl.presentation.models.RegisterCredentialsUi
import ru.aleshin.studyassistant.auth.impl.presentation.ui.login.contract.LoginEffect
import ru.aleshin.studyassistant.auth.impl.presentation.ui.register.contract.RegisterAction
import ru.aleshin.studyassistant.auth.impl.presentation.ui.register.contract.RegisterEffect
import ru.aleshin.studyassistant.preview.api.navigation.PreviewScreen

/**
 * @author Stanislav Aleshin on 20.04.2024.
 */
internal interface RegisterWorkProcessor :
    FlowWorkProcessor<RegisterWorkCommand, RegisterAction, RegisterEffect> {

    class Base(
        private val screenProvider: AuthScreenProvider,
        private val authInteractor: AuthInteractor,
    ) : RegisterWorkProcessor {

        override suspend fun work(command: RegisterWorkCommand) = when (command) {
            is RegisterWorkCommand.RegisterNewAccount -> registerNewAccountWork(command.credentialsUi)
        }

        private fun registerNewAccountWork(credentials: RegisterCredentialsUi) = flow {
            emit(ActionResult(RegisterAction.UpdateLoading(true)))
            authInteractor.registerNewAccount(credentials.mapToDomain()).handle(
                onLeftAction = {
                    emit(EffectResult(RegisterEffect.ShowError(it)))
                    emit(ActionResult(RegisterAction.UpdateLoading(false)))
                },
                onRightAction = {
                    val setupScreen = screenProvider.providePreviewScreen(PreviewScreen.Setup)
                    emit(EffectResult(RegisterEffect.ReplaceGlobalScreen(setupScreen)))
                },
            )
        }
    }
}

internal sealed class RegisterWorkCommand : WorkCommand {
    data class RegisterNewAccount(val credentialsUi: RegisterCredentialsUi) : RegisterWorkCommand()
}