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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlinx.datetime.isoDayNumber
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.extensions.setHoursAndMinutes
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.subject.EventType
import ru.aleshin.studyassistant.core.presentation.models.subjects.SubjectUi
import ru.aleshin.studyassistant.core.presentation.models.users.EmployeeUi
import ru.aleshin.studyassistant.core.ui.mappers.mapToSting
import ru.aleshin.studyassistant.core.ui.mappers.mapToString
import ru.aleshin.studyassistant.core.ui.theme.material.full
import ru.aleshin.studyassistant.core.ui.views.ClickableInfoTextField
import ru.aleshin.studyassistant.core.ui.views.ExpandedIcon
import ru.aleshin.studyassistant.core.ui.views.InfoTextField
import ru.aleshin.studyassistant.core.ui.views.dialog.SelectorAddItemView
import ru.aleshin.studyassistant.core.ui.views.dialog.SelectorItemView
import ru.aleshin.studyassistant.core.ui.views.dialog.TimePickerDialog
import ru.aleshin.studyassistant.core.ui.views.sheet.BaseSelectorBottomSheet
import ru.aleshin.studyassistant.core.ui.views.sheet.MediumDragHandle
import ru.aleshin.studyassistant.schedule.impl.presentation.models.importing.ScheduleImportClassUi
import ru.aleshin.studyassistant.schedule.impl.resources.Res
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_apply_button
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_class_editor_title
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_day_label
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_day_selector_header
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_day_selector_title
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_end_label
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_event_type_label
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_event_type_placeholder
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_event_type_selector_header
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_event_type_selector_title
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_location_label
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_new_subject_name
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_new_teacher_name
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_office_label
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_start_label
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_subject_label
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_subject_placeholder
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_subject_selector_header
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_subject_selector_title
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_teacher_label
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_teacher_placeholder
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_teacher_selector_header
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_teacher_selector_title
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_time_placeholder
import kotlin.time.Clock
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.delete_confirm_title as core_delete_confirm_title
import ru.aleshin.studyassistant.core.ui.resources.ic_class as core_ic_class
import ru.aleshin.studyassistant.core.ui.resources.ic_clock_outline as core_ic_clock_outline
import ru.aleshin.studyassistant.core.ui.resources.ic_employee as core_ic_employee
import ru.aleshin.studyassistant.core.ui.resources.ic_map_marker as core_ic_map_marker
import ru.aleshin.studyassistant.core.ui.resources.ic_organization as core_ic_organization
import ru.aleshin.studyassistant.core.ui.resources.ic_select_date as core_ic_select_date

