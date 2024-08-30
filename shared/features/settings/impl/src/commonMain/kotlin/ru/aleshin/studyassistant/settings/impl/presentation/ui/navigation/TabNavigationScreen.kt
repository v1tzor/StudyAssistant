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

package ru.aleshin.studyassistant.settings.impl.presentation.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.NavigatorDisposeBehavior
import ru.aleshin.studyassistant.core.common.architecture.screen.ScreenContent
import ru.aleshin.studyassistant.core.common.di.withDirectDI
import ru.aleshin.studyassistant.core.common.navigation.NestedFeatureNavigator
import ru.aleshin.studyassistant.core.common.navigation.nestedPop
import ru.aleshin.studyassistant.core.common.navigation.rememberNavigatorManager
import ru.aleshin.studyassistant.core.common.navigation.rememberScreenProvider
import ru.aleshin.studyassistant.core.ui.views.TabItem
import ru.aleshin.studyassistant.settings.api.navigation.SettingsScreen
import ru.aleshin.studyassistant.settings.api.presentation.SettingsRootScreen
import ru.aleshin.studyassistant.settings.impl.di.holder.SettingsFeatureDIHolder
import ru.aleshin.studyassistant.settings.impl.navigation.SettingsNavigatorManager
import ru.aleshin.studyassistant.settings.impl.navigation.SettingsScreenProvider
import ru.aleshin.studyassistant.settings.impl.presentation.theme.SettingsTheme
import ru.aleshin.studyassistant.settings.impl.presentation.theme.SettingsThemeRes
import ru.aleshin.studyassistant.settings.impl.presentation.ui.calendar.CalendarScreen
import ru.aleshin.studyassistant.settings.impl.presentation.ui.general.GeneralScreen
import ru.aleshin.studyassistant.settings.impl.presentation.ui.navigation.contract.TabNavigationEffect
import ru.aleshin.studyassistant.settings.impl.presentation.ui.navigation.contract.TabNavigationEvent
import ru.aleshin.studyassistant.settings.impl.presentation.ui.navigation.contract.TabNavigationViewState
import ru.aleshin.studyassistant.settings.impl.presentation.ui.navigation.screenmodel.rememberTabNavigationScreenModel
import ru.aleshin.studyassistant.settings.impl.presentation.ui.navigation.views.TabNavigationRow
import ru.aleshin.studyassistant.settings.impl.presentation.ui.navigation.views.TabNavigationTopBar
import ru.aleshin.studyassistant.settings.impl.presentation.ui.notification.NotificationScreen
import ru.aleshin.studyassistant.settings.impl.presentation.ui.subscription.SubscriptionScreen

/**
 * @author Stanislav Aleshin on 08.07.2024.
 */
internal class TabNavigationScreen : SettingsRootScreen() {

    @Composable
    override fun Content() = withDirectDI(directDI = { SettingsFeatureDIHolder.fetchDI() }) {
        ScreenContent(
            initialState = TabNavigationViewState,
            screenModel = rememberTabNavigationScreenModel(),
        ) {
            NestedFeatureNavigator(
                screenProvider = rememberScreenProvider<SettingsScreenProvider, SettingsScreen, SettingsRootScreen>(),
                navigatorManager = rememberNavigatorManager<SettingsNavigatorManager, SettingsScreen, SettingsRootScreen>(),
                disposeBehavior = NavigatorDisposeBehavior(
                    disposeNestedNavigators = false,
                    disposeSteps = false,
                )
            ) { navigator ->
                SettingsTheme {
                    val selectedItem = when (navigator.lastItem) {
                        is GeneralScreen -> SettingsTabItem.GENERAL
                        is CalendarScreen -> SettingsTabItem.CALENDAR
                        is NotificationScreen -> SettingsTabItem.NOTIFICATION
                        is SubscriptionScreen -> SettingsTabItem.SUBSCRIPTION
                        else -> SettingsTabItem.GENERAL
                    }

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        content = { paddingValues ->
                            Box(modifier = Modifier.padding(paddingValues)) {
                                CurrentScreen()
                            }
                        },
                        topBar = {
                            Column {
                                TabNavigationTopBar(
                                    onBackClick = { dispatchEvent(TabNavigationEvent.NavigateToBack) },
                                )
                                TabNavigationRow(
                                    selectedItem = selectedItem,
                                    onSelect = { tabItem ->
                                        when (tabItem) {
                                            SettingsTabItem.GENERAL -> {
                                                dispatchEvent(TabNavigationEvent.NavigateToGeneral)
                                            }
                                            SettingsTabItem.NOTIFICATION -> {
                                                dispatchEvent(TabNavigationEvent.NavigateToNotification)
                                            }
                                            SettingsTabItem.CALENDAR -> {
                                                dispatchEvent(TabNavigationEvent.NavigateToCalendar)
                                            }
                                            SettingsTabItem.SUBSCRIPTION -> {
                                                dispatchEvent(TabNavigationEvent.NavigateToSubscription)
                                            }
                                        }
                                    },
                                )
                            }
                        },
                        contentWindowInsets = WindowInsets(0.dp),
                    )

                    handleEffect { effect ->
                        when (effect) {
                            is TabNavigationEffect.ReplaceScreen -> navigator.replaceAll(effect.screen)
                            is TabNavigationEffect.NavigateToBack -> navigator.nestedPop()
                        }
                    }
                }
            }
        }
    }
}

internal enum class SettingsTabItem : TabItem {
    GENERAL {
        override val index = 0
        override val title @Composable get() = SettingsThemeRes.strings.generalTabHeader
        override val icon @Composable get() = null
    },
    NOTIFICATION {
        override val index = 1
        override val title @Composable get() = SettingsThemeRes.strings.notificationsTabHeader
        override val icon @Composable get() = null
    },
    CALENDAR {
        override val index = 2
        override val title @Composable get() = SettingsThemeRes.strings.calendarTabHeader
        override val icon @Composable get() = null
    },
    SUBSCRIPTION {
        override val index = 3
        override val title @Composable get() = SettingsThemeRes.strings.subscriptionTabHeader
        override val icon @Composable get() = null
    },
}