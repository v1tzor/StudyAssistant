/*
 * Copyright 2023 Stanislav Aleshin
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
package ru.aleshin.studyassistant.presentation.ui.tabs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import architecture.screen.ScreenContent
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.FadeTransition
import managers.rememberDrawerManager
import org.jetbrains.compose.resources.ExperimentalResourceApi
import ru.aleshin.core.common.navigation.screens.EmptyScreen
import ru.aleshin.studyassistant.presentation.ui.tabs.contract.TabsEffect
import ru.aleshin.studyassistant.presentation.ui.tabs.contract.TabsEvent
import ru.aleshin.studyassistant.presentation.ui.tabs.contract.TabsViewState
import ru.aleshin.studyassistant.presentation.ui.tabs.screenmodel.rememberTabsScreenModel
import ru.aleshin.studyassistant.presentation.ui.tabs.views.HomeDrawerItems
import ru.aleshin.studyassistant.presentation.ui.tabs.views.HomeNavigationDrawer
import ru.aleshin.studyassistant.presentation.ui.tabs.views.TabsBottomBarItems
import ru.aleshin.studyassistant.presentation.ui.tabs.views.TabsBottomNavigationBar

/**
 * @author Stanislav Aleshin on 18.02.2023.
 */
class TabsScreen : Screen {

    @Composable
    @OptIn(ExperimentalResourceApi::class)
    override fun Content() = ScreenContent(
        screenModel = rememberTabsScreenModel(),
        initialState = TabsViewState(),
    ) {
        val state = fetchState()
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val drawerManager = rememberDrawerManager(drawerState)

        Navigator(EmptyScreen) {
            HomeNavigationDrawer(
                drawerState = drawerState,
                drawerManager = drawerManager,
                isAlwaysSelected = state.bottomBarItem != TabsBottomBarItems.SCHEDULE,
                onItemSelected = { item ->
                    val event = when (item) {
                        HomeDrawerItems.MAIN -> TabsEvent.SelectedScheduleBottomItem
                    }
                    dispatchEvent(event)
                },
            ) {
                Scaffold(
                    content = { paddingValues ->
                        Box(modifier = Modifier.padding(paddingValues)) {
                            FadeTransition(navigator = it)
                        }
                    },
                    bottomBar = {
                        TabsBottomNavigationBar(
                            selectedItem = state.bottomBarItem,
                            onItemSelected = { tab ->
                                val event = when (tab) {
                                    TabsBottomBarItems.SCHEDULE -> TabsEvent.SelectedScheduleBottomItem
                                    TabsBottomBarItems.TASKS -> TabsEvent.SelectedTasksBottomItem
                                    TabsBottomBarItems.INFO -> TabsEvent.SelectedInfoBottomItem
                                    TabsBottomBarItems.PROFILE -> TabsEvent.SelectedProfileBottomItem
                                }
                                dispatchEvent(event)
                            },
                        )
                    },
                )
            }

            handleEffect { effect ->
                when(effect) {
                    is TabsEffect.ReplaceScreen -> it.replaceAll(effect.screen)
                }
            }
        }
    }
}
