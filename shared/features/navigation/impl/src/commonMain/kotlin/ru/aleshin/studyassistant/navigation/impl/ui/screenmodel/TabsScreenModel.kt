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
package ru.aleshin.studyassistant.navigation.impl.ui.screenmodel

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import org.kodein.di.instance
import ru.aleshin.studyassistant.chat.api.navigation.ChatScreen
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.BaseScreenModel
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.EmptyDeps
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.info.api.navigation.InfoScreen
import ru.aleshin.studyassistant.navigation.impl.di.holder.NavigationFeatureDIHolder
import ru.aleshin.studyassistant.navigation.impl.navigation.TabScreenProvider
import ru.aleshin.studyassistant.navigation.impl.ui.contract.TabsAction
import ru.aleshin.studyassistant.navigation.impl.ui.contract.TabsEffect
import ru.aleshin.studyassistant.navigation.impl.ui.contract.TabsEvent
import ru.aleshin.studyassistant.navigation.impl.ui.contract.TabsViewState
import ru.aleshin.studyassistant.schedule.api.navigation.ScheduleScreen
import ru.aleshin.studyassistant.tasks.api.navigation.TasksScreen

/**
 * @author Stanislav Aleshin on 18.02.2023.
 */
internal class TabsScreenModel(
    private val tabScreenProvider: TabScreenProvider,
    stateCommunicator: TabsStateCommunicator,
    effectCommunicator: TabsEffectCommunicator,
    coroutineManager: CoroutineManager,
) : BaseScreenModel<TabsViewState, TabsEvent, TabsAction, TabsEffect, EmptyDeps>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun init(deps: EmptyDeps) {
        if (!isInitialize) {
            super.init(deps)
            dispatchEvent(TabsEvent.Init)
        }
    }

    override suspend fun WorkScope<TabsViewState, TabsAction, TabsEffect>.handleEvent(
        event: TabsEvent,
    ) = when (event) {
        is TabsEvent.Init -> changeTabItem {
            provideScheduleScreen(ScheduleScreen.Overview)
        }
        is TabsEvent.SelectedScheduleBottomItem -> changeTabItem {
            provideScheduleScreen(ScheduleScreen.Overview)
        }
        is TabsEvent.SelectedChatBottomItem -> changeTabItem {
            provideChatScreen(ChatScreen.Assistant)
        }
        is TabsEvent.SelectedTasksBottomItem -> changeTabItem {
            provideTasksScreen(TasksScreen.Overview)
        }
        is TabsEvent.SelectedInfoBottomItem -> changeTabItem {
            provideInfoScreen(InfoScreen.Organizations)
        }
        is TabsEvent.SelectedProfileBottomItem -> changeTabItem {
            provideProfileScreen()
        }
    }

    override suspend fun reduce(
        action: TabsAction,
        currentState: TabsViewState,
    ) = when (action) {
        is TabsAction.ChangeScreenKeys -> currentState.copy(
            screenKeys = action.keys,
        )
    }

    override fun onDispose() {
        super.onDispose()
        NavigationFeatureDIHolder.clear()
    }

    private suspend fun WorkScope<TabsViewState, TabsAction, TabsEffect>.changeTabItem(
        onAction: TabScreenProvider.() -> Screen,
    ) = with(state()) {
        val screen = tabScreenProvider.let(onAction)
        val updatedKeys = screenKeys.toMutableSet().apply { add(screen.key) }
        sendAction(TabsAction.ChangeScreenKeys(keys = updatedKeys))
        sendEffect(TabsEffect.ReplaceScreen(screen = screen))
    }
}

@Composable
internal fun Screen.rememberTabsScreenModel(): TabsScreenModel {
    return rememberScreenModel { NavigationFeatureDIHolder.fetchDI().instance<TabsScreenModel>() }
}