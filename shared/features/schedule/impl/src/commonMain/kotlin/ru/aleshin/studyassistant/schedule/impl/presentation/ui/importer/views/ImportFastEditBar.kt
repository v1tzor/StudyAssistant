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

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.extensions.setHoursAndMinutes
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.domain.entities.organizations.Millis
import ru.aleshin.studyassistant.core.ui.views.dialog.DurationPickerDialog
import ru.aleshin.studyassistant.core.ui.views.dialog.TimePickerDialog
import ru.aleshin.studyassistant.schedule.impl.presentation.models.importing.ScheduleImportClassUi
import ru.aleshin.studyassistant.schedule.impl.resources.Res
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_fast_edit_add_class
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_fast_edit_breaks
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_fast_edit_classes
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_fast_edit_start
import kotlin.time.Clock
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.ic_clock_outline as core_ic_clock_outline

/**
 * @author Stanislav Aleshin on 20.08.2026.
 */
@Composable
internal fun ImportFastEditBar(
    modifier: Modifier = Modifier,
    weekClasses: List<ScheduleImportClassUi>,
    onUpdateStartOfDay: (String) -> Unit,
    onUpdateClassesDuration: (Millis) -> Unit,
    onUpdateBreaksDuration: (Millis) -> Unit,
    onAddClass: () -> Unit,
) {
    var startDialogOpen by remember { mutableStateOf(false) }
    var classesDialogOpen by remember { mutableStateOf(false) }
    var breaksDialogOpen by remember { mutableStateOf(false) }
    val firstStart = remember(weekClasses) {
        weekClasses.minByOrNull(ScheduleImportClassUi::startTime)?.startTime
    }
    val classDuration = remember(weekClasses) {
        modalDuration(weekClasses.mapNotNull(::classDurationMillis)) ?: DEFAULT_CLASS_DURATION
    }
    val breakDuration = remember(weekClasses) {
        modalDuration(weekClasses.groupBy { classModel ->
            classModel.dayOfWeek to classModel.repeatWeek
        }.values.flatMap { dayClasses ->
            dayClasses.sortedBy(ScheduleImportClassUi::startTime).zipWithNext { current, next ->
                minutesBetween(current.endTime, next.startTime)?.times(MILLIS_IN_MINUTE)
            }
        }.filterNotNull()) ?: DEFAULT_BREAK_DURATION
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AssistChip(
            onClick = { startDialogOpen = true },
            enabled = weekClasses.isNotEmpty(),
            label = { Text(text = stringResource(Res.string.schedule_import_fast_edit_start)) },
            leadingIcon = {
                Icon(
                    modifier = Modifier.size(18.dp),
                    painter = painterResource(CoreRes.drawable.core_ic_clock_outline),
                    contentDescription = null,
                )
            },
        )
        AssistChip(
            onClick = { classesDialogOpen = true },
            enabled = weekClasses.isNotEmpty(),
            label = { Text(text = stringResource(Res.string.schedule_import_fast_edit_classes)) },
        )
        AssistChip(
            onClick = { breaksDialogOpen = true },
            enabled = weekClasses.size > 1,
            label = { Text(text = stringResource(Res.string.schedule_import_fast_edit_breaks)) },
        )
        AssistChip(
            onClick = onAddClass,
            label = { Text(text = stringResource(Res.string.schedule_import_fast_edit_add_class)) },
            leadingIcon = {
                Icon(
                    modifier = Modifier.size(18.dp),
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                )
            },
        )
    }

    if (startDialogOpen) {
        val parsedStart = parseClock(firstStart) ?: LocalTime(8, 0)
        TimePickerDialog(
            initTime = Clock.System.now().startThisDay().setHoursAndMinutes(parsedStart),
            onDismiss = { startDialogOpen = false },
            onConfirmTime = { instant ->
                onUpdateStartOfDay(instant.dateTime().time.formatClock())
                startDialogOpen = false
            },
        )
    }
    if (classesDialogOpen) {
        DurationPickerDialog(
            headerTitle = stringResource(Res.string.schedule_import_fast_edit_classes),
            duration = classDuration,
            onDismiss = { classesDialogOpen = false },
            onSelectedDuration = { duration ->
                onUpdateClassesDuration(duration)
                classesDialogOpen = false
            },
        )
    }
    if (breaksDialogOpen) {
        DurationPickerDialog(
            headerTitle = stringResource(Res.string.schedule_import_fast_edit_breaks),
            duration = breakDuration,
            onDismiss = { breaksDialogOpen = false },
            onSelectedDuration = { duration ->
                onUpdateBreaksDuration(duration)
                breaksDialogOpen = false
            },
        )
    }
}

private fun classDurationMillis(classModel: ScheduleImportClassUi): Millis? {
    val start = parseClock(classModel.startTime) ?: return null
    val end = parseClock(classModel.endTime) ?: return null
    val minutes = end.toMinutes() - start.toMinutes()
    if (minutes <= 0) return null
    return minutes * MILLIS_IN_MINUTE
}

private fun modalDuration(durations: List<Millis>): Millis? {
    if (durations.isEmpty()) return null
    val megaCloned = durations.size > 1 && durations.toSet().size == 1 && durations.first() >= MEGA_DURATION
    if (megaCloned) return DEFAULT_CLASS_DURATION
    return durations.groupingBy { duration -> duration }.eachCount().maxBy { entry -> entry.value }.key
}

private fun minutesBetween(start: String, end: String): Int? {
    val from = parseClock(start) ?: return null
    val to = parseClock(end) ?: return null
    return (to.toMinutes() - from.toMinutes()).takeIf { amount -> amount >= 0 }
}

private fun parseClock(value: String?): LocalTime? {
    val raw = value?.trim().orEmpty().replace('.', ':')
    if (raw.isEmpty()) return null
    val parts = raw.split(':')
    if (parts.size !in 2..3) return null
    val hour = parts[0].toIntOrNull() ?: return null
    val minute = parts[1].toIntOrNull() ?: return null
    return runCatching { LocalTime(hour, minute) }.getOrNull()
}

private fun LocalTime.toMinutes(): Int = hour * 60 + minute

private fun LocalTime.formatClock(): String {
    val hours = hour.toString().padStart(2, '0')
    val minutes = minute.toString().padStart(2, '0')
    return "$hours:$minutes"
}

private const val MILLIS_IN_MINUTE = 60_000L
private const val DEFAULT_CLASS_DURATION = 45 * MILLIS_IN_MINUTE
private const val DEFAULT_BREAK_DURATION = 10 * MILLIS_IN_MINUTE
private const val MEGA_DURATION = 3 * 60 * MILLIS_IN_MINUTE
