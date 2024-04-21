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
package ru.aleshin.studyassistant.navigation.impl.ui.views

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import theme.StudyAssistantRes
import views.BottomBarItem
import views.BottomNavigationBar

/**
 * @author Stanislav Aleshin on 08.02.2024.
 */
@Composable
internal fun TabsBottomNavigationBar(
    modifier: Modifier = Modifier,
    selectedItem: TabsBottomBarItems,
    onItemSelected: (TabsBottomBarItems) -> Unit,
) {
    BottomNavigationBar(
        modifier = modifier.height(60.dp),
        selectedItem = selectedItem,
        items = TabsBottomBarItems.entries.toTypedArray(),
        showLabel = false,
        onItemSelected = onItemSelected,
    )
}

@OptIn(ExperimentalResourceApi::class)
internal enum class TabsBottomBarItems : BottomBarItem {
    SCHEDULE {
        override val label: String @Composable get() = StudyAssistantRes.strings.scheduleBottomItem
        override val enabledIcon: DrawableResource @Composable get() = StudyAssistantRes.icons.schedule
        override val disabledIcon: DrawableResource @Composable get() = StudyAssistantRes.icons.scheduleDisabled
    },
    TASKS {
        override val label: String @Composable get() = StudyAssistantRes.strings.tasksBottomItem
        override val enabledIcon: DrawableResource @Composable get() = StudyAssistantRes.icons.tasks
        override val disabledIcon: DrawableResource @Composable get() = StudyAssistantRes.icons.tasksDisabled
    },
    INFO {
        override val label: String @Composable get() = StudyAssistantRes.strings.infoBottomItem
        override val enabledIcon: DrawableResource @Composable get() = StudyAssistantRes.icons.information
        override val disabledIcon: DrawableResource @Composable get() = StudyAssistantRes.icons.informationDisabled
    },
    PROFILE {
        override val label: String @Composable get() = StudyAssistantRes.strings.profileBottomItem
        override val enabledIcon: DrawableResource @Composable get() = StudyAssistantRes.icons.profile
        override val disabledIcon: DrawableResource @Composable get() = StudyAssistantRes.icons.profileDisabled
    },
}
