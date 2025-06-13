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

package ru.aleshin.studyassistant.tasks.impl.presentation.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import kotlinx.datetime.format.DateTimeComponents
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.core.common.extensions.formatByTimeZone
import ru.aleshin.studyassistant.core.common.extensions.mapEpochTimeToInstant
import ru.aleshin.studyassistant.core.common.extensions.shiftDay
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalType
import ru.aleshin.studyassistant.core.domain.entities.organizations.Millis
import ru.aleshin.studyassistant.core.ui.mappers.toMinutesAndHoursTitle
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.ClickableTextField
import ru.aleshin.studyassistant.core.ui.views.DialogButtons
import ru.aleshin.studyassistant.core.ui.views.DialogHeader
import ru.aleshin.studyassistant.core.ui.views.dialog.BaseDatePickerDialog
import ru.aleshin.studyassistant.core.ui.views.dialog.DurationPickerDialog
import ru.aleshin.studyassistant.core.ui.views.shortWeekdayDayMonthFormat
import ru.aleshin.studyassistant.tasks.impl.presentation.models.goals.GoalCreateModelUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.TodoDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.theme.TasksThemeRes

/**
 * @author Stanislav Aleshin on 04.06.2025.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun GoalCreatorDialog(
    modifier: Modifier = Modifier,
    contentType: GoalType,
    currentDate: Instant,
    contentHomework: HomeworkUi? = null,
    contentTodo: TodoDetailsUi? = null,
    onDismiss: () -> Unit,
    onCreate: (GoalCreateModelUi) -> Unit,
) {
    BasicAlertDialog(onDismissRequest = onDismiss, modifier = modifier) {
        var selectedDate by remember { mutableStateOf<Instant?>(null) }
        var desiredTime by remember { mutableStateOf<Millis?>(null) }
        var datePickerState by remember { mutableStateOf(false) }
        var durationPickerState by remember { mutableStateOf(false) }
        Surface(
            modifier = Modifier.width(350.dp).wrapContentHeight(),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainer,
            shadowElevation = 4.dp,
        ) {
            Column {
                DialogHeader(
                    header = TasksThemeRes.strings.goalCreatorHeadline,
                    title = TasksThemeRes.strings.goalCreatorTitle,
                )
                HorizontalDivider()
                Column(
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    ClickableTextField(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { datePickerState = true },
                        label = TasksThemeRes.strings.goalCreatorDateFieldTitle,
                        value = selectedDate?.formatByTimeZone(
                            format = DateTimeComponents.Formats.shortWeekdayDayMonthFormat(
                                StudyAssistantRes.strings
                            )
                        ),
                        placeholder = TasksThemeRes.strings.goalCreatorDateFieldPlaceholder,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.EditCalendar,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        },
                        backgroundColor = MaterialTheme.colorScheme.surfaceContainer,
                    )
                    ClickableTextField(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { durationPickerState = true },
                        label = TasksThemeRes.strings.goalCreatorDesiredTimeFieldTitle,
                        value = desiredTime?.toMinutesAndHoursTitle(),
                        placeholder = 0L.toMinutesAndHoursTitle(),
                        leadingIcon = {
                            Icon(
                                painter = painterResource(TasksThemeRes.icons.timer),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        },
                        backgroundColor = MaterialTheme.colorScheme.surfaceContainer,
                    )
                }
                DialogButtons(
                    enabledConfirm = selectedDate != null,
                    confirmTitle = TasksThemeRes.strings.goalCreatorConfirmTitle,
                    onCancelClick = onDismiss,
                    onConfirmClick = {
                        val createModel = GoalCreateModelUi(
                            date = checkNotNull(selectedDate),
                            contentType = contentType,
                            contentHomework = contentHomework,
                            contentTodo = contentTodo,
                            desiredTime = desiredTime,
                        )
                        onCreate(createModel)
                    },
                )
            }
        }
        if (datePickerState) {
            BaseDatePickerDialog(
                state = rememberDatePickerState(
                    selectableDates = object : SelectableDates {
                        override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                            val timeRange = TimeRange(
                                from = currentDate.startThisDay(),
                                to = currentDate.shiftDay(22),
                            )
                            return timeRange.containsDate(utcTimeMillis.mapEpochTimeToInstant())
                        }
                    }
                ),
                onDismiss = { datePickerState = false },
                onConfirmDate = {
                    selectedDate = it
                    datePickerState = false
                },
            )
        }

        if (durationPickerState) {
            DurationPickerDialog(
                headerTitle = TasksThemeRes.strings.goalSheetDesiredTimePickerTitle,
                duration = desiredTime,
                onDismiss = { durationPickerState = false },
                onSelectedDuration = {
                    desiredTime = if (it == 0L) null else it
                    durationPickerState = false
                },
            )
        }
    }
}