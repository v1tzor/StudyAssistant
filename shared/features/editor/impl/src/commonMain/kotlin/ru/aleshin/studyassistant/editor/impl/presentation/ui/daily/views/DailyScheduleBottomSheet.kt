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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.Instant
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.char
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.common.extensions.epochTimeDuration
import ru.aleshin.studyassistant.core.common.extensions.formatByTimeZone
import ru.aleshin.studyassistant.core.presentation.models.schedules.CustomScheduleUi
import ru.aleshin.studyassistant.core.ui.views.dayOfWeekNames
import ru.aleshin.studyassistant.core.ui.views.monthNames
import ru.aleshin.studyassistant.core.ui.views.sheet.StickyBottomSheet
import ru.aleshin.studyassistant.editor.impl.presentation.models.classes.FastEditDurations
import ru.aleshin.studyassistant.editor.impl.resources.Res
import ru.aleshin.studyassistant.editor.impl.resources.custom_schedule_date_title
import ru.aleshin.studyassistant.editor.impl.resources.edit_custom_schedule_title
import ru.aleshin.studyassistant.editor.impl.resources.fast_edit_breaks_duration_label
import ru.aleshin.studyassistant.editor.impl.resources.fast_edit_classes_duration_label
import ru.aleshin.studyassistant.editor.impl.resources.fast_edit_daily_schedule_header
import ru.aleshin.studyassistant.editor.impl.resources.fast_edit_start_of_day_label
import ru.aleshin.studyassistant.editor.impl.resources.ic_break
import ru.aleshin.studyassistant.editor.impl.resources.return_schedule_title
import ru.aleshin.studyassistant.editor.impl.resources.save_button_title
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.ic_class as core_ic_class
import ru.aleshin.studyassistant.core.ui.resources.ic_clock_outline as core_ic_clock_outline

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun DailyScheduleBottomSheet(
    modifier: Modifier = Modifier,
    sheetState: SheetState,
    layoutHeight: Int,
    isLoading: Boolean,
    editMode: Boolean,
    targetDate: Instant?,
    customSchedule: CustomScheduleUi?,
    onEditClick: () -> Unit,
    onSaveClick: () -> Unit,
    onReturnScheduleClick: () -> Unit,
    onEditStartOfDay: (Instant) -> Unit,
    onEditClassesDuration: (FastEditDurations) -> Unit,
    onEditBreaksDuration: (FastEditDurations) -> Unit,
) {
    StickyBottomSheet(
        modifier = modifier,
        sheetState = sheetState,
        layoutHeight = layoutHeight,
        header = {
            DailyScheduleSheetHeader(
                targetDate = targetDate,
            )
        },
        expandedContent = {
            DailyScheduleBottomSheetContent(
                enabledFastEdit = !isLoading,
                editMode = editMode,
                customSchedule = customSchedule,
                onEditStartOfDay = onEditStartOfDay,
                onEditBreaksDuration = onEditBreaksDuration,
                onEditClassesDuration = onEditClassesDuration,
            )
        },
        footer = { paddingValues ->
            DailyScheduleBottomSheetFooter(
                modifier = Modifier.padding(paddingValues),
                editMode = editMode,
                onEditClick = onEditClick,
                onSaveClick = onSaveClick,
                onReturnScheduleClick = onReturnScheduleClick,
            )
        },
    )
}

@Composable
internal fun DailyScheduleSheetHeader(
    modifier: Modifier = Modifier,
    targetDate: Instant?,
) {
    val localizedMonthNames = monthNames()
    val localizedDayOfWeekNames = dayOfWeekNames()
    val dateFormat = DateTimeComponents.Format {
        dayOfMonth()
        char(' ')
        monthName(localizedMonthNames)
        chars(", ")
        dayOfWeek(localizedDayOfWeekNames)
    }

    Row(
        modifier = modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp, top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = stringResource(Res.string.custom_schedule_date_title),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelMedium,
            )
            Text(
                modifier = Modifier.animateContentSize(),
                text = targetDate?.formatByTimeZone(dateFormat) ?: " ",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
        }
    }
}

