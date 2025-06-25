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

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import cafe.adriel.voyager.core.lifecycle.DisposableEffectIgnoringConfiguration
import cafe.adriel.voyager.core.stack.StackEvent
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.NavigatorDisposeBehavior
import ru.aleshin.studyassistant.chat.api.presentation.ChatRootScreen
import ru.aleshin.studyassistant.core.common.architecture.screen.EmptyScreen
import ru.aleshin.studyassistant.core.common.architecture.screen.ScreenContent
import ru.aleshin.studyassistant.core.common.navigation.disposeByKeys
import ru.aleshin.studyassistant.info.api.presentation.InfoRootScreen
import ru.aleshin.studyassistant.navigation.api.presentation.TabsRootScreen
import ru.aleshin.studyassistant.navigation.impl.ui.contract.TabsEffect
import ru.aleshin.studyassistant.navigation.impl.ui.contract.TabsEvent
import ru.aleshin.studyassistant.navigation.impl.ui.contract.TabsViewState
import ru.aleshin.studyassistant.navigation.impl.ui.screenmodel.rememberTabsScreenModel
import ru.aleshin.studyassistant.navigation.impl.ui.views.TabsBottomBarItems
import ru.aleshin.studyassistant.navigation.impl.ui.views.TabsNavigationSuiteItems
import ru.aleshin.studyassistant.profile.api.presentation.ProfileRootScreen
import ru.aleshin.studyassistant.schedule.api.presentation.ScheduleRootScreen
import ru.aleshin.studyassistant.tasks.api.presentation.TasksRootScreen

/**
 * @author Stanislav Aleshin on 18.02.2023.
 */
internal class TabsScreen : TabsRootScreen() {

    @Composable
    override fun Content() = ScreenContent(
        screenModel = rememberTabsScreenModel(),
        initialState = TabsViewState(),
    ) { state ->
        val screenKeys by rememberUpdatedState(state.screenKeys)
        Navigator(
            screen = EmptyScreen,
            disposeBehavior = NavigatorDisposeBehavior(
                disposeNestedNavigators = false,
                disposeSteps = false
            ),
        ) { navigator ->
            NavigationSuiteScaffold(
                navigationSuiteItems = {
                    TabsNavigationSuiteItems(
                        selectedItem = when (navigator.lastItem) {
                            is ScheduleRootScreen -> TabsBottomBarItems.SCHEDULE
                            is TasksRootScreen -> TabsBottomBarItems.TASKS
                            is ChatRootScreen -> TabsBottomBarItems.CHAT
                            is InfoRootScreen -> TabsBottomBarItems.INFO
                            is ProfileRootScreen -> TabsBottomBarItems.PROFILE
                            else -> null
                        },
                        onItemSelected = { tab ->
                            val event = when (tab) {
                                TabsBottomBarItems.SCHEDULE -> TabsEvent.SelectedScheduleBottomItem
                                TabsBottomBarItems.TASKS -> TabsEvent.SelectedTasksBottomItem
                                TabsBottomBarItems.CHAT -> TabsEvent.SelectedChatBottomItem
                                TabsBottomBarItems.INFO -> TabsEvent.SelectedInfoBottomItem
                                TabsBottomBarItems.PROFILE -> TabsEvent.SelectedProfileBottomItem
                            }
                            dispatchEvent(event)
                        },
                    )
                },
                navigationSuiteColors = NavigationSuiteDefaults.colors(
                    navigationBarContainerColor = when (navigator.lastItem) {
                        is ScheduleRootScreen -> TabsBottomBarItems.SCHEDULE.containerColor
                        is TasksRootScreen -> TabsBottomBarItems.TASKS.containerColor
                        is ChatRootScreen -> TabsBottomBarItems.CHAT.containerColor
                        is InfoRootScreen -> TabsBottomBarItems.INFO.containerColor
                        is ProfileRootScreen -> TabsBottomBarItems.PROFILE.containerColor
                        else -> MaterialTheme.colorScheme.background
                    },
                )
            ) {
                CurrentScreen()
            }
            DisposableEffectIgnoringConfiguration {
                onDispose {
                    if (navigator.parent?.lastEvent != StackEvent.Push) {
                        navigator.disposeByKeys(screenKeys)
                    }
                }
            }

            handleEffect { effect ->
                when (effect) {
                    is TabsEffect.ReplaceScreen -> navigator.replaceAll(effect.screen)
                }
            }
        }
    }
}