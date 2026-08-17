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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.daily.views

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import kotlinx.datetime.Instant
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.char
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.common.extensions.formatByTimeZone
import ru.aleshin.studyassistant.core.ui.views.TopAppBarTitle
import ru.aleshin.studyassistant.core.ui.views.dayOfWeekNames
import ru.aleshin.studyassistant.core.ui.views.monthNames
import ru.aleshin.studyassistant.editor.impl.resources.Res
import ru.aleshin.studyassistant.editor.impl.resources.daily_schedule_editor_header

/**
 * @author Stanislav Aleshin on 14.07.2024.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun DailyScheduleTopBar(
    modifier: Modifier = Modifier,
    isExpanded: Boolean = false,
    selectedDate: Instant? = null,
    onBackClick: () -> Unit,
) {
    val navigationIcon: @Composable () -> Unit = {
        IconButton(onClick = onBackClick) {
            Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = null)
        }
    }
    val colors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.background,
    )
    if (isExpanded) {
        TopAppBar(
            modifier = modifier,
            title = {
                TopAppBarTitle(
                    header = stringResource(Res.string.daily_schedule_editor_header),
                    title = selectedDate?.let { date -> formatDailyScheduleDate(date) },
                    textAlign = TextAlign.Start,
                )
            },
            navigationIcon = navigationIcon,
            colors = colors,
        )
    } else {
        CenterAlignedTopAppBar(
            modifier = modifier,
            title = {
                Text(text = stringResource(Res.string.daily_schedule_editor_header))
            },
            navigationIcon = navigationIcon,
            colors = colors,
        )
    }
}

@Composable
internal fun formatDailyScheduleDate(targetDate: Instant): String {
    val localizedMonthNames = monthNames()
    val localizedDayOfWeekNames = dayOfWeekNames()
    val dateFormat = DateTimeComponents.Format {
        dayOfMonth()
        char(' ')
        monthName(localizedMonthNames)
        chars(", ")
        dayOfWeek(localizedDayOfWeekNames)
    }
    return targetDate.formatByTimeZone(dateFormat)
}