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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.isoDayNumber
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.presentation.models.organizations.OrganizationShortUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.importing.ScheduleImportEntryUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.schedule.NumberOfWeekItem
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.contract.ImportState
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.views.ImportCatalogItem
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.views.ImportDayColumn
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.views.ImportOrganizationField
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.views.ImportSourceActions
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.views.ScheduleWeekChip
import ru.aleshin.studyassistant.schedule.impl.resources.Res
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_done_button
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_extract_button
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_note_description
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_note_label
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_note_placeholder
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_processing_hint
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_processing_title
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_review_description
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_review_title
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_source_description
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_source_title
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_subjects_title
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_success_description
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_success_title
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_teachers_title
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_unparsed_title
import ru.aleshin.studyassistant.schedule.impl.resources.shared_schedule_header

/**
 * @author Stanislav Aleshin on 16.08.2026.
 */
@Composable
internal fun ImportLayout(
    modifier: Modifier = Modifier,
    state: ImportState,
    onSelectPhoto: () -> Unit,
    onTakePhoto: () -> Unit,
    onNoteChanged: (String) -> Unit,
    onOrganizationSelect: (OrganizationShortUi?) -> Unit,
    onAddOrganization: () -> Unit,
    onExtract: () -> Unit,
    onClassClick: (Int) -> Unit,
    onMoveClass: (Int, Int) -> Unit,
    onSwapClasses: (Int, Int) -> Unit,
    onSubjectClick: (String) -> Unit,
    onTeacherClick: (String) -> Unit,
    onDone: () -> Unit,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        AnimatedContent(
            targetState = when {
                state.isLoading -> ImportContentState.LOADING
                state.isApplied -> ImportContentState.SUCCESS
                state.draft != null -> ImportContentState.REVIEW
                else -> ImportContentState.SOURCE
            },
            transitionSpec = { fadeIn().togetherWith(fadeOut()) },
            contentKey = { contentState -> contentState },
        ) { contentState ->
            when (contentState) {
                ImportContentState.SOURCE -> ImportSourceLayout(
                    modifier = Modifier.widthIn(max = 600.dp),
                    state = state,
                    enabled = !state.isLoading,
                    onSelectPhoto = onSelectPhoto,
                    onTakePhoto = onTakePhoto,
                    onNoteChanged = onNoteChanged,
                    onOrganizationSelect = onOrganizationSelect,
                    onAddOrganization = onAddOrganization,
                    onExtract = onExtract,
                )
                ImportContentState.LOADING -> ImportLoadingLayout(
                    modifier = Modifier.fillMaxSize(),
                )
                ImportContentState.REVIEW -> ImportReviewLayout(
                    modifier = Modifier.widthIn(max = 800.dp),
                    state = state,
                    onClassClick = onClassClick,
                    onMoveClass = onMoveClass,
                    onSwapClasses = onSwapClasses,
                    onSubjectClick = onSubjectClick,
                    onTeacherClick = onTeacherClick,
                )
                ImportContentState.SUCCESS -> ImportSuccessLayout(
                    modifier = Modifier.widthIn(max = 600.dp),
                    onDone = onDone,
                )
            }
        }
    }
}

@Composable
private fun ImportSourceLayout(
    modifier: Modifier = Modifier,
    state: ImportState,
    enabled: Boolean,
    onSelectPhoto: () -> Unit,
    onTakePhoto: () -> Unit,
    onNoteChanged: (String) -> Unit,
    onOrganizationSelect: (OrganizationShortUi?) -> Unit,
    onAddOrganization: () -> Unit,
    onExtract: () -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
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
        item(key = PHOTO_SECTION_KEY) {
            ImportSourceActions(
                enabled = enabled,
                hasPhoto = state.hasPhoto,
                onSelectPhoto = onSelectPhoto,
                onTakePhoto = onTakePhoto,
            )
        }
        item(key = ORGANIZATION_SECTION_KEY) {
            ImportOrganizationField(
                enabled = enabled,
                organization = state.organization,
                organizations = state.organizations,
                onAddOrganization = onAddOrganization,
                onSelected = onOrganizationSelect,
            )
        }
        item(key = NOTE_SECTION_KEY) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.note,
                    enabled = enabled,
                    onValueChange = { value ->
                        if (value.length <= MAX_NOTE_LENGTH) onNoteChanged(value)
                    },
                    label = { Text(stringResource(Res.string.schedule_import_note_label)) },
                    placeholder = { Text(stringResource(Res.string.schedule_import_note_placeholder)) },
                    supportingText = {
                        Text(stringResource(Res.string.schedule_import_note_description))
                    },
                    shape = MaterialTheme.shapes.large,
                )
            }
        }
        item(key = EXTRACT_SECTION_KEY) {
            Button(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                enabled = enabled && state.hasPhoto && state.organization != null,
                onClick = onExtract,
            ) {
                Text(stringResource(Res.string.schedule_import_extract_button))
            }
        }
    }
}

@Composable
private fun ImportLoadingLayout(
    modifier: Modifier = Modifier,
) {
    var elapsedSeconds by remember { mutableIntStateOf(0) }
    val elapsedLabel = remember(elapsedSeconds) {
        val minutes = elapsedSeconds / 60
        val seconds = elapsedSeconds % 60
        "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1_000)
            elapsedSeconds += 1
        }
    }

    Box(
        modifier = modifier.fillMaxSize().padding(horizontal = 32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.widthIn(max = 360.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            CircularProgressIndicator()
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(Res.string.schedule_import_processing_title),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                )
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
        }
    }
}

