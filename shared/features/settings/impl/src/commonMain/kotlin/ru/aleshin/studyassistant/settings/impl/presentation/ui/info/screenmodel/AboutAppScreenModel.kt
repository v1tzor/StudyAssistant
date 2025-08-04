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

package ru.aleshin.studyassistant.settings.impl.presentation.ui.info.screenmodel

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.common.architecture.communications.state.EffectCommunicator
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.BaseScreenModel
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.EmptyDeps
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.settings.impl.di.holder.SettingsFeatureDIHolder
import ru.aleshin.studyassistant.settings.impl.presentation.ui.info.contract.AboutAppAction
import ru.aleshin.studyassistant.settings.impl.presentation.ui.info.contract.AboutAppEffect
import ru.aleshin.studyassistant.settings.impl.presentation.ui.info.contract.AboutAppEvent
import ru.aleshin.studyassistant.settings.impl.presentation.ui.info.contract.AboutAppViewState

/**
 * @author Stanislav Aleshin on 04.08.2025
 */
internal class AboutAppScreenModel(
    stateCommunicator: AboutAppStateCommunicator,
    coroutineManager: CoroutineManager,
) : BaseScreenModel<AboutAppViewState, AboutAppEvent, AboutAppAction, AboutAppEffect, EmptyDeps>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = EffectCommunicator.Empty(),
    coroutineManager = coroutineManager,
) {

    override suspend fun WorkScope<AboutAppViewState, AboutAppAction, AboutAppEffect>.handleEvent(
        event: AboutAppEvent,
    ) = Unit

    override suspend fun reduce(action: AboutAppAction, currentState: AboutAppViewState) = currentState
}

@Composable
internal fun Screen.rememberAboutAppScreenModel(): AboutAppScreenModel {
    val di = SettingsFeatureDIHolder.fetchDI()
    return rememberScreenModel { di.instance<AboutAppScreenModel>() }
}