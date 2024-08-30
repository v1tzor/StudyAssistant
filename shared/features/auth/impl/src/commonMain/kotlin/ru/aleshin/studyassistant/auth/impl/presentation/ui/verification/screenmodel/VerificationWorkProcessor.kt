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

import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.isActive
import ru.aleshin.studyassistant.auth.api.navigation.AuthScreen
import ru.aleshin.studyassistant.auth.impl.domain.interactors.AppUserInteractor
import ru.aleshin.studyassistant.auth.impl.domain.interactors.AuthInteractor
import ru.aleshin.studyassistant.auth.impl.navigation.AuthScreenProvider
import ru.aleshin.studyassistant.auth.impl.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.auth.impl.presentation.ui.verification.contract.VerificationAction
import ru.aleshin.studyassistant.auth.impl.presentation.ui.verification.contract.VerificationEffect
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.ActionResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.EffectResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.FlowWorkProcessor
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkCommand
import ru.aleshin.studyassistant.core.common.functional.Constants.Date.MILLIS_IN_MINUTE
import ru.aleshin.studyassistant.core.common.functional.Constants.Date.MILLIS_IN_SECONDS
import ru.aleshin.studyassistant.core.common.functional.Constants.Delay.UPDATE_EMAIL_VERIFICATION
import ru.aleshin.studyassistant.core.common.functional.DeviceInfoProvider
import ru.aleshin.studyassistant.core.common.functional.handle
import ru.aleshin.studyassistant.preview.api.navigation.PreviewScreen

/**
 * @author Stanislav Aleshin on 29.08.2024.
 */
internal interface VerificationWorkProcessor :
    FlowWorkProcessor<VerificationWorkCommand, VerificationAction, VerificationEffect> {

    class Base(
        private val authInteractor: AuthInteractor,
        private val appUserInteractor: AppUserInteractor,
        private val deviceInfoProvider: DeviceInfoProvider,
        private val screenProvider: AuthScreenProvider,
    ) : VerificationWorkProcessor {

        override suspend fun work(command: VerificationWorkCommand) = when (command) {
            is VerificationWorkCommand.LoadAppUser -> loadAppUserWork()
            is VerificationWorkCommand.SendEmailVerification -> sendEmailVerificationWork()
            is VerificationWorkCommand.SignOut -> signOutWork()
        }

        private fun loadAppUserWork() = channelFlow {
            var verificationJob: Job? = null
            appUserInteractor.fetchAppUser().collect { userEither ->
                verificationJob?.cancelAndJoin()
                userEither.handle(
                    onLeftAction = { send(EffectResult(VerificationEffect.ShowError(it))) },
                    onRightAction = { user ->
                        send(ActionResult(VerificationAction.UpdateAppUser(user.mapToUi())))
                        verificationJob = cycleCheckEmailVerification()
                            .onEach { send(it) }
                            .launchIn(this)
                            .apply { start() }
                    }
                )
            }
        }

        private fun sendEmailVerificationWork() = flow {
            authInteractor.sendEmailVerification().handle(
                onLeftAction = { emit(EffectResult(VerificationEffect.ShowError(it))) },
                onRightAction = {
                    emit(ActionResult(VerificationAction.UpdateLoadingSend(false)))
                    emitAll(cycleUpdateRetryTime())
                },
            )
        }.onStart {
            emit(ActionResult(VerificationAction.UpdateLoadingSend(true)))
        }

        private fun signOutWork() = flow {
            val deviceId = deviceInfoProvider.fetchDeviceId()
            authInteractor.signOut(deviceId).handle(
                onLeftAction = { emit(EffectResult(VerificationEffect.ShowError(it))) },
                onRightAction = {
                    val authScreen = screenProvider.provideFeatureScreen(AuthScreen.Login)
                    emit(EffectResult(VerificationEffect.ReplaceScreen(authScreen)))
                },
            )
        }

        private fun cycleUpdateRetryTime() = flow {
            var leftTime = MILLIS_IN_MINUTE * 2
            while (currentCoroutineContext().isActive) {
                leftTime -= MILLIS_IN_SECONDS
                if (leftTime > 0) {
                    emit(ActionResult(VerificationAction.UpdateRetryAvailableTime(leftTime)))
                } else {
                    emit(ActionResult(VerificationAction.UpdateRetryAvailableTime(null)))
                    break
                }
                delay(MILLIS_IN_SECONDS)
            }
        }

        private fun cycleCheckEmailVerification() = flow {
            while (currentCoroutineContext().isActive) {
                appUserInteractor.checkEmailVerification().handle(
                    onLeftAction = {
                        emit(EffectResult(VerificationEffect.ShowError(it)))
                        currentCoroutineContext().cancel()
                    },
                    onRightAction = { isVerify ->
                        if (isVerify) {
                            val previewScreen = screenProvider.providePreviewScreen(PreviewScreen.Setup)
                            emit(EffectResult(VerificationEffect.ReplaceGlobalScreen(previewScreen)))
                        }
                    },
                )
                delay(UPDATE_EMAIL_VERIFICATION)
            }
        }
    }
}

internal sealed class VerificationWorkCommand : WorkCommand {
    data object LoadAppUser : VerificationWorkCommand()
    data object SendEmailVerification : VerificationWorkCommand()
    data object SignOut : VerificationWorkCommand()
}