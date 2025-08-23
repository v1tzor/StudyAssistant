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

import ru.aleshin.studyassistant.auth.impl.presentation.ui.login.contract.LoginAction
import ru.aleshin.studyassistant.auth.impl.presentation.ui.login.contract.LoginEffect
import ru.aleshin.studyassistant.auth.impl.presentation.ui.login.contract.LoginEvent
import ru.aleshin.studyassistant.auth.impl.presentation.ui.login.contract.LoginOutput
import ru.aleshin.studyassistant.auth.impl.presentation.ui.login.contract.LoginState
import ru.aleshin.studyassistant.auth.impl.presentation.validation.EmailValidator
import ru.aleshin.studyassistant.auth.impl.presentation.validation.PasswordValidator
import ru.aleshin.studyassistant.core.common.architecture.component.EmptyInput
import ru.aleshin.studyassistant.core.common.architecture.component.OutputConsumer
import ru.aleshin.studyassistant.core.common.architecture.store.BaseOnlyOutComposeStore
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.EffectCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.StateCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.common.validation.operateValidate

/**
 * @author Stanislav Aleshin on 16.04.2024
 */
internal class LoginComposeStore(
    private val workProcessor: LoginWorkProcessor,
    private val emailValidator: EmailValidator,
    private val passwordValidator: PasswordValidator,
    outputConsumer: OutputConsumer<LoginOutput>,
    stateCommunicator: StateCommunicator<LoginState>,
    effectCommunicator: EffectCommunicator<LoginEffect>,
    coroutineManager: CoroutineManager,
) : BaseOnlyOutComposeStore<LoginState, LoginEvent, LoginAction, LoginEffect, LoginOutput>(
    outputConsumer = outputConsumer,
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun initialize(input: EmptyInput) {
        if (!isInit.value) {
            dispatchEvent(LoginEvent.Started)
            isInit.value = true
        }
    }

    override suspend fun WorkScope<LoginState, LoginAction, LoginEffect, LoginOutput>.handleEvent(
        event: LoginEvent
    ) {
        when (event) {
            is LoginEvent.Started -> {
                launchBackgroundWork(BackgroundKey.CHECK_GOOGLE) {
                    val command = LoginWorkCommand.CheckGoogleAvailable
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is LoginEvent.SubmitCredentials -> launchBackgroundWork(BackgroundKey.LOGIN) {
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
            is LoginEvent.SocialNetworkAuthSucceeded -> launchBackgroundWork(BackgroundKey.LOGIN_VIA_OAUTH) {
                val command = LoginWorkCommand.SuccessOAuthLogin(event.session)
                workProcessor.work(command).collectAndHandleWork()
            }
            is LoginEvent.ClickForgotPassword -> {
                consumeOutput(LoginOutput.NavigateToRecovery)
            }
            is LoginEvent.ClickSignUp -> {
                consumeOutput(LoginOutput.NavigateToSignUp)
            }
        }
    }

    override suspend fun reduce(
        action: LoginAction,
        currentState: LoginState,
    ) = when (action) {
        is LoginAction.UpdateValidErrors -> currentState.copy(
            isLoading = false,
            emailValidError = action.email,
            passwordValidError = action.password,
        )
        is LoginAction.UpdateGoogleAvailability -> currentState.copy(
            isAvailableGoogle = action.isAvailable,
        )
        is LoginAction.UpdateLoading -> currentState.copy(
            isLoading = action.isLoading,
            emailValidError = null,
            passwordValidError = null,
        )
    }

    enum class BackgroundKey : BackgroundWorkKey {
        CHECK_GOOGLE, LOGIN, LOGIN_VIA_OAUTH
    }

    class Factory(
        private val workProcessor: LoginWorkProcessor,
        private val emailValidator: EmailValidator,
        private val passwordValidator: PasswordValidator,
        private val coroutineManager: CoroutineManager,
    ) : BaseOnlyOutComposeStore.Factory<LoginComposeStore, LoginState, LoginOutput> {

        override fun create(
            savedState: LoginState,
            input: EmptyInput,
            output: OutputConsumer<LoginOutput>,
        ): LoginComposeStore {
            return LoginComposeStore(
                workProcessor = workProcessor,
                emailValidator = emailValidator,
                passwordValidator = passwordValidator,
                outputConsumer = output,
                stateCommunicator = StateCommunicator.Default(savedState),
                effectCommunicator = EffectCommunicator.Default(),
                coroutineManager = coroutineManager,
            )
        }
    }
}