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

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import org.kodein.di.instance
import ru.aleshin.studyassistant.auth.api.navigation.AuthScreen
import ru.aleshin.studyassistant.auth.impl.di.holder.AuthFeatureDIHolder
import ru.aleshin.studyassistant.auth.impl.navigation.AuthScreenProvider
import ru.aleshin.studyassistant.auth.impl.presentation.ui.forgot.contract.ForgotAction
import ru.aleshin.studyassistant.auth.impl.presentation.ui.forgot.contract.ForgotEffect
import ru.aleshin.studyassistant.auth.impl.presentation.ui.forgot.contract.ForgotEvent
import ru.aleshin.studyassistant.auth.impl.presentation.ui.forgot.contract.ForgotViewState
import ru.aleshin.studyassistant.auth.impl.presentation.validation.EmailValidator
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.BaseScreenModel
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.EmptyDeps
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.common.validation.operateValidate

/**
 * @author Stanislav Aleshin on 17.04.2024
 */
internal class ForgotScreenModel(
    private val workProcessor: ForgotWorkProcessor,
    private val screenProvider: AuthScreenProvider,
    private val emailValidator: EmailValidator,
    stateCommunicator: ForgotStateCommunicator,
    effectCommunicator: ForgotEffectCommunicator,
    coroutineManager: CoroutineManager,
) : BaseScreenModel<ForgotViewState, ForgotEvent, ForgotAction, ForgotEffect, EmptyDeps>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override suspend fun WorkScope<ForgotViewState, ForgotAction, ForgotEffect>.handleEvent(
        event: ForgotEvent,
    ) {
        when (event) {
            is ForgotEvent.SendResetPasswordEmail -> launchBackgroundWork(BackgroundKey.RESET_PASSWORD) {
                val emailValidResult = emailValidator.validate(event.credentials.email)
                operateValidate(
                    isSuccess = {
                        val command = ForgotWorkCommand.SendResetPasswordEmail(event.credentials)
                        workProcessor.work(command).collectAndHandleWork()
                    },
                    isError = {
                        val action = ForgotAction.UpdateValidError(
                            email = emailValidResult.validError,
                        )
                        sendAction(action)
                    },
                    emailValidResult.isValid,
                )
            }
            is ForgotEvent.NavigateToLogin -> {
                val screen = screenProvider.provideFeatureScreen(AuthScreen.Login)
                sendEffect(ForgotEffect.NavigateToLocal(screen))
            }
        }
    }

    override suspend fun reduce(
        action: ForgotAction,
        currentState: ForgotViewState,
    ) = when (action) {
        is ForgotAction.UpdateValidError -> currentState.copy(
            isLoading = false,
            emailValidError = action.email,
        )
        is ForgotAction.UpdateLoading -> currentState.copy(
            isLoading = action.isLoading,
            emailValidError = null,
        )
    }

    enum class BackgroundKey : BackgroundWorkKey {
        RESET_PASSWORD,
    }
}

@Composable
internal fun Screen.rememberForgotScreenModel(): ForgotScreenModel {
    val di = AuthFeatureDIHolder.fetchDI()
    return rememberScreenModel { di.instance<ForgotScreenModel>() }
}