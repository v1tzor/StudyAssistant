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

package ru.aleshin.studyassistant.preview.impl.presentation.ui.intro.screenmodel

import androidx.compose.runtime.Composable
import architecture.screenmodel.BaseScreenModel
import architecture.screenmodel.EmptyDeps
import architecture.screenmodel.work.WorkScope
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import managers.CoroutineManager
import org.kodein.di.instance
import ru.aleshin.studyassistant.auth.api.navigation.AuthScreen
import ru.aleshin.studyassistant.preview.impl.di.holder.PreviewFeatureDIHolder
import ru.aleshin.studyassistant.preview.impl.navigation.FeatureScreenProvider
import ru.aleshin.studyassistant.preview.impl.presentation.ui.intro.contract.IntroAction
import ru.aleshin.studyassistant.preview.impl.presentation.ui.intro.contract.IntroEffect
import ru.aleshin.studyassistant.preview.impl.presentation.ui.intro.contract.IntroEvent
import ru.aleshin.studyassistant.preview.impl.presentation.ui.intro.contract.IntroViewState

/**
 * @author Stanislav Aleshin on 14.04.2024
 */
internal class IntroScreenModel constructor(
    private val screenProvider: FeatureScreenProvider,
    stateCommunicator: IntroStateCommunicator,
    effectCommunicator: IntroEffectCommunicator,
    coroutineManager: CoroutineManager,
) : BaseScreenModel<IntroViewState, IntroEvent, IntroAction, IntroEffect, EmptyDeps>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override suspend fun WorkScope<IntroViewState, IntroAction, IntroEffect>.handleEvent(
        event: IntroEvent,
    ) = when (event) {
        is IntroEvent.NavigateToLogin -> {
            val screen = screenProvider.provideAuthScreen(AuthScreen.Login)
            sendEffect(IntroEffect.ReplaceGlobalScreen(screen))
        }
        is IntroEvent.NavigateToRegister -> {
            val screen = screenProvider.provideAuthScreen(AuthScreen.Register)
            sendEffect(IntroEffect.ReplaceGlobalScreen(screen))
        }
        is IntroEvent.NextPage -> {
            sendEffect(IntroEffect.ScrollToPage(event.currentPage + 1))
        }
        is IntroEvent.PreviousPage -> {
            sendEffect(IntroEffect.ScrollToPage(event.currentPage - 1))
        }
    }

    override suspend fun reduce(action: IntroAction, currentState: IntroViewState) = IntroViewState.Default
}

@Composable
internal fun Screen.rememberIntroScreenModel(): IntroScreenModel {
    val di = PreviewFeatureDIHolder.fetchDI()
    return rememberScreenModel { di.instance<IntroScreenModel>() }
}