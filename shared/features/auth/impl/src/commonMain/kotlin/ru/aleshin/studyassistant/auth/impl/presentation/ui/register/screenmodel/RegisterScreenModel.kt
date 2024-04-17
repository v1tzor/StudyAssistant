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
import architecture.screenmodel.work.WorkScope
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import managers.CoroutineManager
import org.kodein.di.instance
import ru.aleshin.studyassistant.auth.impl.di.holder.AuthFeatureDIHolder
import ru.aleshin.studyassistant.auth.impl.presentation.ui.register.contract.RegisterAction
import ru.aleshin.studyassistant.auth.impl.presentation.ui.register.contract.RegisterEffect
import ru.aleshin.studyassistant.auth.impl.presentation.ui.register.contract.RegisterEvent
import ru.aleshin.studyassistant.auth.impl.presentation.ui.register.contract.RegisterViewState

/**
 * @author Stanislav Aleshin on 17.04.2024
 */
internal class RegisterScreenModel(
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
    ) = when (event) {
        is RegisterEvent.NavigateToLogin -> TODO()
        is RegisterEvent.PressRegisterButton -> TODO()
    }

    override suspend fun reduce(
        action: RegisterAction,
        currentState: RegisterViewState,
    ) = when (action) {
        is RegisterAction.UpdateLoading -> currentState.copy(
            isLoading = action.isLoading,
        )
        is RegisterAction.UpdateValidErrors -> currentState.copy(
            isLoading = false,
        )
    }
}

@Composable
internal fun Screen.rememberRegisterScreenModel(): RegisterScreenModel {
    val di = AuthFeatureDIHolder.fetchDI()
    return rememberScreenModel { di.instance<RegisterScreenModel>() }
}