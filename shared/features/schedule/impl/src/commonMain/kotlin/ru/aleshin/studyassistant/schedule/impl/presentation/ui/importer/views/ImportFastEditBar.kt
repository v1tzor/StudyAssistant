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
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DayOfWeek
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.domain.entities.organizations.Millis
import ru.aleshin.studyassistant.core.ui.mappers.mapToSting
import ru.aleshin.studyassistant.schedule.impl.presentation.models.importing.ImportFastEditDurationMath
import ru.aleshin.studyassistant.schedule.impl.presentation.models.importing.ScheduleImportClassUi
import ru.aleshin.studyassistant.schedule.impl.resources.Res
import ru.aleshin.studyassistant.schedule.impl.resources.ic_break
import ru.aleshin.studyassistant.schedule.impl.resources.ic_swap_horiz
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_fast_edit_add_class
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_fast_edit_breaks
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_fast_edit_classes
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_fast_edit_start
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_swap_days
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.friday_short_title as core_friday_short_title
import ru.aleshin.studyassistant.core.ui.resources.ic_class as core_ic_class
import ru.aleshin.studyassistant.core.ui.resources.ic_clock_outline as core_ic_clock_outline
import ru.aleshin.studyassistant.core.ui.resources.monday_short_title as core_monday_short_title
import ru.aleshin.studyassistant.core.ui.resources.saturday_short_title as core_saturday_short_title
import ru.aleshin.studyassistant.core.ui.resources.sunday_short_title as core_sunday_short_title
import ru.aleshin.studyassistant.core.ui.resources.thursday_short_title as core_thursday_short_title
import ru.aleshin.studyassistant.core.ui.resources.tuesday_short_title as core_tuesday_short_title
import ru.aleshin.studyassistant.core.ui.resources.wednesday_short_title as core_wednesday_short_title

/**
 * @author Stanislav Aleshin on 20.08.2026.
 */
@Composable
internal fun ImportFastEditBar(
    modifier: Modifier = Modifier,
    dayClasses: List<ScheduleImportClassUi>,
    onUpdateStartOfDay: (String) -> Unit,
    onUpdateClassesDuration: (Millis, List<Pair<Int, Long>>) -> Unit,
    onUpdateBreaksDuration: (Millis, List<Pair<Int, Long>>) -> Unit,
    onAddClass: () -> Unit,
    onSwapDaysClick: () -> Unit,
) {
    var startDialogOpen by remember { mutableStateOf(false) }
    var classesDialogOpen by remember { mutableStateOf(false) }
    var breaksDialogOpen by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        IconButton(
            modifier = Modifier.size(32.dp),
            onClick = onSwapDaysClick
        ) {
            Icon(
                modifier = Modifier.size(18.dp),
                painter = painterResource(Res.drawable.ic_swap_horiz),
                contentDescription = stringResource(Res.string.schedule_import_swap_days),
            )
        }
        AssistChip(
            onClick = { startDialogOpen = true },
            enabled = dayClasses.isNotEmpty(),
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
            enabled = dayClasses.isNotEmpty(),
            label = { Text(text = stringResource(Res.string.schedule_import_fast_edit_classes)) },
            leadingIcon = {
                Icon(
                    modifier = Modifier.size(18.dp),
                    painter = painterResource(CoreRes.drawable.core_ic_class),
                    contentDescription = null,
                )
            },
        )
        AssistChip(
            onClick = { breaksDialogOpen = true },
            enabled = dayClasses.size > 1,
            label = { Text(text = stringResource(Res.string.schedule_import_fast_edit_breaks)) },
            leadingIcon = {
                Icon(
                    modifier = Modifier.size(18.dp),
                    painter = painterResource(Res.drawable.ic_break),
                    contentDescription = null,
                )
            },
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
        ImportStartOfDayEditorDialog(
            startOfDay = ImportFastEditDurationMath.firstStartInstant(dayClasses),
            onDismiss = { startDialogOpen = false },
            onConfirm = { instant ->
                onUpdateStartOfDay(instant.dateTime().time.formatClock())
                startDialogOpen = false
            },
        )
    }
    if (classesDialogOpen && dayClasses.isNotEmpty()) {
        val classesDurations = remember(dayClasses) {
            ImportFastEditDurationMath.classDurations(dayClasses)
        }
        ImportClassesDurationEditorDialog(
            classesDurations = classesDurations,
            onDismiss = { classesDialogOpen = false },
            onConfirm = { base, specific ->
                onUpdateClassesDuration(
                    base,
                    specific.map { duration -> duration.number to duration.duration },
                )
                classesDialogOpen = false
            },
        )
    }
    if (breaksDialogOpen && dayClasses.size > 1) {
        val breaksDurations = remember(dayClasses) {
            ImportFastEditDurationMath.breakDurations(dayClasses)
        }
        ImportBreaksDurationEditorDialog(
            breaksDurations = breaksDurations,
            onDismiss = { breaksDialogOpen = false },
            onConfirm = { base, specific ->
                onUpdateBreaksDuration(
                    base,
                    specific.map { duration -> duration.number to duration.duration },
                )
                breaksDialogOpen = false
            },
        )
    }
}

@Composable
private fun DayOfWeek.mapToShortTitle(): String = when (this) {
    DayOfWeek.MONDAY -> stringResource(CoreRes.string.core_monday_short_title)
    DayOfWeek.TUESDAY -> stringResource(CoreRes.string.core_tuesday_short_title)
    DayOfWeek.WEDNESDAY -> stringResource(CoreRes.string.core_wednesday_short_title)
    DayOfWeek.THURSDAY -> stringResource(CoreRes.string.core_thursday_short_title)
    DayOfWeek.FRIDAY -> stringResource(CoreRes.string.core_friday_short_title)
    DayOfWeek.SATURDAY -> stringResource(CoreRes.string.core_saturday_short_title)
    DayOfWeek.SUNDAY -> stringResource(CoreRes.string.core_sunday_short_title)
    else -> mapToSting()
}

private fun kotlinx.datetime.LocalTime.formatClock(): String {
    val hours = hour.toString().padStart(2, '0')
    val minutes = minute.toString().padStart(2, '0')
    return "$hours:$minutes"
}
