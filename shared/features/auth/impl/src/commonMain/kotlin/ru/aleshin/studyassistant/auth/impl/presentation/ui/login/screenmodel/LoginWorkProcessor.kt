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

package ru.aleshin.studyassistant.auth.impl.presentation.ui.login.screenmodel

import architecture.screenmodel.work.ActionResult
import architecture.screenmodel.work.EffectResult
import architecture.screenmodel.work.FlowWorkProcessor
import architecture.screenmodel.work.WorkCommand
import functional.handle
import kotlinx.coroutines.flow.flow
import ru.aleshin.studyassistant.auth.impl.domain.interactors.AuthInteractor
import ru.aleshin.studyassistant.auth.impl.navigation.AuthScreenProvider
import ru.aleshin.studyassistant.auth.impl.presentation.models.LoginCredentialsUi
import ru.aleshin.studyassistant.auth.impl.presentation.ui.login.contract.LoginAction
import ru.aleshin.studyassistant.auth.impl.presentation.ui.login.contract.LoginEffect

/**
 * @author Stanislav Aleshin on 20.04.2024.
 */
internal interface LoginWorkProcessor :
    FlowWorkProcessor<LoginWorkCommand, LoginAction, LoginEffect> {

    class Base(
        private val authInteractor: AuthInteractor,
        private val screenProvider: AuthScreenProvider,
    ) : LoginWorkProcessor {

        override suspend fun work(command: LoginWorkCommand) = when (command) {
            is LoginWorkCommand.LoginWithEmail -> loginWithEmailWork(command.credentials)
            is LoginWorkCommand.LoginWithGoogle -> loginWithGoogleWork(command.idToken)
        }

        private fun loginWithEmailWork(credentials: LoginCredentialsUi) = flow {
            emit(ActionResult(LoginAction.UpdateLoading(true)))
            authInteractor.loginWithEmail(credentials.mapToDomain()).handle(
                onLeftAction = {
                    emit(EffectResult(LoginEffect.ShowError(it)))
                    emit(ActionResult(LoginAction.UpdateLoading(false)))
                },
                onRightAction = {
                    val tabScreen = screenProvider.provideTabNavigationScreen()
                    emit(ActionResult(LoginAction.UpdateLoading(false)))
                    emit(EffectResult(LoginEffect.ReplaceGlobalScreen(tabScreen)))
                }
            )
        }

        private fun loginWithGoogleWork(idToken: String?) = flow {
            authInteractor.loginViaGoogle(idToken).handle(
                onLeftAction = { emit(EffectResult(LoginEffect.ShowError(it))) },
                onRightAction = {
                    val tabScreen = screenProvider.provideTabNavigationScreen()
                    emit(EffectResult(LoginEffect.ReplaceGlobalScreen(tabScreen)))
                }
            )
        }
    }
}

internal sealed class LoginWorkCommand : WorkCommand {
    data class LoginWithEmail(val credentials: LoginCredentialsUi) : LoginWorkCommand()
    data class LoginWithGoogle(val idToken: String?) : LoginWorkCommand()
}