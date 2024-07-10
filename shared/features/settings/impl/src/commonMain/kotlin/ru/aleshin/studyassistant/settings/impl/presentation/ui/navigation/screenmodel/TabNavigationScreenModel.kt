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

package ru.aleshin.studyassistant.settings.impl.presentation.ui.navigation.screenmodel

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import org.kodein.di.compose.localDI
import org.kodein.di.direct
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.BaseScreenModel
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.EmptyDeps
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.settings.api.navigation.SettingsScreen
import ru.aleshin.studyassistant.settings.impl.di.holder.SettingsFeatureDIHolder
import ru.aleshin.studyassistant.settings.impl.navigation.SettingsScreenProvider
import ru.aleshin.studyassistant.settings.impl.presentation.ui.navigation.contract.TabNavigationAction
import ru.aleshin.studyassistant.settings.impl.presentation.ui.navigation.contract.TabNavigationEffect
import ru.aleshin.studyassistant.settings.impl.presentation.ui.navigation.contract.TabNavigationEvent
import ru.aleshin.studyassistant.settings.impl.presentation.ui.navigation.contract.TabNavigationViewState

/**
 * @author Stanislav Aleshin on 08.07.2024
 */
internal class TabNavigationScreenModel(
    private val screenProvider: SettingsScreenProvider,
    stateCommunicator: TabNavigationStateCommunicator,
    effectCommunicator: TabNavigationEffectCommunicator,
    coroutineManager: CoroutineManager,
) : BaseScreenModel<TabNavigationViewState, TabNavigationEvent, TabNavigationAction, TabNavigationEffect, EmptyDeps>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {
    override suspend fun WorkScope<TabNavigationViewState, TabNavigationAction, TabNavigationEffect>.handleEvent(
        event: TabNavigationEvent,
    ) = when (event) {
        is TabNavigationEvent.NavigateToGeneral -> {
            val screen = screenProvider.provideFeatureScreen(SettingsScreen.General)
            sendEffect(TabNavigationEffect.ReplaceScreen(screen))
        }
        is TabNavigationEvent.NavigateToNotification -> {
            val screen = screenProvider.provideFeatureScreen(SettingsScreen.Notification)
            sendEffect(TabNavigationEffect.ReplaceScreen(screen))
        }
        is TabNavigationEvent.NavigateToCalendar -> {
            val screen = screenProvider.provideFeatureScreen(SettingsScreen.Calendar)
            sendEffect(TabNavigationEffect.ReplaceScreen(screen))
        }
        is TabNavigationEvent.NavigateToSubscription -> {
            val screen = screenProvider.provideFeatureScreen(SettingsScreen.Subscription)
            sendEffect(TabNavigationEffect.ReplaceScreen(screen))
        }
        is TabNavigationEvent.NavigateToBack -> {
            sendEffect(TabNavigationEffect.NavigateToBack)
        }
    }

    override suspend fun reduce(
        action: TabNavigationAction,
        currentState: TabNavigationViewState,
    ) = TabNavigationViewState

    override fun onDispose() {
        super.onDispose()
        SettingsFeatureDIHolder.clear()
    }
}

@Composable
internal fun Screen.rememberTabNavigationScreenModel(): TabNavigationScreenModel {
    val di = localDI().direct
    return rememberScreenModel { di.instance<TabNavigationScreenModel>() }
}