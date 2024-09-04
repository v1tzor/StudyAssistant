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

package ru.aleshin.studyassistant.settings.impl.presentation.ui.calendar.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRightAlt
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.core.common.extensions.formatByTimeZone
import ru.aleshin.studyassistant.core.common.extensions.mapEpochTimeToInstant
import ru.aleshin.studyassistant.core.ui.mappers.mapToSting
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.theme.material.topSide
import ru.aleshin.studyassistant.core.ui.views.ClickableTextField
import ru.aleshin.studyassistant.core.ui.views.DialogButtons
import ru.aleshin.studyassistant.core.ui.views.DialogHeader
import ru.aleshin.studyassistant.core.ui.views.ExpandedIcon
import ru.aleshin.studyassistant.core.ui.views.dayMonthFormat
import ru.aleshin.studyassistant.core.ui.views.dialog.BaseCheckedDialog
import ru.aleshin.studyassistant.core.ui.views.dialog.CheckedItemView
import ru.aleshin.studyassistant.core.ui.views.shortWeekdayDayMonthFormat
import ru.aleshin.studyassistant.settings.impl.presentation.models.organizations.OrganizationShortUi
import ru.aleshin.studyassistant.settings.impl.presentation.models.settings.EditHolidaysUi
import ru.aleshin.studyassistant.settings.impl.presentation.models.settings.HolidaysUi
import ru.aleshin.studyassistant.settings.impl.presentation.models.settings.convertToBase
import ru.aleshin.studyassistant.settings.impl.presentation.models.settings.convertToEdit
import ru.aleshin.studyassistant.settings.impl.presentation.theme.SettingsThemeRes

/**
 * @author Stanislav Aleshin on 30.08.2024.
 */
