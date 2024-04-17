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
package ru.aleshin.studyassistant.presentation.ui.tabs.views

import androidx.compose.foundation.layout.width
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import managers.DrawerItem
import managers.DrawerManager
import managers.LocalDrawerManager
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import theme.StudyAssistantRes
import views.DrawerImageSection
import views.DrawerItems

/**
 * @author Stanislav Aleshin on 08.02.2024.
 */
@Composable
@OptIn(ExperimentalResourceApi::class)
fun HomeNavigationDrawer(
    drawerState: DrawerState,
    drawerManager: DrawerManager,
    isAlwaysSelected: Boolean,
    onItemSelected: (HomeDrawerItems) -> Unit,
    content: @Composable () -> Unit,
) {
    val selectedItem by drawerManager.selectedItem.collectAsState(0)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet {
                DrawerImageSection(
                    image = painterResource(StudyAssistantRes.icons.logo),
                    description = StudyAssistantRes.strings.appName,
                )
                DrawerItems(
                    modifier = Modifier.width(300.dp),
                    selectedItemIndex = selectedItem,
                    items = HomeDrawerItems.entries.toTypedArray(),
                    isAlwaysSelected = isAlwaysSelected,
                    onItemSelected = { item ->
                        onItemSelected(item)
                        scope.launch { drawerState.close() }
                    },
                )
            }
        },
    ) {
        CompositionLocalProvider(
            LocalDrawerManager provides drawerManager,
            content = content,
        )
    }
}

@ExperimentalResourceApi
enum class HomeDrawerItems : DrawerItem {
    MAIN {
        override val icon: DrawableResource @Composable get() = StudyAssistantRes.icons.scheduleDisabled
        override val title: String @Composable get() = StudyAssistantRes.strings.scheduleBottomItem
    },
}
