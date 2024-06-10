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

import androidx.compose.runtime.Composable
import architecture.screenmodel.BaseScreenModel
import architecture.screenmodel.EmptyDeps
import architecture.screenmodel.work.BackgroundWorkKey
import architecture.screenmodel.work.WorkScope
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import managers.CoroutineManager
import org.kodein.di.instance
import ru.aleshin.studyassistant.auth.api.navigation.AuthScreen
import ru.aleshin.studyassistant.auth.impl.di.holder.AuthFeatureDIHolder
import ru.aleshin.studyassistant.auth.impl.navigation.AuthScreenProvider
import ru.aleshin.studyassistant.auth.impl.presentation.ui.login.contract.LoginAction
import ru.aleshin.studyassistant.auth.impl.presentation.ui.login.contract.LoginEffect
import ru.aleshin.studyassistant.auth.impl.presentation.ui.login.contract.LoginEvent
import ru.aleshin.studyassistant.auth.impl.presentation.ui.login.contract.LoginViewState
import ru.aleshin.studyassistant.auth.impl.presentation.validation.EmailValidator
import ru.aleshin.studyassistant.auth.impl.presentation.validation.PasswordValidator
import validation.operateValidate

/**
 * @author Stanislav Aleshin on 16.04.2024
 */
internal class LoginScreenModel(
    private val workProcessor: LoginWorkProcessor,
    private val screenProvider: AuthScreenProvider,
    private val emailValidator: EmailValidator,
    private val passwordValidator: PasswordValidator,
    stateCommunicator: LoginStateCommunicator,
    effectCommunicator: LoginEffectCommunicator,
    coroutineManager: CoroutineManager,
) : BaseScreenModel<LoginViewState, LoginEvent, LoginAction, LoginEffect, EmptyDeps>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override suspend fun WorkScope<LoginViewState, LoginAction, LoginEffect>.handleEvent(
        event: LoginEvent,
    ) {
        when (event) {
            is LoginEvent.LoginWithEmail -> launchBackgroundWork(BackgroundKey.LOGIN) {
                val emailValidResult = emailValidator.validate(event.credentials.email)
                val passwordValidResult = passwordValidator.validate(event.credentials.password)
                operateValidate(
                    isSuccess = {
                        val command = LoginWorkCommand.LoginWithEmail(event.credentials)
                        workProcessor.work(command).collectAndHandleWork()
                    },
                    isError = {
                        val action = LoginAction.UpdateValidErrors(
                            email = emailValidResult.validError,
                            password = passwordValidResult.validError
                        )
                        sendAction(action)
                    },
                    emailValidResult.isValid,
                    passwordValidResult.isValid,
                )
            }
            is LoginEvent.LoginViaGoogle -> launchBackgroundWork(BackgroundKey.LOGIN_VIA_GOOGLE) {
                val command = LoginWorkCommand.LoginWithGoogle(event.idToken)
                workProcessor.work(command).collectAndHandleWork()
            }
            is LoginEvent.NavigateToForgot -> {
                val screen = screenProvider.provideFeatureScreen(AuthScreen.Forgot)
                sendEffect(LoginEffect.PushScreen(screen))
            }
            is LoginEvent.NavigateToRegister -> {
                val screen = screenProvider.provideFeatureScreen(AuthScreen.Register)
                sendEffect(LoginEffect.PushScreen(screen))
            }
        }
    }

    override suspend fun reduce(
        action: LoginAction,
        currentState: LoginViewState,
    ) = when (action) {
        is LoginAction.UpdateLoading -> currentState.copy(
            isLoading = action.isLoading,
            emailValidError = null,
            passwordValidError = null,
        )
        is LoginAction.UpdateValidErrors -> currentState.copy(
            isLoading = false,
            emailValidError = action.email,
            passwordValidError = action.password,
        )
    }

    enum class BackgroundKey : BackgroundWorkKey {
        LOGIN, LOGIN_VIA_GOOGLE
    }
}

@Composable
internal fun Screen.rememberLoginScreenModel(): LoginScreenModel {
    val di = AuthFeatureDIHolder.fetchDI()
    return rememberScreenModel { di.instance<LoginScreenModel>() }
}