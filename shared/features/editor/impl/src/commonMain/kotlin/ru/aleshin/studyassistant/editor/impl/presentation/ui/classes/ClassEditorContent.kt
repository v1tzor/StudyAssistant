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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.classes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import entities.subject.EventType
import functional.TimeRange
import kotlinx.datetime.Instant
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.char
import mappers.mapToString
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.editor.impl.presentation.models.ContactInfoUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.EmployeeUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.OrganizationShortUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.ScheduleTimeIntervalsUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.SubjectUi
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.contract.ClassEditorViewState
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.views.ClassTimeRangeChooser
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.views.EventTypeSelectorDialog
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.views.LocationSelectorDialog
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.views.OfficeSelectorDialog
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.views.OrganizationSelectorDialog
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.views.SubjectSelectorDialog
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.views.TeacherSelectorDialog
import theme.StudyAssistantRes
import theme.material.full
import views.ClickableInfoTextField
import views.ClickableTextField
import views.ExpandedIcon
import views.dialog.BaseTimePickerDialog

/**
 * @author Stanislav Aleshin on 02.06.2024
 */
@Composable
internal fun ClassEditorContent(
    state: ClassEditorViewState,
    modifier: Modifier,
    onAddOrganization: () -> Unit,
    onAddSubject: () -> Unit,
    onAddTeacher: () -> Unit,
    onAddLocation: (ContactInfoUi) -> Unit,
    onAddOffice: (Int) -> Unit,
    onSelectOrganization: (OrganizationShortUi?) -> Unit,
    onSelectSubject: (EventType?, SubjectUi?) -> Unit,
    onSelectTeacher: (EmployeeUi?) -> Unit,
    onSelectLocation: (ContactInfoUi?, Int?) -> Unit,
    onSelectTime: (Instant?, Instant?) -> Unit,
    onChangeNotifyParams: (Boolean) -> Unit,
) = with(state) {
    Column(
        modifier = modifier.padding(top = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        OrganizationInfoField(
            organization = editModel?.organization,
            allOrganization = organizations,
            onAddOrganization = onAddOrganization,
            onSelected = onSelectOrganization,
        )
        SubjectInfoField(
            subject = editModel?.subject,
            eventType = editModel?.eventType,
            allSubjects = subjects,
            onAddSubject = onAddSubject,
            onSelectedEventType = { onSelectSubject(it, editModel?.subject) },
            onSelectedSubject = { onSelectSubject(editModel?.eventType, it) },
        )
        TeacherInfoField(
            teacher = editModel?.teacher,
            allTeachers = teachers,
            onAddTeacher = onAddTeacher,
            onSelected = onSelectTeacher,
        )
        LocationInfoField(
            location = editModel?.location,
            office = editModel?.office,
            allLocations = locations,
            allOffices = offices,
            onAddOffice = onAddOffice,
            onAddLocation = onAddLocation,
            onSelectedLocation = { onSelectLocation(it, editModel?.office) },
            onSelectedOffice = { onSelectLocation(editModel?.location, it) }
        )
        TimeInfoField(
            startTime = editModel?.startTime,
            endTime = editModel?.endTime,
            timeIntervals = editModel?.organization?.scheduleTimeIntervals,
            classesTimeRanges = classesTimeRanges,
            onSelectedTime = onSelectTime,
        )
        NotifyParameter(
            notification = editModel?.notification ?: false,
            onChangeParams = onChangeNotifyParams,
        )
    }
}

@Composable
@OptIn(ExperimentalResourceApi::class)
internal fun OrganizationInfoField(
    modifier: Modifier = Modifier,
    organization: OrganizationShortUi?,
    allOrganization: List<OrganizationShortUi>,
    onAddOrganization: () -> Unit,
    onSelected: (OrganizationShortUi?) -> Unit,
) {
    var isOpenOrganizationSelector by remember { mutableStateOf(false) }

    ClickableInfoTextField(
        modifier = modifier,
        paddingValues = PaddingValues(start = 16.dp, end = 24.dp),
        value = organization?.shortName,
        label = EditorThemeRes.strings.organizationFieldLabel,
        placeholder = EditorThemeRes.strings.organizationFieldPlaceholder,
        leadingInfoIcon = painterResource(EditorThemeRes.icons.organization),
        trailingIcon = {
            ExpandedIcon(
                isExpanded = isOpenOrganizationSelector,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        onClick = { isOpenOrganizationSelector = true },
    )

    if (isOpenOrganizationSelector) {
        OrganizationSelectorDialog(
            selected = organization,
            organizations = allOrganization,
            onAddOrganization = onAddOrganization,
            onDismiss = { isOpenOrganizationSelector = false },
            onConfirm = {
                onSelected(it)
                isOpenOrganizationSelector = false
            },
        )
    }
}

@Composable
@OptIn(ExperimentalResourceApi::class)
internal fun SubjectInfoField(
    modifier: Modifier = Modifier,
    subject: SubjectUi?,
    eventType: EventType?,
    allSubjects: List<SubjectUi>,
    onAddSubject: () -> Unit,
    onSelectedSubject: (SubjectUi?) -> Unit,
    onSelectedEventType: (EventType?) -> Unit,
) {
    var isOpenEventTypeSelector by remember { mutableStateOf(false) }
    var isOpenSubjectSelector by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.padding(start = 16.dp, end = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Box(modifier = Modifier.height(61.dp), contentAlignment = Alignment.Center) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = painterResource(EditorThemeRes.icons.classes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ClickableTextField(
                value = eventType?.mapToString(StudyAssistantRes.strings),
                label = EditorThemeRes.strings.eventTypeFieldLabel,
                placeholder = EditorThemeRes.strings.eventTypeFieldPlaceholder,
                trailingIcon = {
                    ExpandedIcon(
                        isExpanded = isOpenEventTypeSelector,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                onClick = { isOpenEventTypeSelector = true },
            )
            ClickableTextField(
                value = subject?.name,
                label = null,
                placeholder = EditorThemeRes.strings.subjectFieldPlaceholder,
                leadingIcon = if (subject != null) {
                    {
                        Surface(
                            shape = MaterialTheme.shapes.full(),
                            color = Color(subject.color),
                            content = { Box(modifier = Modifier.size(8.dp, 24.dp)) },
                        )
                    }
                } else {
                    null
                },
                trailingIcon = {
                    ExpandedIcon(
                        isExpanded = isOpenSubjectSelector,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                singleLine = false,
                maxLines = 3,
                onClick = { isOpenSubjectSelector = true },
            )
        }
    }

    if (isOpenEventTypeSelector) {
        EventTypeSelectorDialog(
            selected = eventType,
            onDismiss = { isOpenEventTypeSelector = false },
            onConfirm = {
                onSelectedEventType(it)
                isOpenEventTypeSelector = false
            },
        )
    }

    if (isOpenSubjectSelector) {
        SubjectSelectorDialog(
            selected = subject,
            eventType = eventType,
            subjects = allSubjects,
            onAddSubject = onAddSubject,
            onDismiss = { isOpenSubjectSelector = false },
            onConfirm = {
                onSelectedSubject(it)
                isOpenSubjectSelector = false
            },
        )
    }
}

@Composable
@OptIn(ExperimentalResourceApi::class)
internal fun TeacherInfoField(
    modifier: Modifier = Modifier,
    teacher: EmployeeUi?,
    allTeachers: List<EmployeeUi>,
    onAddTeacher: () -> Unit,
    onSelected: (EmployeeUi?) -> Unit,
) {
    var isOpenTeacherSelector by remember { mutableStateOf(false) }

    ClickableInfoTextField(
        modifier = modifier,
        paddingValues = PaddingValues(start = 16.dp, end = 24.dp),
        value = teacher?.name(),
        label = EditorThemeRes.strings.organizationFieldLabel,
        placeholder = EditorThemeRes.strings.organizationFieldPlaceholder,
        leadingInfoIcon = painterResource(EditorThemeRes.icons.employee),
        trailingIcon = {
            ExpandedIcon(
                isExpanded = isOpenTeacherSelector,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        onClick = { isOpenTeacherSelector = true },
    )

    if (isOpenTeacherSelector) {
        TeacherSelectorDialog(
            selected = teacher,
            teachers = allTeachers,
            onAddTeacher = onAddTeacher,
            onDismiss = { isOpenTeacherSelector = false },
            onConfirm = {
                onSelected(it)
                isOpenTeacherSelector = false
            },
        )
    }
}

@Composable
@OptIn(ExperimentalResourceApi::class)
internal fun LocationInfoField(
    modifier: Modifier = Modifier,
    location: ContactInfoUi?,
    office: Int?,
    allLocations: List<ContactInfoUi>,
    allOffices: List<Int>,
    onAddLocation: (ContactInfoUi) -> Unit,
    onAddOffice: (Int) -> Unit,
    onSelectedLocation: (ContactInfoUi?) -> Unit,
    onSelectedOffice: (Int?) -> Unit,
) {
    var isOpenLocationSelector by remember { mutableStateOf(false) }
    var isOpenOfficeSelector by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.padding(start = 16.dp, end = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(EditorThemeRes.icons.location),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ClickableTextField(
                modifier = Modifier.weight(0.6f),
                value = (location?.label ?: location?.value)?.ifEmpty { null },
                label = EditorThemeRes.strings.locationFieldLabel,
                placeholder = EditorThemeRes.strings.locationFieldPlaceholder,
                trailingIcon = {
                    ExpandedIcon(
                        isExpanded = isOpenLocationSelector,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                onClick = { isOpenLocationSelector = true },
            )
            ClickableTextField(
                modifier = Modifier.weight(0.4f),
                value = office?.toString(),
                label = EditorThemeRes.strings.officeFieldLabel,
                placeholder = EditorThemeRes.strings.officeFieldPlaceholder,
                trailingIcon = {
                    ExpandedIcon(
                        isExpanded = isOpenOfficeSelector,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                onClick = { isOpenOfficeSelector = true },
            )
        }
    }

    if (isOpenLocationSelector) {
        LocationSelectorDialog(
            selected = location,
            locations = allLocations,
            onAddLocation = onAddLocation,
            onDismiss = { isOpenLocationSelector = false },
            onConfirm = {
                onSelectedLocation(it)
                isOpenLocationSelector = false
            },
        )
    }

    if (isOpenOfficeSelector) {
        OfficeSelectorDialog(
            selected = office,
            offices = allOffices,
            onAddOffice = onAddOffice,
            onDismiss = { isOpenOfficeSelector = false },
            onConfirm = {
                onSelectedOffice(it)
                isOpenOfficeSelector = false
            },
        )
    }
}

@Composable
@OptIn(ExperimentalResourceApi::class)
internal fun TimeInfoField(
    modifier: Modifier = Modifier,
    startTime: Instant?,
    endTime: Instant?,
    timeIntervals: ScheduleTimeIntervalsUi?,
    classesTimeRanges: List<TimeRange>,
    onSelectedTime: (Instant?, Instant?) -> Unit,
) {
    var isOpenStartTimePicker by remember { mutableStateOf(false) }
    var isOpenEndTimePicker by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.padding(start = 16.dp, end = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Box(modifier = Modifier.height(61.dp), contentAlignment = Alignment.Center) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = painterResource(EditorThemeRes.icons.time),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val timeFormat = DateTimeComponents.Format {
                    hour()
                    char(':')
                    minute()
                }
                ClickableTextField(
                    modifier = Modifier.weight(1f),
                    value = startTime?.format(timeFormat),
                    label = EditorThemeRes.strings.startTimeFieldLabel,
                    placeholder = EditorThemeRes.strings.startTimeFieldPlaceholder,
                    trailingIcon = {
                        ExpandedIcon(
                            isExpanded = isOpenStartTimePicker,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    onClick = { isOpenStartTimePicker = true },
                )
                ClickableTextField(
                    modifier = Modifier.weight(1f),
                    value = endTime?.format(timeFormat),
                    label = EditorThemeRes.strings.endTimeFieldLabel,
                    placeholder = EditorThemeRes.strings.endTimeFieldPlaceholder,
                    trailingIcon = {
                        ExpandedIcon(
                            isExpanded = isOpenEndTimePicker,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    onClick = { isOpenEndTimePicker = true },
                )
            }
            ClassTimeRangeChooser(
                timeIntervals = timeIntervals,
                classesTimeRanges = classesTimeRanges,
                onChoose = { onSelectedTime(it.from, it.to) },
            )
        }
    }

    if (isOpenStartTimePicker) {
        BaseTimePickerDialog(
            initTime = startTime,
            onDismiss = { isOpenStartTimePicker = false },
            onConfirmTime = { selectedStartTime ->
                onSelectedTime(selectedStartTime, endTime)
                isOpenStartTimePicker = false
            },
        )
    }

    if (isOpenEndTimePicker) {
        BaseTimePickerDialog(
            initTime = endTime,
            onDismiss = { isOpenEndTimePicker = false },
            onConfirmTime = { selectedEndTime ->
                onSelectedTime(startTime, selectedEndTime)
                isOpenEndTimePicker = false
            },
        )
    }
}

@Composable
@OptIn(ExperimentalResourceApi::class)
internal fun NotifyParameter(
    modifier: Modifier = Modifier,
    notification: Boolean,
    onChangeParams: (Boolean) -> Unit,
) {
    Row(
        modifier = modifier.height(56.dp).padding(start = 16.dp, end = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(EditorThemeRes.icons.notification),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = EditorThemeRes.strings.notificationParamsTitle,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = EditorThemeRes.strings.notificationParamsTitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelMedium,
            )
        }
        Switch(
            checked = notification,
            onCheckedChange = onChangeParams,
        )
    }
}
