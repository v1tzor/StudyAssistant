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

package ru.aleshin.studyassistant.auth.impl.presentation.ui.register.store

import ru.aleshin.studyassistant.auth.impl.presentation.ui.register.contract.RegisterAction
import ru.aleshin.studyassistant.auth.impl.presentation.ui.register.contract.RegisterEffect
import ru.aleshin.studyassistant.auth.impl.presentation.ui.register.contract.RegisterEvent
import ru.aleshin.studyassistant.auth.impl.presentation.ui.register.contract.RegisterOutput
import ru.aleshin.studyassistant.auth.impl.presentation.ui.register.contract.RegisterState
import ru.aleshin.studyassistant.auth.impl.presentation.validation.EmailValidator
import ru.aleshin.studyassistant.auth.impl.presentation.validation.PasswordValidator
import ru.aleshin.studyassistant.auth.impl.presentation.validation.UsernameValidator
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
 * @author Stanislav Aleshin on 17.04.2024
 */
internal class RegisterComposeStore(
    private val workProcessor: RegisterWorkProcessor,
    private val usernameValidator: UsernameValidator,
    private val emailValidator: EmailValidator,
    private val passwordValidator: PasswordValidator,
    outputConsumer: OutputConsumer<RegisterOutput>,
    stateCommunicator: StateCommunicator<RegisterState>,
    effectCommunicator: EffectCommunicator<RegisterEffect>,
    coroutineManager: CoroutineManager,
) : BaseOnlyOutComposeStore<RegisterState, RegisterEvent, RegisterAction, RegisterEffect, RegisterOutput>(
    outputConsumer = outputConsumer,
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun initialize(input: EmptyInput) {
        isInit.value = true
    }

    override suspend fun WorkScope<RegisterState, RegisterAction, RegisterEffect, RegisterOutput>.handleEvent(
        event: RegisterEvent,
    ) {
        when (event) {
            is RegisterEvent.SubmitCredentials -> launchBackgroundWork(BackgroundKey.REGISTER) {
                val usernameValidResult = usernameValidator.validate(event.credentials.username)
                val emailValidResult = emailValidator.validate(event.credentials.email)
                val passwordValidResult = passwordValidator.validate(event.credentials.password)
                operateValidate(
                    isSuccess = {
                        val command = RegisterWorkCommand.RegisterNewAccount(event.credentials)
                        workProcessor.work(command).collectAndHandleWork()
                    },
                    isError = {
                        val action = RegisterAction.UpdateValidErrors(
                            username = usernameValidResult.validError,
                            email = emailValidResult.validError,
                            password = passwordValidResult.validError
                        )
                        sendAction(action)
                    },
                    usernameValidResult.isValid,
                    emailValidResult.isValid,
                    passwordValidResult.isValid,
                )
            }
            is RegisterEvent.ClickLogin -> {
                consumeOutput(RegisterOutput.NavigateToLogin)
            }
        }
    }

    override suspend fun reduce(
        action: RegisterAction,
        currentState: RegisterState,
    ) = when (action) {
        is RegisterAction.UpdateValidErrors -> currentState.copy(
            isLoading = false,
            usernameValidError = action.username,
            emailValidError = action.email,
            passwordValidError = action.password,
        )
        is RegisterAction.UpdateLoading -> currentState.copy(
            isLoading = action.isLoading,
            usernameValidError = null,
            emailValidError = null,
            passwordValidError = null,
        )
    }

    enum class BackgroundKey : BackgroundWorkKey {
        REGISTER,
    }

    class Factory(
        private val workProcessor: RegisterWorkProcessor,
        private val usernameValidator: UsernameValidator,
        private val emailValidator: EmailValidator,
        private val passwordValidator: PasswordValidator,
        private val coroutineManager: CoroutineManager,
    ) : BaseOnlyOutComposeStore.Factory<RegisterComposeStore, RegisterState, RegisterOutput> {

        override fun create(
            savedState: RegisterState,
            input: EmptyInput,
            output: OutputConsumer<RegisterOutput>,
        ): RegisterComposeStore {
            return RegisterComposeStore(
                workProcessor = workProcessor,
                usernameValidator = usernameValidator,
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