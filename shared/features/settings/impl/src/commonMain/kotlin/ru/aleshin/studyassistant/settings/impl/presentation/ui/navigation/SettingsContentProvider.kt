/*
 * Copyright 2025 Stanislav Aleshin
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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.experimental.stack.ChildStack
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import ru.aleshin.studyassistant.core.common.di.withDirectDI
import ru.aleshin.studyassistant.core.common.inject.FeatureContentProvider
import ru.aleshin.studyassistant.core.ui.views.TabItem
import ru.aleshin.studyassistant.settings.impl.di.holder.SettingsFeatureManager
import ru.aleshin.studyassistant.settings.impl.presentation.theme.SettingsTheme
import ru.aleshin.studyassistant.settings.impl.presentation.theme.SettingsThemeRes
import ru.aleshin.studyassistant.settings.impl.presentation.ui.calendar.CalendarContent
import ru.aleshin.studyassistant.settings.impl.presentation.ui.general.GeneralContent
import ru.aleshin.studyassistant.settings.impl.presentation.ui.info.AboutAppContent
import ru.aleshin.studyassistant.settings.impl.presentation.ui.navigation.contract.TabNavigationEvent
import ru.aleshin.studyassistant.settings.impl.presentation.ui.navigation.store.InternalSettingsFeatureComponent
import ru.aleshin.studyassistant.settings.impl.presentation.ui.navigation.store.InternalSettingsFeatureComponent.Child
import ru.aleshin.studyassistant.settings.impl.presentation.ui.navigation.views.TabNavigationRow
import ru.aleshin.studyassistant.settings.impl.presentation.ui.navigation.views.TabNavigationTopBar
import ru.aleshin.studyassistant.settings.impl.presentation.ui.notification.NotificationContent
import ru.aleshin.studyassistant.settings.impl.presentation.ui.subscription.SubscriptionContent

/**
 * @author Stanislav Aleshin on 26.08.2025.
 */
public class SettingsContentProvider internal constructor(
    private val component: InternalSettingsFeatureComponent,
) : FeatureContentProvider {

    @Composable
    @OptIn(ExperimentalDecomposeApi::class)
    override fun invoke(modifier: Modifier) {
        withDirectDI(directDI = { SettingsFeatureManager.fetchDI() }) {
            SettingsTheme {
                val store = component.store
                val stack by component.stack.subscribeAsState()

                Scaffold(
                    modifier = modifier.fillMaxSize(),
                    content = { paddingValues ->
                        ChildStack(
                            modifier = Modifier.padding(paddingValues),
                            stack = stack,
                            animation = stackAnimation(),
                        ) { child ->
                            when (val instance = child.instance) {
                                is Child.GeneralChild -> GeneralContent(instance.component)
                                is Child.AboutAppChild -> AboutAppContent(instance.component)
                                is Child.CalendarChild -> CalendarContent(instance.component)
                                is Child.NotificationChild -> NotificationContent(instance.component)
                                is Child.SubscriptionChild -> SubscriptionContent(instance.component)
                            }
                        }
                    },
                    topBar = {
                        Column {
                            TabNavigationTopBar(
                                onBackClick = { store.dispatchEvent(TabNavigationEvent.NavigateToBack) },
                            )
                            TabNavigationRow(
                                selectedItem = remember(stack.active) {
                                    when (stack.active.instance) {
                                        is Child.GeneralChild -> SettingsTabItem.GENERAL
                                        is Child.CalendarChild -> SettingsTabItem.CALENDAR
                                        is Child.NotificationChild -> SettingsTabItem.NOTIFICATION
                                        is Child.SubscriptionChild -> SettingsTabItem.SUBSCRIPTION
                                        is Child.AboutAppChild -> SettingsTabItem.ABOUT_APP
                                    }
                                },
                                onSelect = { tabItem ->
                                    when (tabItem) {
                                        SettingsTabItem.GENERAL -> {
                                            store.dispatchEvent(TabNavigationEvent.NavigateToGeneral)
                                        }
                                        SettingsTabItem.NOTIFICATION -> {
                                            store.dispatchEvent(TabNavigationEvent.NavigateToNotification)
                                        }
                                        SettingsTabItem.CALENDAR -> {
                                            store.dispatchEvent(TabNavigationEvent.NavigateToCalendar)
                                        }
                                        SettingsTabItem.SUBSCRIPTION -> {
                                            store.dispatchEvent(TabNavigationEvent.NavigateToSubscription)
                                        }
                                        SettingsTabItem.ABOUT_APP -> {
                                            store.dispatchEvent(TabNavigationEvent.NavigateToAboutApp)
                                        }
                                    }
                                },
                            )
                        }
                    },
                    contentWindowInsets = WindowInsets(0.dp),
                )
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
    ABOUT_APP {
        override val index = 4
        override val title @Composable get() = SettingsThemeRes.strings.aboutAppTabHeader
        override val icon @Composable get() = null
    },
}