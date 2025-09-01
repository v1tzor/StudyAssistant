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

package ru.aleshin.studyassistant.auth.impl.presentation.ui.verification.store

import ru.aleshin.studyassistant.auth.impl.presentation.ui.verification.contract.VerificationAction
import ru.aleshin.studyassistant.auth.impl.presentation.ui.verification.contract.VerificationEffect
import ru.aleshin.studyassistant.auth.impl.presentation.ui.verification.contract.VerificationEvent
import ru.aleshin.studyassistant.auth.impl.presentation.ui.verification.contract.VerificationOutput
import ru.aleshin.studyassistant.auth.impl.presentation.ui.verification.contract.VerificationState
import ru.aleshin.studyassistant.core.common.architecture.store.BaseOnlyOutComposeStore
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.EffectCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.StateCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager

/**
 * @author Stanislav Aleshin on 29.08.2024
 */
internal class VerificationComposeStore(
    private val workProcessor: VerificationWorkProcessor,
    stateCommunicator: StateCommunicator<VerificationState>,
    effectCommunicator: EffectCommunicator<VerificationEffect>,
    coroutineManager: CoroutineManager,
) : BaseOnlyOutComposeStore<VerificationState, VerificationEvent, VerificationAction, VerificationEffect, VerificationOutput>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override suspend fun WorkScope<VerificationState, VerificationAction, VerificationEffect, VerificationOutput>.handleEvent(
        event: VerificationEvent,
    ) {
        when (event) {
            is VerificationEvent.Started -> {
                launchBackgroundWork(BackgroundKey.LOAD_APP_USER) {
                    val command = VerificationWorkCommand.LoadAppUser
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_VERIFY_STATUS) {
                    val command = VerificationWorkCommand.LoadVerifyStatus
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is VerificationEvent.ClickSendEmail -> {
                launchBackgroundWork(BackgroundKey.SEND_EMAIL) {
                    val command = VerificationWorkCommand.SendEmailVerification
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is VerificationEvent.ClickSignOut -> {
                launchBackgroundWork(BackgroundKey.SIGN_OUT) {
                    val command = VerificationWorkCommand.SignOut
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
        }
    }

    override suspend fun reduce(
        action: VerificationAction,
        currentState: VerificationState,
    ) = when (action) {
        is VerificationAction.UpdateAppUser -> currentState.copy(
            appUser = action.appUser,
        )
        is VerificationAction.UpdateRetryAvailableTime -> currentState.copy(
            retryAvailableTime = action.retryAvailableTime,
        )
    }

    enum class BackgroundKey : BackgroundWorkKey {
        LOAD_APP_USER, LOAD_VERIFY_STATUS, SEND_EMAIL, SIGN_OUT
    }

    class Factory(
        private val workProcessor: VerificationWorkProcessor,
        private val coroutineManager: CoroutineManager,
    ) : BaseOnlyOutComposeStore.Factory<VerificationComposeStore, VerificationState> {

        override fun create(savedState: VerificationState): VerificationComposeStore {
            return VerificationComposeStore(
                workProcessor = workProcessor,
                stateCommunicator = StateCommunicator.Default(savedState),
                effectCommunicator = EffectCommunicator.Default(),
                coroutineManager = coroutineManager,
            )
        }
    }
}