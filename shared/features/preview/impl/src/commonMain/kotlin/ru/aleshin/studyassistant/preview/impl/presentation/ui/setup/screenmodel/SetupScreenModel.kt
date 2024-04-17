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

package ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.screenmodel

import androidx.compose.runtime.Composable
import architecture.screenmodel.BaseScreenModel
import architecture.screenmodel.work.WorkScope
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.kodein.rememberScreenModel
import managers.CoroutineManager
import org.kodein.di.instance
import ru.aleshin.studyassistant.preview.impl.di.holder.PreviewFeatureDIHolder
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.contract.SetupAction
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.contract.SetupDeps
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.contract.SetupEffect
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.contract.SetupEvent
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.contract.SetupViewState

/**
 * @author Stanislav Aleshin on 17.04.2024
 */
internal class SetupScreenModel(
    stateCommunicator: SetupStateCommunicator,
    effectCommunicator: SetupEffectCommunicator,
    coroutineManager: CoroutineManager,
) : BaseScreenModel<SetupViewState, SetupEvent, SetupAction, SetupEffect, SetupDeps>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun init(deps: SetupDeps) {
        if (!isInitialize) {
            super.init(deps)
            // TODO: Init
        }
    }

    override suspend fun WorkScope<SetupViewState, SetupAction, SetupEffect>.handleEvent(
        event: SetupEvent,
    ) = when (event) {
        else -> {}
    }

    override suspend fun reduce(
        action: SetupAction,
        currentState: SetupViewState,
    ) = when (action) {
        else -> currentState
    }
}

@Composable
internal fun Screen.rememberSetupScreenModel(): SetupScreenModel {
    val di = PreviewFeatureDIHolder.fetchDI()
    return rememberScreenModel { di.instance<SetupScreenModel>() }
}