@Composable
private fun ImportReviewLayout(
    modifier: Modifier = Modifier,
    state: ImportState,
    onClassClick: (Int) -> Unit,
    onMoveClass: (Int, Int) -> Unit,
    onSwapClasses: (Int, Int) -> Unit,
    onSubjectClick: (String) -> Unit,
    onTeacherClick: (String) -> Unit,
) {
    val draft = state.draft ?: return
    val coroutineScope = rememberCoroutineScope()
    val schedulesRowState = rememberLazyListState()
    val subjects = remember(draft.entries) {
        draft.entries
            .filter(ScheduleImportEntryUi::included)
            .mapNotNull { entry -> entry.subject.takeIf(String::isNotBlank) }
            .distinct()
    }
    val teachers = remember(draft.entries) {
        draft.entries
            .filter(ScheduleImportEntryUi::included)
            .mapNotNull { entry -> entry.teacher.takeIf(String::isNotBlank) }
            .distinct()
    }
    val maxNumberOfWeek = draft.entries.maxOfOrNull { entry -> entry.repeatWeek } ?: 1

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(key = REVIEW_HEADER_KEY) {
            Column(
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
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
        if (draft.unparsedLines.isNotEmpty()) {
            item(key = UNPARSED_SECTION_KEY) {
                ElevatedCard(
                    modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = stringResource(Res.string.schedule_import_unparsed_title),
                            style = MaterialTheme.typography.titleSmall,
                        )
                        draft.unparsedLines.forEach { line ->
                            Text(
                                text = line,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
        item(key = WEEK_SECTION_KEY) {
            ImportWeekSection(
                entries = draft.entries.filter(ScheduleImportEntryUi::included),
                maxNumberOfWeek = maxNumberOfWeek,
                onClassClick = onClassClick,
                onMoveClass = onMoveClass,
                onSwapClasses = onSwapClasses,
                onWeekSelected = {
                    coroutineScope.launch { schedulesRowState.animateScrollToItem(0) }
                },
                schedulesRowState = schedulesRowState,
            )
        }
        item(key = SUBJECTS_SECTION_KEY) {
            ImportCatalogSection(
                title = stringResource(Res.string.schedule_import_subjects_title),
                items = subjects,
                isNew = { name ->
                    state.subjects.none { subject -> subject.name.equals(name, ignoreCase = true) }
                },
                onClick = onSubjectClick,
            )
        }
        item(key = TEACHERS_SECTION_KEY) {
            ImportCatalogSection(
                title = stringResource(Res.string.schedule_import_teachers_title),
                items = teachers,
                isNew = { name ->
                    state.employees.none { employee ->
                        listOfNotNull(employee.secondName, employee.firstName, employee.patronymic)
                            .joinToString(" ")
                            .equals(name, ignoreCase = true)
                    }
                },
                onClick = onTeacherClick,
            )
        }
        item(key = REVIEW_SPACER_KEY) {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ImportWeekSection(
    entries: List<ScheduleImportEntryUi>,
    maxNumberOfWeek: Int,
    onClassClick: (Int) -> Unit,
    onMoveClass: (Int, Int) -> Unit,
    onSwapClasses: (Int, Int) -> Unit,
    onWeekSelected: () -> Unit,
    schedulesRowState: androidx.compose.foundation.lazy.LazyListState,
) {
    var selectedWeek by rememberSaveable { mutableIntStateOf(NumberOfWeekItem.ONE.isoWeekNumber) }
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
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
        LazyRow(
            modifier = Modifier.fillMaxWidth().height(300.dp),
            state = schedulesRowState,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(
                items = DayOfWeek.entries,
                key = { dayOfWeek -> dayOfWeek.isoDayNumber },
            ) { dayOfWeek ->
                val dayEntries = entries.filter { entry ->
                    entry.dayOfWeek == dayOfWeek.isoDayNumber && entry.repeatWeek == selectedWeek
                }
                ImportDayColumn(
                    dayOfWeek = dayOfWeek,
                    entries = dayEntries,
                    onClassClick = onClassClick,
                    onMoveClass = onMoveClass,
                    onSwapClasses = onSwapClasses,
                )
            }
        }
    }
}

@Composable
private fun ImportCatalogSection(
    title: String,
    items: List<String>,
    isNew: (String) -> Boolean,
    onClick: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
        )
        items.forEach { item ->
            ImportCatalogItem(
                title = item,
                isNew = isNew(item),
                onClick = { onClick(item) },
            )
        }
    }
}

@Composable
private fun ImportSuccessLayout(
    modifier: Modifier = Modifier,
    onDone: () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(Res.string.schedule_import_success_title),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(Res.string.schedule_import_success_description),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onDone,
        ) {
            Text(stringResource(Res.string.schedule_import_done_button))
        }
    }
}

private enum class ImportContentState {
    SOURCE, LOADING, REVIEW, SUCCESS
}

private const val SOURCE_HEADER_KEY = "import_source_header"
private const val PHOTO_SECTION_KEY = "import_photo_section"
private const val ORGANIZATION_SECTION_KEY = "import_organization_section"
private const val NOTE_SECTION_KEY = "import_note_section"
private const val EXTRACT_SECTION_KEY = "import_extract_section"
private const val REVIEW_HEADER_KEY = "import_review_header"
private const val UNPARSED_SECTION_KEY = "import_unparsed_section"
private const val WEEK_SECTION_KEY = "import_week_section"
private const val SUBJECTS_SECTION_KEY = "import_subjects_section"
private const val TEACHERS_SECTION_KEY = "import_teachers_section"
private const val REVIEW_SPACER_KEY = "import_review_spacer"
private const val MAX_NOTE_LENGTH = 120
