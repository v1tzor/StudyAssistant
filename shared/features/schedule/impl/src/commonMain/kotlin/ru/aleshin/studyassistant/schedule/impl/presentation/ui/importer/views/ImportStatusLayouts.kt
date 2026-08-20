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

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.isoDayNumber
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.presentation.models.organizations.OrganizationShortUi
import ru.aleshin.studyassistant.core.presentation.models.subjects.SubjectUi
import ru.aleshin.studyassistant.core.presentation.models.users.EmployeeUi
import ru.aleshin.studyassistant.core.ui.ads.AdPlacement
import ru.aleshin.studyassistant.core.ui.ads.YandexInlineBanner
import ru.aleshin.studyassistant.core.ui.resources.ic_tooltip
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.InfoTextField
import ru.aleshin.studyassistant.schedule.impl.presentation.models.importing.ScheduleImportClassUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.schedule.NumberOfWeekItem
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.contract.ImportState
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.views.ScheduleWeekChip
import ru.aleshin.studyassistant.schedule.impl.resources.Res
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_add_button
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_cancel_button
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_catalog_existing_label
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_done_button
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_extract_button
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_large_photo_hint
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_note_description
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_note_label
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_note_placeholder
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_processing_hint
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_processing_status_assemble
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_processing_status_clarify
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_processing_status_days
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_processing_status_details
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_processing_status_empty
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_processing_status_free
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_processing_status_group
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_processing_status_intervals
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_processing_status_looking
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_processing_status_rows
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_processing_status_structure
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_processing_status_subjects
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_processing_status_thinking
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_processing_status_times
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_review_description
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_review_title
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_source_description
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_source_title
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_subjects_title
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_success_description
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_success_title
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_teachers_title
import ru.aleshin.studyassistant.schedule.impl.resources.shared_schedule_header
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes

/**
 * @author Stanislav Aleshin on 17.08.2026.
 */
