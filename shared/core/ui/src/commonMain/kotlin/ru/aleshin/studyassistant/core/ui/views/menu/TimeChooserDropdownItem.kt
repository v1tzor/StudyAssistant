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

package ru.aleshin.studyassistant.core.ui.views.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.core.common.extensions.hoursToMillis
import ru.aleshin.studyassistant.core.common.extensions.minutesToMillis
import ru.aleshin.studyassistant.core.common.extensions.setHoursAndMinutes
import ru.aleshin.studyassistant.core.common.extensions.toHorses
import ru.aleshin.studyassistant.core.common.extensions.toMinutesInHours
import ru.aleshin.studyassistant.core.domain.entities.organizations.Millis
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.MenuTextField

/**
 * @author Stanislav Aleshin on 25.05.2024.
 */
@Composable
@OptIn(ExperimentalResourceApi::class)
fun TimeChooserDropdownItem(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    currentTime: Instant?,
    onChangeTime: (Instant?) -> Unit,
) {
    // TODO: Handle AM/PM format
    var editableHour by remember {
        mutableStateOf(currentTime?.toLocalDateTime(TimeZone.currentSystemDefault())?.hour)
    }
    var editableMinute by remember {
        mutableStateOf(currentTime?.toLocalDateTime(TimeZone.currentSystemDefault())?.minute)
    }

    Row(
        modifier = modifier.height(48.dp).padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(StudyAssistantRes.icons.timeOutline),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MenuTextField(
                enabled = enabled,
                modifier = Modifier.weight(0.45f),
                text = editableHour?.toString() ?: "",
                onTextChange = {
                    val hour = it.toIntOrNull() ?: if (it.isEmpty()) {
                        return@MenuTextField let { editableHour = null }
                    } else {
                        return@MenuTextField
                    }
                    if (hour in 0..23) {
                        editableHour = hour
                        val instant = currentTime ?: Clock.System.now()
                        val newTime = instant.setHoursAndMinutes(
                            hour = hour,
                            minute = editableMinute ?: 0.apply { editableMinute = this }
                        )
                        onChangeTime(newTime)
                    }
                },
                suffix = { Text(text = StudyAssistantRes.strings.hoursSuffix) },
            )
            MenuTextField(
                enabled = enabled,
                modifier = Modifier.weight(0.55f),
                text = editableMinute?.toString() ?: "",
                onTextChange = {
                    val minute = it.toIntOrNull() ?: if (it.isEmpty()) {
                        return@MenuTextField let {
                            editableMinute = null
                        }
                    } else {
                        return@MenuTextField
                    }
                    if (minute in 0..59) {
                        editableMinute = minute
                        val instant = currentTime ?: Clock.System.now()
                        val newTime = instant.setHoursAndMinutes(
                            hour = editableHour ?: instant.toLocalDateTime(TimeZone.currentSystemDefault()).hour.apply {
                                editableHour = this
                            },
                            minute = minute
                        )
                        onChangeTime(newTime)
                    }
                },
                suffix = { Text(text = StudyAssistantRes.strings.minutesSuffix) },
            )
            IconButton(
                enabled = enabled,
                onClick = {
                    editableHour = null
                    editableMinute = null
                    onChangeTime(null)
                },
                modifier = Modifier.size(24.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
            ) {
                Icon(
                    modifier = Modifier.size(18.dp),
                    imageVector = Icons.Filled.Close,
                    contentDescription = null,
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalResourceApi::class)
fun DurationChooserDropdownItem(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    currentDuration: Millis?,
    onChangeTime: (Millis?) -> Unit,
) {
    var editableHour by remember {
        mutableStateOf(currentDuration?.toHorses())
    }
    var editableMinute by remember {
        mutableStateOf(currentDuration?.toMinutesInHours())
    }

    Row(
        modifier = modifier.fillMaxWidth().height(48.dp).padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(StudyAssistantRes.icons.duration),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MenuTextField(
                enabled = enabled,
                modifier = Modifier.weight(0.6f),
                text = editableHour?.toString() ?: "",
                onTextChange = {
                    val hour = it.toLongOrNull() ?: if (it.isEmpty()) {
                        return@MenuTextField let {
                            editableHour = null
                            onChangeTime(editableMinute?.toInt()?.minutesToMillis())
                        }
                    } else {
                        return@MenuTextField
                    }
                    if (hour in 0..23) {
                        editableHour = hour
                        val hourMillis = hour.toInt().hoursToMillis()
                        val minuteMillis = editableMinute?.toInt()?.minutesToMillis() ?: 0L.apply {
                            editableMinute = this
                        }
                        onChangeTime(hourMillis + minuteMillis)
                    }
                },
                suffix = { Text(text = StudyAssistantRes.strings.hoursSuffix) },
            )
            MenuTextField(
                enabled = enabled,
                modifier = Modifier.weight(0.4f),
                text = editableMinute?.toString() ?: "",
                onTextChange = {
                    val minute = it.toLongOrNull() ?: if (it.isEmpty()) {
                        return@MenuTextField let {
                            editableMinute = null
                            onChangeTime(editableHour?.toInt()?.hoursToMillis())
                        }
                    } else {
                        return@MenuTextField
                    }
                    if (minute in 0..59) {
                        editableMinute = minute
                        val hourMillis = editableHour?.toInt()?.hoursToMillis() ?: 0L.apply {
                            editableHour = this
                        }
                        val minuteMillis = minute.toInt().minutesToMillis()
                        onChangeTime(hourMillis + minuteMillis)
                    }
                },
                suffix = { Text(text = StudyAssistantRes.strings.minutesSuffix) },
            )
            IconButton(
                enabled = enabled,
                onClick = {
                    editableHour = 0
                    editableMinute = 0
                    onChangeTime(0)
                },
                modifier = Modifier.size(24.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
            ) {
                Icon(
                    modifier = Modifier.size(18.dp),
                    imageVector = Icons.Filled.Close,
                    contentDescription = null,
                )
            }
        }
    }
}
