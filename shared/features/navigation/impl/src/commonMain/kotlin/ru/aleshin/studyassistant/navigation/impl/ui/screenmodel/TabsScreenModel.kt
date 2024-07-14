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
import co.touchlab.kermit.Logger
import org.kodein.di.instance
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
import ru.aleshin.studyassistant.navigation.impl.ui.views.TabsBottomBarItems
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
        TabsEvent.Init -> changeTabItem(TabsBottomBarItems.SCHEDULE) {
            provideScheduleScreen(ScheduleScreen.Overview(null))
        }
        TabsEvent.SelectedScheduleBottomItem -> changeTabItem(TabsBottomBarItems.SCHEDULE) {
            provideScheduleScreen(ScheduleScreen.Overview(null))
        }
        TabsEvent.SelectedTasksBottomItem -> changeTabItem(TabsBottomBarItems.TASKS) {
            provideTasksScreen(TasksScreen.Overview)
        }
        TabsEvent.SelectedInfoBottomItem -> changeTabItem(TabsBottomBarItems.INFO) {
            provideInfoScreen(InfoScreen.Organizations)
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

    override fun onDispose() {
        super.onDispose()
        Logger.i("test") { "onDispose -> tabs" }
        NavigationFeatureDIHolder.clear()
    }

    private suspend fun WorkScope<TabsViewState, TabsAction, TabsEffect>.changeTabItem(
        bottomItem: TabsBottomBarItems,
        onAction: TabScreenProvider.() -> Screen,
    ) = sendAction(TabsAction.ChangeNavItems(item = bottomItem)).apply {
        sendEffect(TabsEffect.ReplaceScreen(screen = tabScreenProvider.let(onAction)))
    }
}

@Composable
internal fun Screen.rememberTabsScreenModel(): TabsScreenModel {
    return rememberScreenModel { NavigationFeatureDIHolder.fetchDI().instance<TabsScreenModel>() }
}