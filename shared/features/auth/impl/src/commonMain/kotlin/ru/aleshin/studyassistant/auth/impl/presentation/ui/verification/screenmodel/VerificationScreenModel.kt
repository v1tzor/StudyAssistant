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

package ru.aleshin.studyassistant.auth.impl.presentation.ui.verification.screenmodel

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import org.kodein.di.instance
import ru.aleshin.studyassistant.auth.impl.di.holder.AuthFeatureDIHolder
import ru.aleshin.studyassistant.auth.impl.presentation.ui.verification.contract.VerificationAction
import ru.aleshin.studyassistant.auth.impl.presentation.ui.verification.contract.VerificationEffect
import ru.aleshin.studyassistant.auth.impl.presentation.ui.verification.contract.VerificationEvent
import ru.aleshin.studyassistant.auth.impl.presentation.ui.verification.contract.VerificationViewState
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.BaseScreenModel
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.EmptyDeps
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager

/**
 * @author Stanislav Aleshin on 29.08.2024
 */
internal class VerificationScreenModel(
    private val workProcessor: VerificationWorkProcessor,
    stateCommunicator: VerificationStateCommunicator,
    effectCommunicator: VerificationEffectCommunicator,
    coroutineManager: CoroutineManager,
) : BaseScreenModel<VerificationViewState, VerificationEvent, VerificationAction, VerificationEffect, EmptyDeps>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun init(deps: EmptyDeps) {
        if (!isInitialize) {
            super.init(deps)
            dispatchEvent(VerificationEvent.Init)
        }
    }

    override suspend fun WorkScope<VerificationViewState, VerificationAction, VerificationEffect>.handleEvent(
        event: VerificationEvent,
    ) {
        when (event) {
            is VerificationEvent.Init -> {
                launchBackgroundWork(BackgroundKey.LOAD_APP_USER) {
                    val command = VerificationWorkCommand.LoadAppUser
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_VERIFY_STATUS) {
                    val command = VerificationWorkCommand.LoadVerifyStatus
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is VerificationEvent.SendEmailVerification -> {
                launchBackgroundWork(BackgroundKey.SEND_EMAIL) {
                    val command = VerificationWorkCommand.SendEmailVerification
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is VerificationEvent.SignOut -> {
                launchBackgroundWork(BackgroundKey.SIGN_OUT) {
                    val command = VerificationWorkCommand.SignOut
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
        }
    }

    override suspend fun reduce(
        action: VerificationAction,
        currentState: VerificationViewState,
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
}

@Composable
internal fun Screen.rememberVerificationScreenModel(): VerificationScreenModel {
    val di = AuthFeatureDIHolder.fetchDI()
    return rememberScreenModel { di.instance<VerificationScreenModel>() }
}