@Composable
internal fun HolidaysView(
    modifier: Modifier = Modifier,
    allOrganizations: List<OrganizationShortUi>,
    holidays: List<HolidaysUi>,
    onUpdateHolidays: (List<HolidaysUi>) -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.large,
    ) {
        Column {
            var showHolidayItems by rememberSaveable { mutableStateOf(false) }

            Surface(
                onClick = { showHolidayItems = showHolidayItems.not() },
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = if (showHolidayItems) {
                    MaterialTheme.shapes.large.topSide
                } else {
                    MaterialTheme.shapes.large
                },
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.BeachAccess,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        modifier = Modifier.weight(1f),
                        text = SettingsThemeRes.strings.holidaysViewTitle,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    ExpandedIcon(isExpanded = showHolidayItems)
                }
            }
            AnimatedVisibility(visible = showHolidayItems) {
                Column {
                    HorizontalDivider()
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        holidays.forEach { holiday ->
                            var holidaysEditorState by remember { mutableStateOf(false) }

                            HolidayViewItem(
                                onClick = { holidaysEditorState = true },
                                start = holiday.start,
                                end = holiday.end,
                                organizations = holiday.organizations.mapNotNull { organizationId ->
                                    allOrganizations.find { it.uid == organizationId }
                                },
                            )

                            if (holidaysEditorState) {
                                HolidaysEditorDialog(
                                    holiday = holiday,
                                    allOrganizations = allOrganizations,
                                    onDismissRequest = { holidaysEditorState = false },
                                    onDelete = { targetHoliday ->
                                        val updatedHolidays = buildList {
                                            addAll(holidays)
                                            remove(targetHoliday)
                                        }
                                        onUpdateHolidays(updatedHolidays)
                                        holidaysEditorState = false
                                    },
                                    onSave = { targetHoliday ->
                                        val updatedHolidays = buildList {
                                            addAll(holidays)
                                            remove(holiday)
                                            add(targetHoliday)
                                        }
                                        onUpdateHolidays(updatedHolidays)
                                        holidaysEditorState = false
                                    },
                                )
                            }
                        }
                        var holidaysCreatorState by remember { mutableStateOf(false) }

                        AddHolidayViewItem(onClick = { holidaysCreatorState = true })

                        if (holidaysCreatorState) {
                            HolidaysEditorDialog(
                                allOrganizations = allOrganizations,
                                onDismissRequest = { holidaysCreatorState = false },
                                onSave = { targetHoliday ->
                                    val updatedHolidays = buildList {
                                        addAll(holidays)
                                        add(targetHoliday)
                                    }
                                    onUpdateHolidays(updatedHolidays)
                                    holidaysCreatorState = false
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HolidayViewItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    start: Instant,
    end: Instant,
    organizations: List<OrganizationShortUi> = emptyList(),
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val dateFormat = DateTimeComponents.Formats.dayMonthFormat(StudyAssistantRes.strings)
                Text(
                    text = start.format(dateFormat),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleMedium,
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowRightAlt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = end.format(dateFormat),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            HorizontalDivider()
            LazyRow(
                modifier = Modifier.heightIn(min = 48.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(organizations, key = { it.uid }) { organization ->
                    HolidayOrganizationView(shortName = organization.shortName)
                }
            }
        }
    }
}

@Composable
private fun AddHolidayViewItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
            )
            Text(
                text = StudyAssistantRes.strings.addTitle,
                maxLines = 1,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun HolidayOrganizationView(
    modifier: Modifier = Modifier,
    shortName: String,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        color = Color.Transparent,
    ) {
        Row(
            modifier = Modifier.padding(start = 8.dp, end = 16.dp, top = 6.dp, bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier.size(18.dp),
                painter = painterResource(StudyAssistantRes.icons.organizationGeo),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = shortName,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun HolidaysEditorDialog(
    modifier: Modifier = Modifier,
    holiday: HolidaysUi? = null,
    allOrganizations: List<OrganizationShortUi>,
    onDismissRequest: () -> Unit,
    onSave: (HolidaysUi) -> Unit,
    onDelete: (HolidaysUi) -> Unit = {},
) {
    var editableHoliday by remember {
        mutableStateOf(holiday?.convertToEdit() ?: EditHolidaysUi())
    }
    val selectedOrganizations = remember(editableHoliday) {
        editableHoliday.organizations.mapNotNull { organizationId ->
            allOrganizations.find { it.uid == organizationId }
        }
    }

    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
    ) {
        Surface(
            modifier = Modifier.width(350.dp).wrapContentHeight(),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainer,
            shadowElevation = 4.dp,
        ) {
            Column {
                DialogHeader(header = SettingsThemeRes.strings.holidaysEditorHeader)
                HorizontalDivider()
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    var organizationsPickerState by remember { mutableStateOf(false) }
                    var startDatePickerState by remember { mutableStateOf(false) }
                    var endDatePickerState by remember { mutableStateOf(false) }

                    ClickableTextField(
                        onClick = { startDatePickerState = true },
                        value = editableHoliday.start?.formatByTimeZone(
                            format = DateTimeComponents.Formats.shortWeekdayDayMonthFormat(StudyAssistantRes.strings)
                        ),
                        label = SettingsThemeRes.strings.holidaysStartLabel,
                        placeholder = SettingsThemeRes.strings.holidaysDatePlaceholder,
                        backgroundColor = MaterialTheme.colorScheme.surfaceContainer,
                        trailingIcon = {
                            Icon(
                                painter = painterResource(StudyAssistantRes.icons.selectDate),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    )

                    ClickableTextField(
                        onClick = { endDatePickerState = true },
                        value = editableHoliday.end?.formatByTimeZone(
                            format = DateTimeComponents.Formats.shortWeekdayDayMonthFormat(StudyAssistantRes.strings)
                        ),
                        label = SettingsThemeRes.strings.holidaysEndLabel,
                        placeholder = SettingsThemeRes.strings.holidaysDatePlaceholder,
                        backgroundColor = MaterialTheme.colorScheme.surfaceContainer,
                        trailingIcon = {
                            Icon(
                                painter = painterResource(StudyAssistantRes.icons.selectDate),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    )

                    ClickableTextField(
                        onClick = { organizationsPickerState = true },
                        value = selectedOrganizations.takeIf { it.isNotEmpty() }?.joinToString { it.shortName },
                        label = SettingsThemeRes.strings.holidaysOrganizationsLabel,
                        backgroundColor = MaterialTheme.colorScheme.surfaceContainer,
                        placeholder = SettingsThemeRes.strings.holidaysOrganizationsPlaceholder,
                        trailingIcon = {
                            ExpandedIcon(
                                isExpanded = organizationsPickerState,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                    )

                    if (startDatePickerState) {
                        HolidayDatePicker(
                            onDismiss = { startDatePickerState = false },
                            onSelectedDate = { date ->
                                editableHoliday = editableHoliday.copy(start = date)
                                startDatePickerState = false
                            }
                        )
                    }

                    if (endDatePickerState) {
                        HolidayDatePicker(
                            onDismiss = { endDatePickerState = false },
                            onSelectedDate = { date ->
                                editableHoliday = editableHoliday.copy(end = date)
                                endDatePickerState = false
                            }
                        )
                    }

                    if (organizationsPickerState) {
                        OrganizationsSelectorDialog(
                            selected = selectedOrganizations,
                            organizations = allOrganizations,
                            onDismiss = { organizationsPickerState = false },
                            onConfirm = { organizations ->
                                editableHoliday = editableHoliday.copy(
                                    organizations = organizations.map { it.uid }
                                )
                                organizationsPickerState = false
                            },
                        )
                    }
                }
                DialogButtons(
                    enabledConfirmFirst = editableHoliday.isValid(),
                    enabledConfirmSecond = holiday != null,
                    confirmFirstTitle = StudyAssistantRes.strings.saveConfirmTitle,
                    confirmSecondTitle = StudyAssistantRes.strings.deleteConfirmTitle,
                    onCancelClick = onDismissRequest,
                    onConfirmFirstClick = { onSave(editableHoliday.convertToBase()) },
                    onConfirmSecondClick = { if (holiday != null) onDelete(holiday) },
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun HolidayDatePicker(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onSelectedDate: (Instant?) -> Unit,
) {
    val datePickerState = rememberDatePickerState()
    val confirmEnabled by remember { derivedStateOf { datePickerState.selectedDateMillis != null } }

    DatePickerDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = confirmEnabled,
                onClick = {
                    val selectedDate = datePickerState.selectedDateMillis ?: return@TextButton
                    val birthday = selectedDate.mapEpochTimeToInstant()
                    onSelectedDate.invoke(birthday)
                },
                content = { Text(text = StudyAssistantRes.strings.selectConfirmTitle) }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = StudyAssistantRes.strings.cancelTitle)
            }
        },
    ) {
        DatePicker(
            state = datePickerState,
            title = {
                Text(
                    modifier = Modifier.padding(start = 24.dp, top = 24.dp),
                    text = StudyAssistantRes.strings.datePickerDialogHeader,
                )
            },
            headline = {
                Text(
                    modifier = Modifier.padding(start = 24.dp),
                    text = SettingsThemeRes.strings.holidaysDatePickerHeadline,
                )
            },
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun OrganizationsSelectorDialog(
    modifier: Modifier = Modifier,
    selected: List<OrganizationShortUi>,
    organizations: List<OrganizationShortUi>,
    onDismiss: () -> Unit,
    onConfirm: (List<OrganizationShortUi>) -> Unit,
) {
    var selectedOrganizations by remember { mutableStateOf(selected) }

    BaseCheckedDialog(
        modifier = modifier,
        selected = selectedOrganizations,
        items = organizations,
        header = SettingsThemeRes.strings.holidaysOrganizationsSelectorHeader,
        title = null,
        itemView = { organization ->
            val isSelected = selectedOrganizations.contains(organization)
            CheckedItemView(
                onClick = {
                    selectedOrganizations = if (isSelected) {
                        selectedOrganizations.toMutableList().apply { remove(organization) }
                    } else {
                        selectedOrganizations.toMutableList().apply { add(organization) }
                    }
                },
                selected = isSelected,
                title = organization.shortName,
                label = organization.type.mapToSting(StudyAssistantRes.strings),
            )
        },
        onDismiss = onDismiss,
        onConfirm = onConfirm,
    )
}