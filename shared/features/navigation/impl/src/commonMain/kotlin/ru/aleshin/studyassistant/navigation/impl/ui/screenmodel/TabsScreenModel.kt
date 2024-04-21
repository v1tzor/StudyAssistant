
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
import architecture.screenmodel.BaseScreenModel
import architecture.screenmodel.EmptyDeps
import architecture.screenmodel.work.WorkScope
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.kodein.rememberScreenModel
import managers.CoroutineManager
import org.kodein.di.instance
import ru.aleshin.studyassistant.navigation.impl.di.holder.NavigationFeatureDIHolder
import ru.aleshin.studyassistant.navigation.impl.navigation.ScreenProvider
import ru.aleshin.studyassistant.navigation.impl.ui.contract.TabsAction
import ru.aleshin.studyassistant.navigation.impl.ui.contract.TabsEffect
import ru.aleshin.studyassistant.navigation.impl.ui.contract.TabsEvent
import ru.aleshin.studyassistant.navigation.impl.ui.contract.TabsViewState
import ru.aleshin.studyassistant.navigation.impl.ui.views.TabsBottomBarItems

/**
 * @author Stanislav Aleshin on 18.02.2023.
 */
internal class TabsScreenModel(
    private val screenProvider: ScreenProvider,
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
        TabsEvent.Init -> changeTabItem(TabsBottomBarItems.SCHEDULE) {
            provideScheduleScreen()
        }
        TabsEvent.SelectedScheduleBottomItem -> changeTabItem(TabsBottomBarItems.SCHEDULE) {
            provideScheduleScreen()
        }
        TabsEvent.SelectedTasksBottomItem -> changeTabItem(TabsBottomBarItems.TASKS) {
            provideTasksScreen()
        }
        TabsEvent.SelectedInfoBottomItem -> changeTabItem(TabsBottomBarItems.INFO) {
            provideInfoScreen()
        }
        TabsEvent.SelectedProfileBottomItem -> changeTabItem(TabsBottomBarItems.PROFILE) {
            provideProfileScreen()
        }
    }

    override suspend fun reduce(
        action: TabsAction,
        currentState: TabsViewState,
    ) = when (action) {
        is TabsAction.ChangeNavItems -> currentState.copy(
            bottomBarItem = action.item,
        )
    }

    private suspend fun WorkScope<TabsViewState, TabsAction, TabsEffect>.changeTabItem(
        bottomItem: TabsBottomBarItems,
        onAction: ScreenProvider.() -> Screen,
    ) = sendAction(TabsAction.ChangeNavItems(item = bottomItem)).apply {
        sendEffect(TabsEffect.ReplaceScreen(screen = screenProvider.let(onAction)))
    }
}

@Composable
internal fun Screen.rememberTabsScreenModel(): TabsScreenModel {
    val di = NavigationFeatureDIHolder.fetchDI()
    return rememberScreenModel { di.instance<TabsScreenModel>() }
}
