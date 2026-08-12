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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.schedule.impl.presentation.models.importing.ScheduleImportEntryUi
import ru.aleshin.studyassistant.schedule.impl.resources.Res
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_day_label
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_end_label
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_entry_title
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_office_label
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_organization_label
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_start_label
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_subject_label
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_teacher_label
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_week_label

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
@Composable
internal fun ImportEntryCard(
    entry: ScheduleImportEntryUi,
    enabled: Boolean,
    onToggle: () -> Unit,
    onUpdate: (ScheduleImportEntryUi) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (entry.included) {
                MaterialTheme.colorScheme.surfaceContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            },
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = entry.included,
                    enabled = enabled,
                    onCheckedChange = { onToggle() },
                )
                Text(
                    text = stringResource(Res.string.schedule_import_entry_title, entry.id + 1),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = entry.subject,
                enabled = enabled && entry.included,
                onValueChange = { onUpdate(entry.copy(subject = it)) },
                label = { Text(stringResource(Res.string.schedule_import_subject_label)) },
                singleLine = true,
                isError = entry.included && entry.subject.isBlank(),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = entry.dayOfWeek.toString(),
                    enabled = enabled && entry.included,
                    onValueChange = { value ->
                        value.toIntOrNull()?.let { onUpdate(entry.copy(dayOfWeek = it)) }
                    },
                    label = { Text(stringResource(Res.string.schedule_import_day_label)) },
                    singleLine = true,
                    isError = entry.included && entry.dayOfWeek !in 1..7,
                )
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = entry.repeatWeek.toString(),
                    enabled = enabled && entry.included,
                    onValueChange = { value ->
                        value.toIntOrNull()?.let { onUpdate(entry.copy(repeatWeek = it)) }
                    },
                    label = { Text(stringResource(Res.string.schedule_import_week_label)) },
                    singleLine = true,
                    isError = entry.included && entry.repeatWeek !in 1..3,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = entry.startTime,
                    enabled = enabled && entry.included,
                    onValueChange = { onUpdate(entry.copy(startTime = it)) },
                    label = { Text(stringResource(Res.string.schedule_import_start_label)) },
                    singleLine = true,
                    isError = entry.included && !entry.startTime.isClockTime(),
                )
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = entry.endTime,
                    enabled = enabled && entry.included,
                    onValueChange = { onUpdate(entry.copy(endTime = it)) },
                    label = { Text(stringResource(Res.string.schedule_import_end_label)) },
                    singleLine = true,
                    isError = entry.included && !entry.endTime.isClockTime(),
                )
            }
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = entry.teacher,
                enabled = enabled && entry.included,
                onValueChange = { onUpdate(entry.copy(teacher = it)) },
                label = { Text(stringResource(Res.string.schedule_import_teacher_label)) },
                singleLine = true,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = entry.office,
                    enabled = enabled && entry.included,
                    onValueChange = { onUpdate(entry.copy(office = it)) },
                    label = { Text(stringResource(Res.string.schedule_import_office_label)) },
                    singleLine = true,
                )
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = entry.organization,
                    enabled = enabled && entry.included,
                    onValueChange = { onUpdate(entry.copy(organization = it)) },
                    label = { Text(stringResource(Res.string.schedule_import_organization_label)) },
                    singleLine = true,
                )
            }
        }
    }
}

private fun String.isClockTime(): Boolean {
    val parts = split(':')
    val hour = parts.getOrNull(0)?.toIntOrNull()
    val minute = parts.getOrNull(1)?.toIntOrNull()
    return parts.size == 2 &&
        hour != null && hour in 0..23 &&
        minute != null && minute in 0..59
}
