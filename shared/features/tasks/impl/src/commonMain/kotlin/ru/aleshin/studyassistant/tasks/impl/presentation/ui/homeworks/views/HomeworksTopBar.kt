/*
 * Copyright 2026 Stanislav Aleshin
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

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.tasks.impl.resources.Res
import ru.aleshin.studyassistant.tasks.impl.resources.current_time_range_desc
import ru.aleshin.studyassistant.tasks.impl.resources.homeworks_header
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.back_icon_desc as core_back_icon_desc
import ru.aleshin.studyassistant.core.ui.resources.ic_calendar_today as core_ic_calendar_today

/**
 * @author Stanislav Aleshin on 03.07.2024.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun HomeworksTopBar(
    modifier: Modifier = Modifier,
    onCurrentTimeRangeClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = {
            Text(text = stringResource(Res.string.homeworks_header))
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(CoreRes.string.core_back_icon_desc),
                )
            }
        },
        actions = {
            IconButton(onClick = onCurrentTimeRangeClick) {
                Icon(
                    painter = painterResource(CoreRes.drawable.core_ic_calendar_today),
                    contentDescription = stringResource(Res.string.current_time_range_desc),
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        )
    )
}