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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
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
 * @author Stanislav Aleshin on 17.08.2026.
 */
@Composable
internal fun ProfileExpandedActions(
    modifier: Modifier = Modifier,
    onAboutAppClick: () -> Unit,
    onGeneralSettingsClick: () -> Unit,
    onNotifySettingsClick: () -> Unit,
    onCalendarSettingsClick: () -> Unit,
    onAiSettingsClick: () -> Unit,
    onShareScheduleClick: () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ProfileActionViewItem(
                modifier = Modifier.weight(1f),
                onClick = onGeneralSettingsClick,
                title = stringResource(Res.string.general_settings_title),
                icon = {
                    Icon(
                        painter = painterResource(Res.drawable.ic_settings_common),
                        contentDescription = null,
                    )
                },
            )
            ProfileActionViewItem(
                modifier = Modifier.weight(1f),
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ProfileActionViewItem(
                modifier = Modifier.weight(1f),
                onClick = onCalendarSettingsClick,
                title = stringResource(Res.string.calendar_settings_title),
                icon = {
                    Icon(
                        painter = painterResource(Res.drawable.ic_calendar),
                        contentDescription = null,
                    )
                },
            )
            ProfileActionViewItem(
                modifier = Modifier.weight(1f),
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
        ProfileActionViewItem(
            modifier = Modifier.fillMaxWidth(),
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