/**
 * @author Stanislav Aleshin on 16.08.2026.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun ImportClassEditorSheet(
    modifier: Modifier = Modifier,
    classModel: ScheduleImportClassUi,
    subjects: List<SubjectUi>,
    employees: List<EmployeeUi>,
    originalSubjectIds: Set<UID>,
    originalEmployeeIds: Set<UID>,
    onDismiss: () -> Unit,
    onConfirm: (ScheduleImportClassUi) -> Unit,
    onDelete: () -> Unit,
    onAddSubject: (String) -> Unit,
    onAddEmployee: (String) -> Unit,
) {
    var editable by remember(classModel) { mutableStateOf(classModel) }
    var isSubjectSelectorOpen by remember { mutableStateOf(false) }
    var isTeacherSelectorOpen by remember { mutableStateOf(false) }
    var isEventTypeSelectorOpen by remember { mutableStateOf(false) }
    var isDaySelectorOpen by remember { mutableStateOf(false) }
    var isStartPickerOpen by remember { mutableStateOf(false) }
    var isEndPickerOpen by remember { mutableStateOf(false) }
    val selectedSubject = subjects.firstOrNull { subject -> subject.uid == editable.subjectId }
    val selectedTeacher = employees.firstOrNull { employee -> employee.uid == editable.teacherId }
    val selectedDay = DayOfWeek.entries.firstOrNull { day -> day.isoDayNumber == editable.dayOfWeek }
    val isTimeRangeValid = remember(editable.startTime, editable.endTime) {
        isClassTimeRangeValid(editable.startTime, editable.endTime)
    }
    val newSubjectName = stringResource(Res.string.schedule_import_new_subject_name)
    val newTeacherName = stringResource(Res.string.schedule_import_new_teacher_name)
    val sheetContainerColor = MaterialTheme.colorScheme.surfaceContainerLow

    ModalBottomSheet(
        modifier = modifier,
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = sheetContainerColor,
        dragHandle = { MediumDragHandle() },
        contentWindowInsets = { WindowInsets.navigationBars },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = stringResource(Res.string.schedule_import_class_editor_title),
                style = MaterialTheme.typography.titleLarge,
            )
            ClickableInfoTextField(
                onClick = { isSubjectSelectorOpen = true },
                modifier = Modifier.padding(start = 16.dp, end = 24.dp),
                value = selectedSubject?.name,
                label = stringResource(Res.string.schedule_import_subject_label),
                placeholder = stringResource(Res.string.schedule_import_subject_placeholder),
                infoIcon = painterResource(CoreRes.drawable.core_ic_class),
                backgroundColor = sheetContainerColor,
                leadingIcon = {
                    Surface(
                        shape = MaterialTheme.shapes.full,
                        color = selectedSubject?.color?.let { Color(it) }
                            ?: MaterialTheme.colorScheme.outlineVariant,
                        content = { Box(modifier = Modifier.size(8.dp, 24.dp)) },
                    )
                },
                trailingIcon = {
                    ExpandedIcon(
                        isExpanded = isSubjectSelectorOpen,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            )
            ClickableInfoTextField(
                onClick = { isEventTypeSelectorOpen = true },
                modifier = Modifier.padding(start = 16.dp, end = 24.dp),
                value = editable.eventType?.mapToString(),
                label = stringResource(Res.string.schedule_import_event_type_label),
                placeholder = stringResource(Res.string.schedule_import_event_type_placeholder),
                infoIcon = painterResource(CoreRes.drawable.core_ic_class),
                backgroundColor = sheetContainerColor,
                trailingIcon = {
                    ExpandedIcon(
                        isExpanded = isEventTypeSelectorOpen,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            )
            ClickableInfoTextField(
                onClick = { isTeacherSelectorOpen = true },
                modifier = Modifier.padding(start = 16.dp, end = 24.dp),
                value = selectedTeacher?.fullName(),
                label = stringResource(Res.string.schedule_import_teacher_label),
                placeholder = stringResource(Res.string.schedule_import_teacher_placeholder),
                infoIcon = painterResource(CoreRes.drawable.core_ic_employee),
                backgroundColor = sheetContainerColor,
                trailingIcon = {
                    ExpandedIcon(
                        isExpanded = isTeacherSelectorOpen,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            )
            ClickableInfoTextField(
                onClick = { isStartPickerOpen = true },
                modifier = Modifier.padding(start = 16.dp, end = 24.dp),
                value = editable.startTime.takeIf(String::isNotBlank),
                label = stringResource(Res.string.schedule_import_start_label),
                placeholder = stringResource(Res.string.schedule_import_time_placeholder),
                infoIcon = painterResource(CoreRes.drawable.core_ic_clock_outline),
                backgroundColor = sheetContainerColor,
            )
            ClickableInfoTextField(
                onClick = { isEndPickerOpen = true },
                modifier = Modifier.padding(start = 16.dp, end = 24.dp),
                value = editable.endTime.takeIf(String::isNotBlank),
                label = stringResource(Res.string.schedule_import_end_label),
                placeholder = stringResource(Res.string.schedule_import_time_placeholder),
                infoIcon = painterResource(CoreRes.drawable.core_ic_clock_outline),
                backgroundColor = sheetContainerColor,
            )
            InfoTextField(
                modifier = Modifier.padding(start = 16.dp, end = 24.dp),
                value = editable.office,
                onValueChange = { value -> editable = editable.copy(office = value) },
                label = stringResource(Res.string.schedule_import_office_label),
                leadingInfoIcon = painterResource(CoreRes.drawable.core_ic_organization),
                containerColor = sheetContainerColor,
            )
            InfoTextField(
                modifier = Modifier.padding(start = 16.dp, end = 24.dp),
                value = editable.location.orEmpty(),
                onValueChange = { value -> editable = editable.copy(location = value) },
                label = stringResource(Res.string.schedule_import_location_label),
                leadingInfoIcon = painterResource(CoreRes.drawable.core_ic_map_marker),
                containerColor = sheetContainerColor,
            )
            ClickableInfoTextField(
                onClick = { isDaySelectorOpen = true },
                modifier = Modifier.padding(start = 16.dp, end = 24.dp),
                value = selectedDay?.mapToSting(),
                label = stringResource(Res.string.schedule_import_day_label),
                placeholder = stringResource(Res.string.schedule_import_day_label),
                infoIcon = painterResource(CoreRes.drawable.core_ic_select_date),
                backgroundColor = sheetContainerColor,
                trailingIcon = {
                    ExpandedIcon(
                        isExpanded = isDaySelectorOpen,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text(stringResource(CoreRes.string.core_delete_confirm_title))
                }
                Button(
                    modifier = Modifier.weight(1f),
                    enabled = isTimeRangeValid,
                    onClick = {
                        onConfirm(editable.copy(location = editable.location?.trim()?.takeIf(String::isNotEmpty)))
                    },
                ) {
                    Text(stringResource(Res.string.schedule_import_apply_button))
                }
            }
        }
    }

    if (isSubjectSelectorOpen) {
        ImportEntitySelectorSheet(
            selectedId = editable.subjectId,
            items = subjects.map { subject ->
                ImportSelectorItem(
                    id = subject.uid,
                    title = subject.name,
                    label = if (subject.uid in originalSubjectIds) subject.teacher?.fullName() else null,
                )
            },
            header = stringResource(Res.string.schedule_import_subject_selector_header),
            title = stringResource(Res.string.schedule_import_subject_selector_title),
            onAdd = { onAddSubject(newSubjectName) },
            onDismiss = { isSubjectSelectorOpen = false },
            onConfirm = { selectedId ->
                val subject = subjects.firstOrNull { item -> item.uid == selectedId }
                editable = editable.copy(
                    subjectId = selectedId,
                    eventType = editable.eventType ?: subject?.eventType,
                    office = editable.office.ifBlank { subject?.office.orEmpty() },
                )
                isSubjectSelectorOpen = false
            },
        )
    }
    if (isTeacherSelectorOpen) {
        ImportEntitySelectorSheet(
            selectedId = editable.teacherId,
            items = employees.map { employee ->
                ImportSelectorItem(
                    id = employee.uid,
                    title = employee.fullName(),
                    label = if (employee.uid in originalEmployeeIds) employee.post.mapToString() else null,
                )
            },
            header = stringResource(Res.string.schedule_import_teacher_selector_header),
            title = stringResource(Res.string.schedule_import_teacher_selector_title),
            onAdd = { onAddEmployee(newTeacherName) },
            onDismiss = { isTeacherSelectorOpen = false },
            onConfirm = { selectedId ->
                editable = editable.copy(teacherId = selectedId)
                isTeacherSelectorOpen = false
            },
        )
    }
    if (isEventTypeSelectorOpen) {
        ImportEntitySelectorSheet(
            selectedId = editable.eventType?.name,
            items = EventType.entries.map { type ->
                ImportSelectorItem(id = type.name, title = type.mapToString(), label = null)
            },
            header = stringResource(Res.string.schedule_import_event_type_selector_header),
            title = stringResource(Res.string.schedule_import_event_type_selector_title),
            onAdd = null,
            onDismiss = { isEventTypeSelectorOpen = false },
            onConfirm = { selectedId ->
                editable = editable.copy(eventType = EventType.entries.firstOrNull { type -> type.name == selectedId })
                isEventTypeSelectorOpen = false
            },
        )
    }
    if (isDaySelectorOpen) {
        ImportEntitySelectorSheet(
            selectedId = editable.dayOfWeek.toString(),
            items = DayOfWeek.entries.map { day ->
                ImportSelectorItem(id = day.isoDayNumber.toString(), title = day.mapToSting(), label = null)
            },
            header = stringResource(Res.string.schedule_import_day_selector_header),
            title = stringResource(Res.string.schedule_import_day_selector_title),
            onAdd = null,
            onDismiss = { isDaySelectorOpen = false },
            onConfirm = { selectedId ->
                editable = editable.copy(dayOfWeek = selectedId?.toIntOrNull() ?: editable.dayOfWeek)
                isDaySelectorOpen = false
            },
        )
    }
    if (isStartPickerOpen) {
        TimePickerDialog(
            initTime = clockInstant(editable.startTime),
            onDismiss = { isStartPickerOpen = false },
            onConfirmTime = { instant ->
                val startTime = formatClock(instant)
                editable = editable.copy(
                    startTime = startTime,
                    endTime = endKeepingDuration(
                        previousStart = editable.startTime,
                        previousEnd = editable.endTime,
                        newStart = startTime,
                    ),
                )
                isStartPickerOpen = false
            },
        )
    }
    if (isEndPickerOpen) {
        TimePickerDialog(
            initTime = clockInstant(editable.endTime),
            onDismiss = { isEndPickerOpen = false },
            onConfirmTime = { instant ->
                editable = editable.copy(endTime = formatClock(instant))
                isEndPickerOpen = false
            },
        )
    }
}

internal data class ImportSelectorItem(
    val id: String,
    val title: String,
    val label: String?,
)

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun ImportEntitySelectorSheet(
    modifier: Modifier = Modifier,
    selectedId: String?,
    items: List<ImportSelectorItem>,
    header: String,
    title: String,
    onAdd: (() -> Unit)?,
    onDismiss: () -> Unit,
    onConfirm: (String?) -> Unit,
) {
    var selected by remember { mutableStateOf(items.firstOrNull { item -> item.id == selectedId }) }

    BaseSelectorBottomSheet(
        modifier = modifier,
        selected = selected,
        items = items,
        itemKeys = { item -> item.id },
        header = header,
        title = title,
        itemView = { item ->
            SelectorItemView(
                onClick = { selected = item },
                selected = item.id == selected?.id,
                title = item.title,
                label = item.label,
            )
        },
        addItemView = onAdd?.let { add ->
            { SelectorAddItemView(onClick = add) }
        },
        onDismissRequest = onDismiss,
        onConfirm = { item -> onConfirm(item?.id) },
    )
}

private fun clockInstant(value: String): kotlin.time.Instant? {
    val time = parseClock(value) ?: return null
    return Clock.System.now().setHoursAndMinutes(time)
}

private fun formatClock(instant: kotlin.time.Instant): String {
    return instant.dateTime().time.formatClock()
}

private fun endKeepingDuration(
    previousStart: String,
    previousEnd: String,
    newStart: String,
): String {
    val from = parseClock(previousStart) ?: return previousEnd
    val to = parseClock(previousEnd) ?: return previousEnd
    val start = parseClock(newStart) ?: return previousEnd
    val durationMinutes = to.toMinutes() - from.toMinutes()
    if (durationMinutes <= 0) return previousEnd
    val endMinutes = start.toMinutes() + durationMinutes
    if (endMinutes !in 1 until MINUTES_IN_DAY) return previousEnd
    return minutesToClock(endMinutes)
}

private fun isClassTimeRangeValid(startTime: String, endTime: String): Boolean {
    val start = parseClock(startTime) ?: return false
    val end = parseClock(endTime) ?: return false
    return start < end
}

private fun parseClock(value: String): LocalTime? {
    val raw = value.trim().replace('.', ':')
    if (raw.isEmpty()) return null
    val parts = raw.split(':')
    if (parts.size !in 2..3) return null
    val hour = parts[0].toIntOrNull() ?: return null
    val minute = parts[1].toIntOrNull() ?: return null
    return runCatching { LocalTime(hour, minute) }.getOrNull()
}

private fun LocalTime.toMinutes(): Int = hour * 60 + minute

private fun LocalTime.formatClock(): String = minutesToClock(toMinutes())

private fun minutesToClock(total: Int): String {
    val hours = (total / 60).toString().padStart(2, '0')
    val minutes = (total % 60).toString().padStart(2, '0')
    return "$hours:$minutes"
}

private const val MINUTES_IN_DAY = 24 * 60