@Composable
internal fun ImportSourceSection(
    modifier: Modifier = Modifier,
    state: ImportState,
    enabled: Boolean,
    horizontalPadding: Dp = 16.dp,
    onSelectPhoto: () -> Unit,
    onTakePhoto: () -> Unit,
    onNoteChanged: (String) -> Unit,
    onOrganizationSelect: (OrganizationShortUi?) -> Unit,
    onAddOrganization: () -> Unit,
    onExtract: () -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = horizontalPadding),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item(key = SOURCE_HEADER_KEY) {
            Column(
                modifier = Modifier.padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(Res.string.schedule_import_source_title),
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = stringResource(Res.string.schedule_import_source_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item(key = PHOTO_HINT_SECTION_KEY) {
            ImportLargePhotoHint()
        }
        item(key = PHOTO_SECTION_KEY) {
            ImportSourceActions(
                enabled = enabled,
                hasPhoto = state.preparedImage != null,
                onSelectPhoto = onSelectPhoto,
                onTakePhoto = onTakePhoto,
            )
        }
        item(key = ORGANIZATION_SECTION_KEY) {
            ImportOrganizationField(
                enabled = enabled,
                selectedOrganization = state.selectedOrganization,
                organizations = state.organizations,
                onAddOrganization = onAddOrganization,
                onSelected = onOrganizationSelect,
            )
        }
        item(key = NOTE_SECTION_KEY) {
            InfoTextField(
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                value = state.note,
                label = stringResource(Res.string.schedule_import_note_label),
                leadingInfoIcon = painterResource(CoreRes.drawable.ic_tooltip),
                onValueChange = { value -> if (value.length <= MAX_NOTE_LENGTH) onNoteChanged(value) },
                placeholder = { Text(stringResource(Res.string.schedule_import_note_placeholder)) },
                supportingText = { Text(stringResource(Res.string.schedule_import_note_description)) },
            )
        }
        item(key = EXTRACT_SECTION_KEY) {
            Button(
                onClick = onExtract,
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                enabled = enabled && state.preparedImage != null && state.selectedOrganization != null,
            ) {
                Text(stringResource(Res.string.schedule_import_extract_button))
            }
        }
    }
}

@Composable
internal fun ImportLoadingSection(
    modifier: Modifier = Modifier,
    startedAt: Long?,
    onCancel: () -> Unit,
) {
    var nowMillis by remember {
        mutableLongStateOf(Clock.System.now().toEpochMilliseconds())
    }
    val elapsedSeconds = startedAt?.let { startMillis -> ((nowMillis - startMillis) / 1_000L).toInt().coerceAtLeast(0) } ?: 0
    val elapsedLabel = remember(elapsedSeconds) {
        val minutes = elapsedSeconds / 60
        val seconds = elapsedSeconds % 60
        "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    }

    LaunchedEffect(startedAt) {
        if (startedAt == null) return@LaunchedEffect
        while (true) {
            nowMillis = Clock.System.now().toEpochMilliseconds()
            delay(1_000.milliseconds)
        }
    }

    val statusResource = remember(elapsedSeconds) {
        importProcessingStatusAt(elapsedSeconds)
    }

    Box(
        modifier = modifier.fillMaxSize().padding(horizontal = 32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.widthIn(max = 360.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            CircularProgressIndicator()
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Crossfade(
                    modifier = Modifier.fillMaxWidth(),
                    targetState = statusResource,
                    label = "import processing status",
                ) { resource ->
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(resource),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 3,
                    )
                }
                Text(
                    text = stringResource(Res.string.schedule_import_processing_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = elapsedLabel,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                )
            }
            TextButton(
                onClick = onCancel,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(text = stringResource(Res.string.schedule_import_cancel_button))
            }
        }
    }
}

@Composable
internal fun ImportReviewSection(
    modifier: Modifier = Modifier,
    state: ImportState,
    horizontalPadding: Dp = 16.dp,
    useSplitCatalogs: Boolean = false,
    onClassClick: (UID) -> Unit,
    onSubjectClick: (UID) -> Unit,
    onTeacherClick: (UID) -> Unit,
    onAddSubject: () -> Unit,
    onAddTeacher: () -> Unit,
    onAddClass: (Int, Int) -> Unit,
    onUpdateStartOfDay: (Int, Int, String) -> Unit,
    onUpdateClassesDuration: (Int, Int, Long, List<Pair<Int, Long>>) -> Unit,
    onUpdateBreaksDuration: (Int, Int, Long, List<Pair<Int, Long>>) -> Unit,
    onSwapDays: (Int, Int, Int) -> Unit,
) {
    val session = state.session ?: return
    val coroutineScope = rememberCoroutineScope()
    val schedulesScrollState = rememberScrollState()
    val usedSubjectIds = remember(session.classes) {
        session.classes.mapNotNull(ScheduleImportClassUi::subjectId).toSet()
    }
    val usedTeacherIds = remember(session.classes) {
        session.classes.mapNotNull(ScheduleImportClassUi::teacherId).toSet()
    }
    val subjects = remember(session.subjects, usedSubjectIds) {
        session.subjects.sortedWith(
            comparator = compareBy<SubjectUi> { subject ->
                if (subject.uid in usedSubjectIds) 0 else 1
            }.thenBy { subject ->
                subject.name.lowercase()
            }
        )
    }
    val teachers = remember(session.employees, usedTeacherIds) {
        session.employees.sortedWith(
            comparator = compareBy<EmployeeUi> { employee ->
                if (employee.uid in usedTeacherIds) 0 else 1
            }.thenBy { employee ->
                employee.fullName().lowercase()
            }
        )
    }
    val maxNumberOfWeek = remember(session.classes) {
        session.classes.maxOfOrNull { classModel -> classModel.repeatWeek } ?: 1
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(key = REVIEW_HEADER_KEY) {
            Column(
                modifier = Modifier.padding(start = horizontalPadding, top = 16.dp, end = horizontalPadding),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(Res.string.schedule_import_review_title),
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = stringResource(Res.string.schedule_import_review_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item(key = BANNER_SECTION_KEY) {
            YandexInlineBanner(
                modifier = Modifier.padding(horizontal = horizontalPadding),
                placement = AdPlacement.AI_IMPORTER,
            )
        }
        item(key = WEEK_SECTION_KEY) {
            ImportWeekSection(
                classes = remember(session.classes) {
                    session.classes.filter(ScheduleImportClassUi::included)
                },
                subjects = session.subjects,
                employees = session.employees,
                maxNumberOfWeek = maxNumberOfWeek,
                horizontalPadding = horizontalPadding,
                onClassClick = onClassClick,
                onAddClass = onAddClass,
                onUpdateStartOfDay = onUpdateStartOfDay,
                onUpdateClassesDuration = onUpdateClassesDuration,
                onUpdateBreaksDuration = onUpdateBreaksDuration,
                onSwapDays = onSwapDays,
                onWeekSelected = {
                    coroutineScope.launch { schedulesScrollState.animateScrollTo(0) }
                },
                schedulesScrollState = schedulesScrollState,
            )
        }
        if (useSplitCatalogs) {
            item(key = CATALOGS_SECTION_KEY) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    ImportSubjectCatalogSection(
                        modifier = Modifier.weight(1f),
                        title = stringResource(Res.string.schedule_import_subjects_title),
                        subjects = subjects,
                        originalSubjectIds = session.originalSubjectIds,
                        onClick = onSubjectClick,
                        onAdd = onAddSubject,
                    )
                    ImportTeacherCatalogSection(
                        modifier = Modifier.weight(1f),
                        title = stringResource(Res.string.schedule_import_teachers_title),
                        employees = teachers,
                        originalEmployeeIds = session.originalEmployeeIds,
                        onClick = onTeacherClick,
                        onAdd = onAddTeacher,
                    )
                }
            }
        } else {
            item(key = SUBJECTS_SECTION_KEY) {
                ImportSubjectCatalogSection(
                    modifier = Modifier.padding(horizontal = horizontalPadding),
                    title = stringResource(Res.string.schedule_import_subjects_title),
                    subjects = subjects,
                    originalSubjectIds = session.originalSubjectIds,
                    onClick = onSubjectClick,
                    onAdd = onAddSubject,
                )
            }
            item(key = TEACHERS_SECTION_KEY) {
                ImportTeacherCatalogSection(
                    modifier = Modifier.padding(horizontal = horizontalPadding),
                    title = stringResource(Res.string.schedule_import_teachers_title),
                    employees = teachers,
                    originalEmployeeIds = session.originalEmployeeIds,
                    onClick = onTeacherClick,
                    onAdd = onAddTeacher,
                )
            }
        }
        item(key = REVIEW_SPACER_KEY) {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
internal fun ImportWeekSection(
    classes: List<ScheduleImportClassUi>,
    subjects: List<SubjectUi>,
    employees: List<EmployeeUi>,
    maxNumberOfWeek: Int,
    horizontalPadding: Dp = 16.dp,
    onClassClick: (UID) -> Unit,
    onAddClass: (Int, Int) -> Unit,
    onUpdateStartOfDay: (Int, Int, String) -> Unit,
    onUpdateClassesDuration: (Int, Int, Long, List<Pair<Int, Long>>) -> Unit,
    onUpdateBreaksDuration: (Int, Int, Long, List<Pair<Int, Long>>) -> Unit,
    onSwapDays: (Int, Int, Int) -> Unit,
    onWeekSelected: () -> Unit,
    schedulesScrollState: ScrollState,
) {
    var selectedWeek by rememberSaveable { mutableIntStateOf(NumberOfWeekItem.ONE.isoWeekNumber) }
    var selectedDay by rememberSaveable { mutableIntStateOf(DayOfWeek.MONDAY.isoDayNumber) }
    var swapDialogOpen by remember { mutableStateOf(false) }
    val weekClasses = remember(classes, selectedWeek) {
        classes.filter { classModel -> classModel.repeatWeek == selectedWeek }
    }
    val selectedDayClasses = remember(weekClasses, selectedDay) {
        weekClasses.filter { classModel -> classModel.dayOfWeek == selectedDay }
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = horizontalPadding),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(Res.string.shared_schedule_header),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.weight(1f))
            ScheduleWeekChip(
                selected = NumberOfWeekItem.valueOf(selectedWeek),
                maxNumberOfWeek = maxNumberOfWeek,
                onSelect = { week ->
                    selectedWeek = week.isoWeekNumber
                    onWeekSelected()
                },
            )
        }
        ImportFastEditBar(
            dayClasses = selectedDayClasses,
            onSwapDaysClick = { swapDialogOpen = true },
            onUpdateStartOfDay = { startTime ->
                onUpdateStartOfDay(selectedWeek, selectedDay, startTime)
            },
            onUpdateClassesDuration = { duration, specificDurations ->
                onUpdateClassesDuration(selectedWeek, selectedDay, duration, specificDurations)
            },
            onUpdateBreaksDuration = { duration, specificDurations ->
                onUpdateBreaksDuration(selectedWeek, selectedDay, duration, specificDurations)
            },
            onAddClass = { onAddClass(selectedDay, selectedWeek) },
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .horizontalScroll(schedulesScrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DayOfWeek.entries.forEach { dayOfWeek ->
                val dayClasses = weekClasses.filter { classModel ->
                    classModel.dayOfWeek == dayOfWeek.isoDayNumber
                }
                ImportDayColumn(
                    dayOfWeek = dayOfWeek,
                    selected = dayOfWeek.isoDayNumber == selectedDay,
                    classes = dayClasses,
                    subjects = subjects,
                    employees = employees,
                    onSelect = { selectedDay = dayOfWeek.isoDayNumber },
                    onClassClick = onClassClick
                )
            }
        }
    }

    if (swapDialogOpen) {
        val secondDay = if (selectedDay == DayOfWeek.MONDAY.isoDayNumber) {
            DayOfWeek.TUESDAY.isoDayNumber
        } else {
            selectedDay - 1
        }
        ImportSwapDaysDialog(
            initialFirstDay = selectedDay,
            initialSecondDay = secondDay,
            onDismiss = { swapDialogOpen = false },
            onConfirm = { firstDay, secondDayOfWeek ->
                onSwapDays(selectedWeek, firstDay, secondDayOfWeek)
                swapDialogOpen = false
            },
        )
    }
}

@Composable
internal fun ImportSubjectCatalogSection(
    modifier: Modifier = Modifier,
    title: String,
    subjects: List<SubjectUi>,
    originalSubjectIds: Set<UID>,
    onClick: (UID) -> Unit,
    onAdd: () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
        )
        subjects.forEach { subject ->
            ImportCatalogItem(
                title = subject.name,
                label = subject.teacher?.fullName(),
                color = subject.color,
                isNew = subject.uid !in originalSubjectIds,
                onClick = { onClick(subject.uid) },
            )
        }
        ImportCatalogAddButton(onClick = onAdd)
    }
}

@Composable
internal fun ImportTeacherCatalogSection(
    modifier: Modifier = Modifier,
    title: String,
    employees: List<EmployeeUi>,
    originalEmployeeIds: Set<UID>,
    onClick: (UID) -> Unit,
    onAdd: () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
        )
        employees.forEach { employee ->
            ImportCatalogItem(
                title = employee.fullName(),
                label = if (employee.uid in originalEmployeeIds) {
                    stringResource(Res.string.schedule_import_catalog_existing_label)
                } else {
                    null
                },
                isNew = employee.uid !in originalEmployeeIds,
                onClick = { onClick(employee.uid) },
            )
        }
        ImportCatalogAddButton(onClick = onAdd)
    }
}

