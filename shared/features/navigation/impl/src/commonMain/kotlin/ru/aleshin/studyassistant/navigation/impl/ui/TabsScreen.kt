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
package ru.aleshin.studyassistant.navigation.impl.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import architecture.screen.ScreenContent
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.NavigatorDisposeBehavior
import cafe.adriel.voyager.transitions.FadeTransition
import ru.aleshin.core.common.navigation.screens.EmptyScreen
import ru.aleshin.studyassistant.navigation.impl.ui.contract.TabsEffect
import ru.aleshin.studyassistant.navigation.impl.ui.contract.TabsEvent
import ru.aleshin.studyassistant.navigation.impl.ui.contract.TabsViewState
import ru.aleshin.studyassistant.navigation.impl.ui.screenmodel.rememberTabsScreenModel
import ru.aleshin.studyassistant.navigation.impl.ui.views.TabsBottomBarItems
import ru.aleshin.studyassistant.navigation.impl.ui.views.TabsBottomNavigationBar

/**
 * @author Stanislav Aleshin on 18.02.2023.
 */
internal class TabsScreen : Screen {

    @Composable
    override fun Content() = ScreenContent(
        screenModel = rememberTabsScreenModel(),
        initialState = TabsViewState(),
    ) { state ->
        Navigator(
            screen = EmptyScreen,
            disposeBehavior = NavigatorDisposeBehavior(false, false),
        ) { navigator ->
            Scaffold(
                content = { paddingValues ->
                    Box(modifier = Modifier.padding(paddingValues)) {
                        FadeTransition(navigator = navigator)
                    }
                },
                bottomBar = {
                    TabsBottomNavigationBar(
                        modifier = Modifier.systemBarsPadding(),
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

            handleEffect { effect ->
                when (effect) {
                    is TabsEffect.ReplaceScreen -> navigator.replaceAll(effect.screen)
                }
            }
        }
    }
}
