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

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.experimental.stack.ChildStack
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import ru.aleshin.studyassistant.presentation.ui.tabnavigation.component.TabsComponent
import ru.aleshin.studyassistant.presentation.ui.tabnavigation.component.TabsComponent.TabsChild
import ru.aleshin.studyassistant.presentation.ui.tabnavigation.views.TabsBottomBarItems
import ru.aleshin.studyassistant.presentation.ui.tabnavigation.views.TabsBottomNavigationBar
import ru.aleshin.studyassistant.presentation.ui.tabnavigation.views.mapToItem

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

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0.dp),
        bottomBar = {
            TabsBottomNavigationBar(
                selectedItem = remember(stack.active) {
                    stack.active.instance.mapToItem()
                },
                onItemSelected = {
                    when (it) {
                        TabsBottomBarItems.SCHEDULE -> tabsComponent.clickScheduleTab()
                        TabsBottomBarItems.TASKS -> tabsComponent.clickTasksTab()
                        TabsBottomBarItems.CHAT -> tabsComponent.clickChatTab()
                        TabsBottomBarItems.INFO -> tabsComponent.clickInfoTab()
                        TabsBottomBarItems.PROFILE -> tabsComponent.clickProfileTab()
                    }
                }
            )
        },
    ) { paddingValues ->
        ChildStack(
            modifier = Modifier.padding(paddingValues),
            stack = stack
        ) { child ->
            when (val instance = child.instance) {
                is TabsChild.ScheduleChild -> {
                    instance.component.contentProvider.invoke(Modifier)
                }
                is TabsChild.InfoChild -> {
                    instance.component.contentProvider.invoke(Modifier)
                }
                is TabsChild.ProfileChild -> {
                    instance.component.contentProvider.invoke(Modifier)
                }
                is TabsChild.TasksChild -> {
                    instance.component.contentProvider.invoke(Modifier)
                }
                is TabsChild.ChatChild -> {
                    instance.component.contentProvider.invoke(Modifier)
                }
            }
        }
    }
}