@Composable
private fun ImportCatalogAddButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    OutlinedButton(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
    ) {
        Icon(
            modifier = Modifier.size(18.dp),
            imageVector = Icons.Default.Add,
            contentDescription = null,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(stringResource(Res.string.schedule_import_add_button))
    }
}

@Composable
internal fun ImportSuccessSection(
    modifier: Modifier = Modifier,
    centered: Boolean = true,
    onDone: () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = if (centered) Alignment.CenterHorizontally else Alignment.Start,
        verticalArrangement = if (centered) Arrangement.Center else Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(Res.string.schedule_import_success_title),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = if (centered) TextAlign.Center else TextAlign.Start,
        )
        if (centered) {
            Spacer(modifier = Modifier.height(8.dp))
        }
        Text(
            text = stringResource(Res.string.schedule_import_success_description),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = if (centered) TextAlign.Center else TextAlign.Start,
        )
        if (centered) {
            Spacer(modifier = Modifier.height(24.dp))
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onDone
        ) {
            Text(stringResource(Res.string.schedule_import_done_button))
        }
    }
}

@Composable
private fun ImportLargePhotoHint(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = StudyAssistantRes.colors.accents.yellowContainer,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                modifier = Modifier.size(32.dp),
                imageVector = Icons.Default.WarningAmber,
                contentDescription = null,
                tint = StudyAssistantRes.colors.accents.onYellowContainer,
            )
            Text(
                text = stringResource(Res.string.schedule_import_large_photo_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = StudyAssistantRes.colors.accents.onYellowContainer,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private data class ImportProcessingStatus(
    val startSeconds: Int,
    val text: StringResource,
)

private fun importProcessingStatusAt(elapsedSeconds: Int): StringResource {
    val thinkingStart = IMPORT_PROCESSING_THINKING_START_SECONDS
    if (elapsedSeconds < thinkingStart) {
        return importProcessingStatuses.last { elapsedSeconds >= it.startSeconds }.text
    }
    val thinkingStatuses = importProcessingStatuses.filter { status ->
        status.startSeconds >= thinkingStart
    }
    val step = (elapsedSeconds - thinkingStart) / IMPORT_PROCESSING_STATUS_INTERVAL_SECONDS
    return thinkingStatuses[step % thinkingStatuses.size].text
}

private val importProcessingStatuses = listOf(
    ImportProcessingStatus(0, Res.string.schedule_import_processing_status_looking),
    ImportProcessingStatus(12, Res.string.schedule_import_processing_status_structure),
    ImportProcessingStatus(24, Res.string.schedule_import_processing_status_times),
    ImportProcessingStatus(36, Res.string.schedule_import_processing_status_group),
    ImportProcessingStatus(48, Res.string.schedule_import_processing_status_subjects),
    ImportProcessingStatus(60, Res.string.schedule_import_processing_status_days),
    ImportProcessingStatus(72, Res.string.schedule_import_processing_status_empty),
    ImportProcessingStatus(84, Res.string.schedule_import_processing_status_rows),
    ImportProcessingStatus(96, Res.string.schedule_import_processing_status_clarify),
    ImportProcessingStatus(108, Res.string.schedule_import_processing_status_free),
    ImportProcessingStatus(120, Res.string.schedule_import_processing_status_intervals),
    ImportProcessingStatus(132, Res.string.schedule_import_processing_status_details),
    ImportProcessingStatus(144, Res.string.schedule_import_processing_status_thinking),
    ImportProcessingStatus(156, Res.string.schedule_import_processing_status_assemble),
)

private const val IMPORT_PROCESSING_STATUS_INTERVAL_SECONDS = 12
private const val IMPORT_PROCESSING_THINKING_START_SECONDS = 96

internal const val SOURCE_HEADER_KEY = "import_source_header"
internal const val PHOTO_HINT_SECTION_KEY = "import_photo_hint_section"
internal const val PHOTO_SECTION_KEY = "import_photo_section"
internal const val ORGANIZATION_SECTION_KEY = "import_organization_section"
internal const val NOTE_SECTION_KEY = "import_note_section"
internal const val EXTRACT_SECTION_KEY = "import_extract_section"
internal const val REVIEW_HEADER_KEY = "import_review_header"
internal const val BANNER_SECTION_KEY = "import_banner_section"
internal const val WEEK_SECTION_KEY = "import_week_section"
internal const val SUBJECTS_SECTION_KEY = "import_subjects_section"
internal const val TEACHERS_SECTION_KEY = "import_teachers_section"
internal const val CATALOGS_SECTION_KEY = "import_catalogs_section"
internal const val REVIEW_SPACER_KEY = "import_review_spacer"
internal const val MAX_NOTE_LENGTH = 120
