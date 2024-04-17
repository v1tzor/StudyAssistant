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
import architecture.screenmodel.BaseScreenModel
import architecture.screenmodel.EmptyDeps
import architecture.screenmodel.work.WorkScope
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import managers.CoroutineManager
import org.kodein.di.instance
import ru.aleshin.studyassistant.auth.impl.di.holder.AuthFeatureDIHolder
import ru.aleshin.studyassistant.auth.impl.presentation.ui.forgot.contract.ForgotAction
import ru.aleshin.studyassistant.auth.impl.presentation.ui.forgot.contract.ForgotEffect
import ru.aleshin.studyassistant.auth.impl.presentation.ui.forgot.contract.ForgotEvent
import ru.aleshin.studyassistant.auth.impl.presentation.ui.forgot.contract.ForgotViewState

/**
 * @author Stanislav Aleshin on 17.04.2024
 */
internal class ForgotScreenModel(
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
    ) = when (event) {
        is ForgotEvent.NavigateToLogin -> TODO()
        is ForgotEvent.PressSendEmailButton -> TODO()
    }

    override suspend fun reduce(
        action: ForgotAction,
        currentState: ForgotViewState,
    ) = when (action) {
        is ForgotAction.UpdateLoading -> currentState.copy(
            isLoading = action.isLoading,
        )
        is ForgotAction.UpdateValidError ->  currentState.copy(
            isLoading = false,
            emailValidError = action.email,
        )
    }
}

@Composable
internal fun Screen.rememberForgotScreenModel(): ForgotScreenModel {
    val di = AuthFeatureDIHolder.fetchDI()
    return rememberScreenModel { di.instance<ForgotScreenModel>() }
}