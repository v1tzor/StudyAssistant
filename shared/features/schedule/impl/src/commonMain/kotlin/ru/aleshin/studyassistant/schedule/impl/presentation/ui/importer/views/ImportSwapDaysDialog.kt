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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.isoDayNumber
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.ui.mappers.mapToSting
import ru.aleshin.studyassistant.core.ui.views.ClickableInfoTextField
import ru.aleshin.studyassistant.core.ui.views.DialogButtons
import ru.aleshin.studyassistant.core.ui.views.DialogHeader
import ru.aleshin.studyassistant.core.ui.views.ExpandedIcon
import ru.aleshin.studyassistant.schedule.impl.resources.Res
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_swap_days
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_swap_days_first
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_swap_days_second
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_swap_days_title
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.ic_select_date as core_ic_select_date
import ru.aleshin.studyassistant.core.ui.resources.save_confirm_title as core_save_confirm_title

/**
 * @author Stanislav Aleshin on 20.08.2026.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun ImportSwapDaysDialog(
    modifier: Modifier = Modifier,
    initialFirstDay: Int,
    initialSecondDay: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit,
) {
    var firstDay by remember { mutableIntStateOf(initialFirstDay.coerceIn(1, 7)) }
    var secondDay by remember { mutableIntStateOf(initialSecondDay.coerceIn(1, 7)) }

    BasicAlertDialog(onDismissRequest = onDismiss, modifier = modifier) {
        Surface(
            modifier = Modifier.width(350.dp).wrapContentHeight(),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainer,
            shadowElevation = 4.dp,
        ) {
            Column {
                DialogHeader(
                    header = stringResource(Res.string.schedule_import_swap_days),
                    title = stringResource(Res.string.schedule_import_swap_days_title),
                )
                HorizontalDivider()
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    DayDropdownField(
                        label = stringResource(Res.string.schedule_import_swap_days_first),
                        selectedDay = firstDay,
                        onSelect = { firstDay = it },
                    )
                    DayDropdownField(
                        label = stringResource(Res.string.schedule_import_swap_days_second),
                        selectedDay = secondDay,
                        onSelect = { secondDay = it },
                    )
                }
                DialogButtons(
                    enabledConfirm = firstDay != secondDay,
                    confirmTitle = stringResource(CoreRes.string.core_save_confirm_title),
                    onCancelClick = onDismiss,
                    onConfirmClick = { onConfirm(firstDay, secondDay) },
                )
            }
        }
    }
}

@Composable
private fun DayDropdownField(
    modifier: Modifier = Modifier,
    label: String,
    selectedDay: Int,
    onSelect: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = DayOfWeek.entries.firstOrNull { day -> day.isoDayNumber == selectedDay }

    Box(modifier = modifier.fillMaxWidth()) {
        ClickableInfoTextField(
            onClick = { expanded = true },
            value = selected?.mapToSting(),
            label = label,
            placeholder = label,
            infoIcon = painterResource(CoreRes.drawable.core_ic_select_date),
            backgroundColor = MaterialTheme.colorScheme.surfaceContainer,
            trailingIcon = {
                ExpandedIcon(
                    isExpanded = expanded,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DayOfWeek.entries.forEach { day ->
                DropdownMenuItem(
                    text = { Text(text = day.mapToSting()) },
                    onClick = {
                        onSelect(day.isoDayNumber)
                        expanded = false
                    },
                )
            }
        }
    }
}