@Composable
private fun DailyScheduleBottomSheetContent(
    modifier: Modifier = Modifier,
    enabledFastEdit: Boolean,
    editMode: Boolean,
    customSchedule: CustomScheduleUi?,
    onEditStartOfDay: (Instant) -> Unit,
    onEditClassesDuration: (FastEditDurations) -> Unit,
    onEditBreaksDuration: (FastEditDurations) -> Unit,
) {
    var startOfDayEditorDialogState by remember { mutableStateOf(false) }
    var classesDurationEditorDialogState by remember { mutableStateOf(false) }
    var breaksDurationEditorDialogState by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = editMode,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Column(
            modifier = modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(Res.string.fast_edit_daily_schedule_header),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelMedium,
            )
            Column {
                AssistChip(
                    onClick = { startOfDayEditorDialogState = true },
                    label = { Text(text = stringResource(Res.string.fast_edit_start_of_day_label)) },
                    enabled = enabledFastEdit,
                    leadingIcon = {
                        Icon(
                            modifier = Modifier.size(18.dp),
                            painter = painterResource(CoreRes.drawable.core_ic_clock_outline),
                            contentDescription = null,
                        )
                    },
                )
                AssistChip(
                    onClick = { classesDurationEditorDialogState = true },
                    label = { Text(text = stringResource(Res.string.fast_edit_classes_duration_label)) },
                    enabled = enabledFastEdit,
                    leadingIcon = {
                        Icon(
                            modifier = Modifier.size(18.dp),
                            painter = painterResource(CoreRes.drawable.core_ic_class),
                            contentDescription = null,
                        )
                    },
                )
                AssistChip(
                    onClick = { breaksDurationEditorDialogState = true },
                    label = { Text(text = stringResource(Res.string.fast_edit_breaks_duration_label)) },
                    enabled = enabledFastEdit,
                    leadingIcon = {
                        Icon(
                            modifier = Modifier.size(18.dp),
                            painter = painterResource(Res.drawable.ic_break),
                            contentDescription = null,
                        )
                    },
                )
            }
        }
    }

    if (startOfDayEditorDialogState) {
        StartOfDayEditorDialog(
            startOfDay = customSchedule?.classes?.firstOrNull()?.timeRange?.from,
            onDismiss = { startOfDayEditorDialogState = false },
            onConfirm = {
                onEditStartOfDay(it)
                startOfDayEditorDialogState = false
            },
        )
    }

    if (classesDurationEditorDialogState && customSchedule != null) {
        val classesDurations = remember(customSchedule.classes) {
            customSchedule.classes.map {
                Pair(customSchedule.classes.indexOf(it).inc(), epochTimeDuration(it.timeRange))
            }
        }

        ClassesDurationEditorDialog(
            classesDurations = classesDurations,
            onDismiss = { classesDurationEditorDialogState = false },
            onConfirm = { base, specific ->
                val fastEditDurations = FastEditDurations(base, specific)
                onEditClassesDuration(fastEditDurations)
                classesDurationEditorDialogState = false
            },
        )
    }

    if (breaksDurationEditorDialogState && customSchedule != null) {
        val breaksDurations = remember(customSchedule.classes) {
            buildList {
                customSchedule.classes.forEachIndexed { index, classModel ->
                    if (index != customSchedule.classes.lastIndex) {
                        val nextClassModel = customSchedule.classes[index + 1]
                        add(
                            Pair(
                                index.inc(),
                                epochTimeDuration(
                                    classModel.timeRange.to,
                                    nextClassModel.timeRange.from
                                )
                            )
                        )
                    }
                }
            }
        }

        BreaksDurationEditorDialog(
            breaksDurations = breaksDurations,
            onDismiss = { breaksDurationEditorDialogState = false },
            onConfirm = { base, specific ->
                val fastEditDurations = FastEditDurations(base, specific)
                onEditBreaksDuration(fastEditDurations)
                breaksDurationEditorDialogState = false
            },
        )
    }
}

@Composable
private fun DailyScheduleBottomSheetFooter(
    modifier: Modifier = Modifier,
    editMode: Boolean,
    onEditClick: () -> Unit,
    onSaveClick: () -> Unit,
    onReturnScheduleClick: () -> Unit,
) {
    Row(
        modifier = modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (editMode) {
            Button(
                onClick = onSaveClick,
                modifier = Modifier.weight(1f),
            ) {
                Text(text = stringResource(Res.string.save_button_title))
            }
            FilledTonalButton(onClick = onReturnScheduleClick) {
                Text(text = stringResource(Res.string.return_schedule_title))
            }
        } else {
            Button(
                onClick = onEditClick,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(Res.string.edit_custom_schedule_title))
            }
        }
    }
}
