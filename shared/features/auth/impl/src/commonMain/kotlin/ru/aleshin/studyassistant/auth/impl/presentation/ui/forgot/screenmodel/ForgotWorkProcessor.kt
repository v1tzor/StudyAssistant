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

package ru.aleshin.studyassistant.auth.impl.presentation.ui.forgot.screenmodel

import kotlinx.coroutines.flow.flow
import ru.aleshin.studyassistant.auth.api.navigation.AuthScreen
import ru.aleshin.studyassistant.auth.impl.domain.interactors.AuthInteractor
import ru.aleshin.studyassistant.auth.impl.navigation.AuthScreenProvider
import ru.aleshin.studyassistant.auth.impl.presentation.mappers.mapToDomain
import ru.aleshin.studyassistant.auth.impl.presentation.models.credentials.ForgotCredentialsUi
import ru.aleshin.studyassistant.auth.impl.presentation.ui.forgot.contract.ForgotAction
import ru.aleshin.studyassistant.auth.impl.presentation.ui.forgot.contract.ForgotEffect
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.ActionResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.EffectResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.FlowWorkProcessor
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkCommand
import ru.aleshin.studyassistant.core.common.functional.handle

/**
 * @author Stanislav Aleshin on 20.04.2024.
 */
internal interface ForgotWorkProcessor :
    FlowWorkProcessor<ForgotWorkCommand, ForgotAction, ForgotEffect> {

    class Base(
        private val screenProvider: AuthScreenProvider,
        private val authInteractor: AuthInteractor,
    ) : ForgotWorkProcessor {

        override suspend fun work(command: ForgotWorkCommand) = when (command) {
            is ForgotWorkCommand.SendResetPasswordEmail -> sendResetPasswordEmailWork(command.credentialsUi)
        }

        private fun sendResetPasswordEmailWork(credentials: ForgotCredentialsUi) = flow {
            emit(ActionResult(ForgotAction.UpdateLoading(true)))
            authInteractor.resetPassword(credentials.mapToDomain()).handle(
                onLeftAction = { emit(EffectResult(ForgotEffect.ShowError(it))) },
                onRightAction = {
                    val targetScreen = screenProvider.provideFeatureScreen(AuthScreen.Login)
                    emit(EffectResult(ForgotEffect.NavigateToLocal(targetScreen)))
                },
            )
            emit(ActionResult(ForgotAction.UpdateLoading(false)))
        }
    }
}

internal sealed class ForgotWorkCommand : WorkCommand {
    data class SendResetPasswordEmail(val credentialsUi: ForgotCredentialsUi) : ForgotWorkCommand()
}