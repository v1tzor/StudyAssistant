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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DayOfWeek
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.presentation.models.organizations.OrganizationShortUi
import ru.aleshin.studyassistant.core.presentation.models.subjects.SubjectUi
import ru.aleshin.studyassistant.core.presentation.models.users.EmployeeUi
import ru.aleshin.studyassistant.core.ui.mappers.mapToSting
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.SmallInfoBadge
import ru.aleshin.studyassistant.core.ui.views.sheet.BaseSelectorBottomSheet
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
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_week_value
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.ic_class as core_ic_class
import ru.aleshin.studyassistant.core.ui.resources.ic_employee as core_ic_employee
import ru.aleshin.studyassistant.core.ui.resources.ic_organization as core_ic_organization
import ru.aleshin.studyassistant.core.ui.resources.not_selected_title as core_not_selected_title

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun ImportEntryCard(
    entry: ScheduleImportEntryUi,
    enabled: Boolean,
    organizations: List<OrganizationShortUi>,
    subjects: List<SubjectUi>,
    employees: List<EmployeeUi>,
    onToggle: () -> Unit,
    onUpdate: (ScheduleImportEntryUi) -> Unit,
    modifier: Modifier = Modifier,
) {
    var openOrganizationSelectorSheet by remember { mutableStateOf(false) }
    var openSubjectSelectorSheet by remember { mutableStateOf(false) }
    var openEmployeeSelectorSheet by remember { mutableStateOf(false) }
    var openDaySelectorSheet by remember { mutableStateOf(false) }
    var openWeekSelectorSheet by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (entry.included) {
                MaterialTheme.colorScheme.surfaceContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            },
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Checkbox(
                    checked = entry.included,
                    enabled = enabled,
                    onCheckedChange = { onToggle() },
                )
                Text(
                    text = stringResource(Res.string.schedule_import_entry_title, entry.id + 1),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (entry.included) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (entry.included) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        ImportSelectorField(
                            modifier = Modifier.weight(1f),
                            label = stringResource(Res.string.schedule_import_day_label),
                            value = DayOfWeek.entries[entry.dayOfWeek - 1].mapToSting(),
                            icon = null,
                            enabled = enabled,
                            onClick = { openDaySelectorSheet = true },
                        )
                        ImportSelectorField(
                            modifier = Modifier.weight(1f),
                            label = stringResource(Res.string.schedule_import_week_label),
                            value = stringResource(Res.string.schedule_import_week_value, entry.repeatWeek),
                            icon = null,
                            enabled = enabled,
                            onClick = { openWeekSelectorSheet = true },
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedTextField(
                            modifier = Modifier.weight(1f),
                            value = entry.startTime,
                            onValueChange = { onUpdate(entry.copy(startTime = it)) },
                            label = { Text(stringResource(Res.string.schedule_import_start_label)) },
                            enabled = enabled,
                            singleLine = true,
                            shape = MaterialTheme.shapes.large,
                        )
                        OutlinedTextField(
                            modifier = Modifier.weight(1f),
                            value = entry.endTime,
                            onValueChange = { onUpdate(entry.copy(endTime = it)) },
                            label = { Text(stringResource(Res.string.schedule_import_end_label)) },
                            enabled = enabled,
                            singleLine = true,
                            shape = MaterialTheme.shapes.large,
                        )
                    }

                    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)

                    ImportSelectorField(
                        label = stringResource(Res.string.schedule_import_organization_label),
                        value = entry.organization,
                        icon = painterResource(CoreRes.drawable.core_ic_organization),
                        enabled = enabled,
                        isLinked = entry.organizationId != null,
                        onClick = { openOrganizationSelectorSheet = true },
                    )

                    ImportSelectorField(
                        label = stringResource(Res.string.schedule_import_subject_label),
                        value = entry.subject,
                        icon = painterResource(CoreRes.drawable.core_ic_class),
                        enabled = enabled,
                        isLinked = entry.subjectId != null,
                        onClick = { openSubjectSelectorSheet = true },
                    )

                    ImportSelectorField(
                        label = stringResource(Res.string.schedule_import_teacher_label),
                        value = entry.teacher,
                        icon = painterResource(CoreRes.drawable.core_ic_employee),
                        enabled = enabled,
                        isLinked = entry.teacherId != null,
                        onClick = { openEmployeeSelectorSheet = true },
                    )

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = entry.office,
                        onValueChange = { onUpdate(entry.copy(office = it)) },
                        label = { Text(stringResource(Res.string.schedule_import_office_label)) },
                        enabled = enabled,
                        singleLine = true,
                        shape = MaterialTheme.shapes.large,
                    )
                }
            }
        }
    }

    if (openOrganizationSelectorSheet) {
        BaseSelectorBottomSheet(
            selected = organizations.find { it.uid == entry.organizationId },
            items = organizations,
            header = stringResource(Res.string.schedule_import_organization_label),
            title = null,
            itemView = { org ->
                Text(
                    modifier = Modifier.clickable {
                        onUpdate(entry.copy(organization = org.shortName, organizationId = org.uid))
                        openOrganizationSelectorSheet = false
                    }.fillMaxWidth().padding(8.dp),
                    text = org.shortName,
                )
            },
            onDismissRequest = { openOrganizationSelectorSheet = false },
            onConfirm = { openOrganizationSelectorSheet = false },
        )
    }

    if (openSubjectSelectorSheet) {
        val filteredSubjects = subjects.filter { 
            it.organizationId == entry.organizationId || entry.organizationId == null 
        }
        BaseSelectorBottomSheet(
            selected = subjects.find { it.uid == entry.subjectId },
            items = filteredSubjects,
            header = stringResource(Res.string.schedule_import_subject_label),
            title = null,
            itemView = { subject ->
                Text(
                    modifier = Modifier.clickable {
                        onUpdate(entry.copy(subject = subject.name, subjectId = subject.uid))
                        openSubjectSelectorSheet = false
                    }.fillMaxWidth().padding(8.dp),
                    text = subject.name,
                )
            },
            onDismissRequest = { openSubjectSelectorSheet = false },
            onConfirm = { openSubjectSelectorSheet = false },
        )
    }

    if (openEmployeeSelectorSheet) {
        val filteredEmployees = employees.filter { 
            it.organizationId == entry.organizationId || entry.organizationId == null 
        }
        BaseSelectorBottomSheet(
            selected = employees.find { it.uid == entry.teacherId },
            items = filteredEmployees,
            header = stringResource(Res.string.schedule_import_teacher_label),
            title = null,
            itemView = { employee ->
                val name = listOfNotNull(employee.secondName, employee.firstName, employee.patronymic).joinToString(" ")
                Text(
                    modifier = Modifier.clickable {
                        onUpdate(entry.copy(teacher = name, teacherId = employee.uid))
                        openEmployeeSelectorSheet = false
                    }.fillMaxWidth().padding(8.dp),
                    text = name,
                )
            },
            onDismissRequest = { openEmployeeSelectorSheet = false },
            onConfirm = { openEmployeeSelectorSheet = false },
        )
    }

    if (openDaySelectorSheet) {
        BaseSelectorBottomSheet(
            selected = DayOfWeek.entries[entry.dayOfWeek - 1],
            items = DayOfWeek.entries,
            header = stringResource(Res.string.schedule_import_day_label),
            title = null,
            itemView = { day ->
                Text(
                    modifier = Modifier.clickable {
                        onUpdate(entry.copy(dayOfWeek = day.ordinal + 1))
                        openDaySelectorSheet = false
                    }.fillMaxWidth().padding(8.dp),
                    text = day.mapToSting(),
                )
            },
            onDismissRequest = { openDaySelectorSheet = false },
            onConfirm = { openDaySelectorSheet = false },
        )
    }

    if (openWeekSelectorSheet) {
        val weeks = (1..3).toList()
        BaseSelectorBottomSheet(
            selected = entry.repeatWeek,
            items = weeks,
            header = stringResource(Res.string.schedule_import_week_label),
            title = null,
            itemView = { week ->
                Text(
                    modifier = Modifier.clickable {
                        onUpdate(entry.copy(repeatWeek = week))
                        openWeekSelectorSheet = false
                    }.fillMaxWidth().padding(8.dp),
                    text = stringResource(Res.string.schedule_import_week_value, week),
                )
            },
            onDismissRequest = { openWeekSelectorSheet = false },
            onConfirm = { openWeekSelectorSheet = false },
        )
    }
}

@Composable
private fun ImportSelectorField(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: Painter?,
    enabled: Boolean,
    isLinked: Boolean = false,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (isLinked) {
                SmallInfoBadge(
                    containerColor = StudyAssistantRes.colors.accents.greenContainer,
                    contentColor = StudyAssistantRes.colors.accents.green,
                ) {
                    Text(text = "Linked")
                }
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (icon != null) {
                Icon(
                    modifier = Modifier.size(18.dp),
                    painter = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                modifier = Modifier.weight(1f),
                text = value.ifBlank { stringResource(CoreRes.string.core_not_selected_title) },
                style = MaterialTheme.typography.bodyLarge,
                color = if (value.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
            )
            Icon(
                modifier = Modifier.size(20.dp),
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
