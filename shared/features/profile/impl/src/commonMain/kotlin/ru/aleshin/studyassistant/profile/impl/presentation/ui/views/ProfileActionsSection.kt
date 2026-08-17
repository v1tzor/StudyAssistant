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

package ru.aleshin.studyassistant.profile.impl.presentation.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.profile.impl.resources.Res
import ru.aleshin.studyassistant.profile.impl.resources.about_app_title
import ru.aleshin.studyassistant.profile.impl.resources.ai_settings_title
import ru.aleshin.studyassistant.profile.impl.resources.calendar_settings_title
import ru.aleshin.studyassistant.profile.impl.resources.general_settings_title
import ru.aleshin.studyassistant.profile.impl.resources.ic_calendar
import ru.aleshin.studyassistant.profile.impl.resources.ic_notifications
import ru.aleshin.studyassistant.profile.impl.resources.ic_settings_common
import ru.aleshin.studyassistant.profile.impl.resources.ic_table
import ru.aleshin.studyassistant.profile.impl.resources.notify_settings_title
import ru.aleshin.studyassistant.profile.impl.resources.shared_schedules_view_title

/**
 * @author Stanislav Aleshin on 21.04.2024.
 */
@Composable
internal fun ProfileActionsSection(
    columns: Int,
    modifier: Modifier = Modifier,
    onAboutAppClick: () -> Unit,
    onGeneralSettingsClick: () -> Unit,
    onNotifySettingsClick: () -> Unit,
    onCalendarSettingsClick: () -> Unit,
    onAiSettingsClick: () -> Unit,
    onShareScheduleClick: () -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(
            key = "ShareSchedule",
            span = { GridItemSpan(maxLineSpan) },
        ) {
            ProfileActionViewItem(
                modifier = Modifier.fillMaxWidth(),
                onClick = onShareScheduleClick,
                title = stringResource(Res.string.shared_schedules_view_title),
                icon = {
                    Icon(
                        painter = painterResource(Res.drawable.ic_table),
                        contentDescription = null,
                    )
                },
            )
        }
        item(key = "GeneralSettings") {
            ProfileActionViewItem(
                onClick = onGeneralSettingsClick,
                title = stringResource(Res.string.general_settings_title),
                icon = {
                    Icon(
                        painter = painterResource(Res.drawable.ic_settings_common),
                        contentDescription = null,
                    )
                },
            )
        }
        item(key = "NotifySettings") {
            ProfileActionViewItem(
                onClick = onNotifySettingsClick,
                title = stringResource(Res.string.notify_settings_title),
                icon = {
                    Icon(
                        painter = painterResource(Res.drawable.ic_notifications),
                        contentDescription = null,
                    )
                },
            )
        }
        item(key = "CalendarSettings") {
            ProfileActionViewItem(
                onClick = onCalendarSettingsClick,
                title = stringResource(Res.string.calendar_settings_title),
                icon = {
                    Icon(
                        painter = painterResource(Res.drawable.ic_calendar),
                        contentDescription = null,
                    )
                },
            )
        }
        item(key = "AiSettings") {
            ProfileActionViewItem(
                onClick = onAiSettingsClick,
                title = stringResource(Res.string.ai_settings_title),
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                    )
                },
            )
        }
        item(
            key = "AboutApp",
            span = { GridItemSpan(maxLineSpan) },
        ) {
            ProfileActionViewItem(
                onClick = onAboutAppClick,
                title = stringResource(Res.string.about_app_title),
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                    )
                },
            )
        }
    }
}

@Composable
internal fun ProfileActionViewItem(
    onClick: () -> Unit,
    title: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(104.dp),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.size(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    androidx.compose.runtime.CompositionLocalProvider(
                        androidx.compose.material3.LocalContentColor provides MaterialTheme.colorScheme.primary,
                        content = icon,
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
            }
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}
