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
package ru.aleshin.studyassistant.presentation.ui.tabnavigation

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.experimental.stack.ChildStack
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.router.stack.ChildStack
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.ui.uikit.navigation.BottomBarIcon
import ru.aleshin.studyassistant.core.ui.uikit.navigation.BottomBarLabel
import ru.aleshin.studyassistant.core.ui.uikit.navigation.BottomNavBarItem
import ru.aleshin.studyassistant.core.ui.utils.useNavigationRail
import ru.aleshin.studyassistant.presentation.ui.tabnavigation.component.TabsComponent
import ru.aleshin.studyassistant.presentation.ui.tabnavigation.component.TabsComponent.TabsChild
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.chat_bottom_item as core_chat_bottom_item
import ru.aleshin.studyassistant.core.ui.resources.ic_chat_helper as core_ic_chat_helper
import ru.aleshin.studyassistant.core.ui.resources.ic_organization_geo as core_ic_organization_geo
import ru.aleshin.studyassistant.core.ui.resources.ic_profile as core_ic_profile
import ru.aleshin.studyassistant.core.ui.resources.ic_study as core_ic_study
import ru.aleshin.studyassistant.core.ui.resources.ic_tasks as core_ic_tasks
import ru.aleshin.studyassistant.core.ui.resources.info_bottom_item as core_info_bottom_item
import ru.aleshin.studyassistant.core.ui.resources.profile_bottom_item as core_profile_bottom_item
import ru.aleshin.studyassistant.core.ui.resources.schedule_bottom_item as core_schedule_bottom_item
import ru.aleshin.studyassistant.core.ui.resources.tasks_bottom_item as core_tasks_bottom_item

/**
 * @author Stanislav Aleshin on 18.02.2023.
 */
@Composable
@OptIn(ExperimentalDecomposeApi::class)
fun TabsContent(
    tabsComponent: TabsComponent,
    modifier: Modifier = Modifier,
) {
    val stack by tabsComponent.stack.subscribeAsState()
    val windowAdaptiveInfo = currentWindowAdaptiveInfoV2()
    val useNavigationRail = windowAdaptiveInfo.useNavigationRail
    val selectedItem = remember(stack.active) {
        stack.active.instance.mapToBottomItem()
    }

    val onItemSelected: (TabsBottomNavBarItems) -> Unit = { destination ->
        when (destination) {
            TabsBottomNavBarItems.SCHEDULE -> tabsComponent.clickScheduleTab()
            TabsBottomNavBarItems.TASKS -> tabsComponent.clickTasksTab()
            TabsBottomNavBarItems.CHAT -> tabsComponent.clickChatTab()
            TabsBottomNavBarItems.INFO -> tabsComponent.clickInfoTab()
            TabsBottomNavBarItems.PROFILE -> tabsComponent.clickProfileTab()
        }
    }

    if (useNavigationRail) {
        Row(modifier = modifier.fillMaxSize()) {
            NavigationRail(containerColor = MaterialTheme.colorScheme.background) {
                TabsBottomNavBarItems.entries.forEach { destination ->
                    val selected = selectedItem == destination
                    NavigationRailItem(
                        selected = selected,
                        onClick = { onItemSelected(destination) },
                        icon = {
                            BottomBarIcon(
                                selected = selected,
                                enabledIcon = painterResource(destination.icon),
                                disabledIcon = painterResource(destination.icon),
                                description = destination.label,
                            )
                        },
                        label = {
                            BottomBarLabel(
                                selected = selected,
                                title = destination.label,
                            )
                        },
                        colors = NavigationRailItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        ),
                    )
                }
            }
            TabsChildStack(
                modifier = Modifier.weight(1f),
                stack = stack,
            )
        }
    } else {
        Scaffold(
            modifier = modifier,
            contentWindowInsets = WindowInsets(0.dp),
            bottomBar = {
                NavigationBar(
                    modifier = Modifier.heightIn(max = 80.dp),
                    containerColor = selectedItem.containerColor
                ) {
                    TabsBottomNavBarItems.entries.forEach { destination ->
                        val selected = selectedItem == destination
                        NavigationBarItem(
                            modifier = Modifier.height(bottomBarHeight),
                            selected = selected,
                            onClick = { onItemSelected(destination) },
                            icon = {
                                BottomBarIcon(
                                    selected = selected,
                                    enabledIcon = painterResource(destination.icon),
                                    disabledIcon = painterResource(destination.icon),
                                    description = destination.label,
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            ),
                        )
                    }
                }
            },
        ) { contentPadding ->
            TabsChildStack(
                modifier = Modifier.padding(contentPadding),
                stack = stack,
            )
        }
    }
}

@Composable
@OptIn(ExperimentalDecomposeApi::class)
private fun TabsChildStack(
    modifier: Modifier,
    stack: ChildStack<*, TabsChild>,
) {
    ChildStack(
        modifier = modifier.fillMaxSize(),
        stack = stack
    ) { child ->
        when (val instance = child.instance) {
            is TabsChild.ScheduleChild -> {
                instance.contentProvider.Content(Modifier)
            }

            is TabsChild.InfoChild -> {
                instance.contentProvider.Content(Modifier)
            }

            is TabsChild.ProfileChild -> {
                instance.contentProvider.Content(Modifier)
            }

            is TabsChild.TasksChild -> {
                instance.contentProvider.Content(Modifier)
            }

            is TabsChild.ChatChild -> {
                instance.contentProvider.Content(Modifier)
            }
        }
    }
}

internal enum class TabsBottomNavBarItems : BottomNavBarItem {
    SCHEDULE {
        override val label: String @Composable get() = stringResource(CoreRes.string.core_schedule_bottom_item)
        override val icon: DrawableResource @Composable get() = CoreRes.drawable.core_ic_study
        override val containerColor: Color @Composable get() = MaterialTheme.colorScheme.surfaceContainerLow
    },
    TASKS {
        override val label: String @Composable get() = stringResource(CoreRes.string.core_tasks_bottom_item)
        override val icon: DrawableResource @Composable get() = CoreRes.drawable.core_ic_tasks
    },
    CHAT {
        override val label: String @Composable get() = stringResource(CoreRes.string.core_chat_bottom_item)
        override val icon: DrawableResource @Composable get() = CoreRes.drawable.core_ic_chat_helper
        override val containerColor: Color @Composable get() = MaterialTheme.colorScheme.surfaceContainerLow
    },
    INFO {
        override val label: String @Composable get() = stringResource(CoreRes.string.core_info_bottom_item)
        override val icon: DrawableResource @Composable get() = CoreRes.drawable.core_ic_organization_geo
        override val containerColor: Color @Composable get() = MaterialTheme.colorScheme.surfaceContainerLow
    },
    PROFILE {
        override val label: String @Composable get() = stringResource(CoreRes.string.core_profile_bottom_item)
        override val icon: DrawableResource @Composable get() = CoreRes.drawable.core_ic_profile
    }
}

internal fun TabsChild.mapToBottomItem() = when (this) {
    is TabsChild.ScheduleChild -> TabsBottomNavBarItems.SCHEDULE
    is TabsChild.TasksChild -> TabsBottomNavBarItems.TASKS
    is TabsChild.ChatChild -> TabsBottomNavBarItems.CHAT
    is TabsChild.InfoChild -> TabsBottomNavBarItems.INFO
    is TabsChild.ProfileChild -> TabsBottomNavBarItems.PROFILE
}

private val bottomBarHeight = 64.dp