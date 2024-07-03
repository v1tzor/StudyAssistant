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

package ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.views

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.tasks.impl.presentation.theme.TasksThemeRes
import theme.StudyAssistantRes

/**
 * @author Stanislav Aleshin on 03.07.2024.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun HomeworksTopBar(
    modifier: Modifier = Modifier,
    onOverviewClick: () -> Unit,
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = {
            Text(text = TasksThemeRes.strings.homeworksHeader)
        },
        actions = {
            IconButton(onClick = onOverviewClick) {
                Icon(
                    painter = painterResource(TasksThemeRes.icons.viewShortTasks),
                    contentDescription = StudyAssistantRes.strings.backIconDesc,
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        )
    )
}