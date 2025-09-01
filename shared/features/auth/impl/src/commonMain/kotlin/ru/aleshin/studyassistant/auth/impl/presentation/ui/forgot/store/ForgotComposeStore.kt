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

package ru.aleshin.studyassistant.auth.impl.presentation.ui.forgot.store

import ru.aleshin.studyassistant.auth.impl.presentation.ui.forgot.contract.ForgotAction
import ru.aleshin.studyassistant.auth.impl.presentation.ui.forgot.contract.ForgotEffect
import ru.aleshin.studyassistant.auth.impl.presentation.ui.forgot.contract.ForgotEvent
import ru.aleshin.studyassistant.auth.impl.presentation.ui.forgot.contract.ForgotOutput
import ru.aleshin.studyassistant.auth.impl.presentation.ui.forgot.contract.ForgotState
import ru.aleshin.studyassistant.auth.impl.presentation.validation.EmailValidator
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
internal class ForgotComposeStore(
    private val workProcessor: ForgotWorkProcessor,
    private val emailValidator: EmailValidator,
    stateCommunicator: StateCommunicator<ForgotState>,
    effectCommunicator: EffectCommunicator<ForgotEffect>,
    coroutineManager: CoroutineManager,
) : BaseOnlyOutComposeStore<ForgotState, ForgotEvent, ForgotAction, ForgotEffect, ForgotOutput>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override suspend fun WorkScope<ForgotState, ForgotAction, ForgotEffect, ForgotOutput>.handleEvent(
        event: ForgotEvent,
    ) {
        when (event) {
            is ForgotEvent.ClickResetPassword -> launchBackgroundWork(BackgroundKey.RESET_PASSWORD) {
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
            is ForgotEvent.ClickLogin -> {
                consumeOutput(ForgotOutput.NavigateToLogin)
            }
        }
    }

    override suspend fun reduce(
        action: ForgotAction,
        currentState: ForgotState,
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

    class Factory(
        private val workProcessor: ForgotWorkProcessor,
        private val emailValidator: EmailValidator,
        private val coroutineManager: CoroutineManager,
    ) : BaseOnlyOutComposeStore.Factory<ForgotComposeStore, ForgotState> {

        override fun create(savedState: ForgotState): ForgotComposeStore {
            return ForgotComposeStore(
                workProcessor = workProcessor,
                emailValidator = emailValidator,
                stateCommunicator = StateCommunicator.Default(savedState),
                effectCommunicator = EffectCommunicator.Default(),
                coroutineManager = coroutineManager,
            )
        }
    }
}