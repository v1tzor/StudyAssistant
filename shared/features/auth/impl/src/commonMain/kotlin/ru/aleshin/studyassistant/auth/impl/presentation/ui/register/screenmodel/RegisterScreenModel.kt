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
import ru.aleshin.studyassistant.auth.impl.presentation.ui.register.contract.RegisterAction
import ru.aleshin.studyassistant.auth.impl.presentation.ui.register.contract.RegisterEffect
import ru.aleshin.studyassistant.auth.impl.presentation.ui.register.contract.RegisterEvent
import ru.aleshin.studyassistant.auth.impl.presentation.ui.register.contract.RegisterViewState
import ru.aleshin.studyassistant.auth.impl.presentation.validation.EmailValidator
import ru.aleshin.studyassistant.auth.impl.presentation.validation.PasswordValidator
import ru.aleshin.studyassistant.auth.impl.presentation.validation.UsernameValidator
import ru.aleshin.studyassistant.preview.api.navigation.PreviewScreen
import validation.operateValidate

/**
 * @author Stanislav Aleshin on 17.04.2024
 */
internal class RegisterScreenModel(
    private val screenProvider: AuthScreenProvider,
    private val workProcessor: RegisterWorkProcessor,
    private val usernameValidator: UsernameValidator,
    private val emailValidator: EmailValidator,
    private val passwordValidator: PasswordValidator,
    stateCommunicator: RegisterStateCommunicator,
    effectCommunicator: RegisterEffectCommunicator,
    coroutineManager: CoroutineManager,
) : BaseScreenModel<RegisterViewState, RegisterEvent, RegisterAction, RegisterEffect, EmptyDeps>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override suspend fun WorkScope<RegisterViewState, RegisterAction, RegisterEffect>.handleEvent(
        event: RegisterEvent,
    ) {
        when (event) {
            is RegisterEvent.RegisterNewAccount -> launchBackgroundWork(RegisterWorkKey.REGISTER) {
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
            is RegisterEvent.NavigateToLogin -> {
                val screen = screenProvider.provideFeatureScreen(AuthScreen.Login)
                sendEffect(RegisterEffect.PushScreen(screen))
            }
        }
    }

    override suspend fun reduce(
        action: RegisterAction,
        currentState: RegisterViewState,
    ) = when (action) {
        is RegisterAction.UpdateLoading -> currentState.copy(
            isLoading = action.isLoading,
            usernameValidError = null,
            emailValidError = null,
            passwordValidError = null,
        )
        is RegisterAction.UpdateValidErrors -> currentState.copy(
            isLoading = false,
            usernameValidError = action.username,
            emailValidError = action.email,
            passwordValidError = action.password,
        )
    }
}

internal enum class RegisterWorkKey : BackgroundWorkKey {
    REGISTER,
}

@Composable
internal fun Screen.rememberRegisterScreenModel(): RegisterScreenModel {
    val di = AuthFeatureDIHolder.fetchDI()
    return rememberScreenModel { di.instance<RegisterScreenModel>() }
}