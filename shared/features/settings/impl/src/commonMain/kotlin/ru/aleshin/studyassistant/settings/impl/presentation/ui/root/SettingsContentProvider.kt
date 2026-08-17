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

package ru.aleshin.studyassistant.settings.impl.presentation.ui.root

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.experimental.stack.ChildStack
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import org.jetbrains.compose.resources.stringResource
import org.kodein.di.DI
import ru.aleshin.studyassistant.core.common.inject.FeatureContentProvider
import ru.aleshin.studyassistant.core.ui.views.TabItem
import ru.aleshin.studyassistant.settings.impl.presentation.ui.ai.AiSettingsContent
import ru.aleshin.studyassistant.settings.impl.presentation.ui.calendar.CalendarContent
import ru.aleshin.studyassistant.settings.impl.presentation.ui.fetchSettingsLayoutMode
import ru.aleshin.studyassistant.settings.impl.presentation.ui.general.GeneralContent
import ru.aleshin.studyassistant.settings.impl.presentation.ui.info.AboutAppContent
import ru.aleshin.studyassistant.settings.impl.presentation.ui.navigation.contract.TabNavigationEvent
import ru.aleshin.studyassistant.settings.impl.presentation.ui.notification.NotificationContent
import ru.aleshin.studyassistant.settings.impl.presentation.ui.root.SettingsFeatureComponent.Child
import ru.aleshin.studyassistant.settings.impl.resources.Res
import ru.aleshin.studyassistant.settings.impl.resources.about_app_tab_header
import ru.aleshin.studyassistant.settings.impl.resources.ai_tab_header
import ru.aleshin.studyassistant.settings.impl.resources.calendar_tab_header
import ru.aleshin.studyassistant.settings.impl.resources.general_tab_header
import ru.aleshin.studyassistant.settings.impl.resources.notifications_tab_header

/**
 * @author Stanislav Aleshin on 26.08.2025.
 */
internal class SettingsContentProvider(
    di: DI,
    private val component: SettingsFeatureComponent,
) : FeatureContentProvider(di) {

    @Composable
    @OptIn(ExperimentalDecomposeApi::class)
    override fun RootContent(modifier: Modifier) {
        val store = component.store
        val stack by component.stack.subscribeAsState()
        val layoutMode = currentWindowAdaptiveInfoV2().fetchSettingsLayoutMode()
        val selectedItem = remember(stack.active) {
            when (stack.active.instance) {
                is Child.GeneralChild -> SettingsTabItem.GENERAL
                is Child.CalendarChild -> SettingsTabItem.CALENDAR
                is Child.NotificationChild -> SettingsTabItem.NOTIFICATION
                is Child.AiSettingsChild -> SettingsTabItem.AI
                is Child.AboutAppChild -> SettingsTabItem.ABOUT_APP
            }
        }

        SettingsLayout(
            modifier = modifier,
            layoutMode = layoutMode,
            selectedItem = selectedItem,
            onBackClick = { store.dispatchEvent(TabNavigationEvent.NavigateToBack) },
            onSelectTab = { tabItem ->
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
                    SettingsTabItem.AI -> {
                        store.dispatchEvent(TabNavigationEvent.NavigateToAi)
                    }
                    SettingsTabItem.ABOUT_APP -> {
                        store.dispatchEvent(TabNavigationEvent.NavigateToAboutApp)
                    }
                }
            },
        ) { paddingValues ->
            ChildStack(
                modifier = Modifier.padding(paddingValues),
                stack = stack,
                animation = stackAnimation(),
            ) { child ->
                when (val instance = child.instance) {
                    is Child.GeneralChild -> GeneralContent(instance.component)
                    is Child.AboutAppChild -> AboutAppContent(instance.component)
                    is Child.CalendarChild -> CalendarContent(instance.component)
                    is Child.AiSettingsChild -> AiSettingsContent(instance.component)
                    is Child.NotificationChild -> NotificationContent(instance.component)
                }
            }
        }
    }
}

internal enum class SettingsTabItem : TabItem {
    GENERAL {
        override val index = 0
        override val title @Composable get() = stringResource(Res.string.general_tab_header)
        override val icon @Composable get() = null
    },
    NOTIFICATION {
        override val index = 1
        override val title @Composable get() = stringResource(Res.string.notifications_tab_header)
        override val icon @Composable get() = null
    },
    CALENDAR {
        override val index = 2
        override val title @Composable get() = stringResource(Res.string.calendar_tab_header)
        override val icon @Composable get() = null
    },
    AI {
        override val index = 3
        override val title @Composable get() = stringResource(Res.string.ai_tab_header)
        override val icon @Composable get() = null
    },
    ABOUT_APP {
        override val index = 4
        override val title @Composable get() = stringResource(Res.string.about_app_tab_header)
        override val icon @Composable get() = null
    },
}
