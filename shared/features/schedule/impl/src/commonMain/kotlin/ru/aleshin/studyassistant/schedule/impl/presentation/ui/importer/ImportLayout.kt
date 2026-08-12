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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.schedule.impl.presentation.models.importing.ScheduleImportDraftUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.importing.ScheduleImportEntryUi
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.contract.ImportState
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.views.ImportEntryCard
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.views.ImportSourceActions
import ru.aleshin.studyassistant.schedule.impl.resources.Res
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_apply_button
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_done_button
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_edit_source_button
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_extract_button
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_review_description
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_review_title
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_selected_count
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_source_description
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_source_title
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_success_description
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_success_title
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_text_label
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_text_placeholder
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_unparsed_title
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_week_value
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_weeks_label

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
@Composable
internal fun ImportLayout(
    state: ImportState,
    onSourceTextChanged: (String) -> Unit,
    onNumberOfWeeksChanged: (Int) -> Unit,
    onSelectPhoto: () -> Unit,
    onTakePhoto: () -> Unit,
    onExtract: () -> Unit,
    onToggleEntry: (Int) -> Unit,
    onUpdateEntry: (ScheduleImportEntryUi) -> Unit,
    onApply: () -> Unit,
    onEditSource: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter,
        ) {
            when {
                state.isApplied -> ImportSuccess(
                    onDone = onDone,
                    modifier = Modifier.widthIn(max = 720.dp),
                )
                state.draft != null -> ImportReview(
                    draft = state.draft,
                    enabled = !state.isLoading,
                    onToggleEntry = onToggleEntry,
                    onUpdateEntry = onUpdateEntry,
                    onApply = onApply,
                    onEditSource = onEditSource,
                    modifier = Modifier.widthIn(max = 840.dp),
                )
                else -> ImportSource(
                    sourceText = state.sourceText,
                    numberOfWeeks = state.numberOfWeeks,
                    enabled = !state.isLoading,
                    onSourceTextChanged = onSourceTextChanged,
                    onNumberOfWeeksChanged = onNumberOfWeeksChanged,
                    onSelectPhoto = onSelectPhoto,
                    onTakePhoto = onTakePhoto,
                    onExtract = onExtract,
                    modifier = Modifier.widthIn(max = 720.dp),
                )
            }
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
private fun ImportSource(
    sourceText: String,
    numberOfWeeks: Int,
    enabled: Boolean,
    onSourceTextChanged: (String) -> Unit,
    onNumberOfWeeksChanged: (Int) -> Unit,
    onSelectPhoto: () -> Unit,
    onTakePhoto: () -> Unit,
    onExtract: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                text = stringResource(Res.string.schedule_import_source_title),
                style = MaterialTheme.typography.headlineSmall,
            )
        }
        item {
            Text(
                text = stringResource(Res.string.schedule_import_source_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            ImportSourceActions(
                enabled = enabled,
                onSelectPhoto = onSelectPhoto,
                onTakePhoto = onTakePhoto,
            )
        }
        item {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = sourceText,
                enabled = enabled,
                onValueChange = { value ->
                    if (value.length <= MAX_SOURCE_TEXT_LENGTH) onSourceTextChanged(value)
                },
                shape = MaterialTheme.shapes.large,
                label = { Text(stringResource(Res.string.schedule_import_text_label)) },
                placeholder = { Text(stringResource(Res.string.schedule_import_text_placeholder)) },
                minLines = 10,
            )
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(Res.string.schedule_import_weeks_label),
                    style = MaterialTheme.typography.labelLarge,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..3).forEach { week ->
                        FilterChip(
                            selected = numberOfWeeks == week,
                            enabled = enabled,
                            onClick = { onNumberOfWeeksChanged(week) },
                            label = {
                                Text(stringResource(Res.string.schedule_import_week_value, week))
                            },
                        )
                    }
                }
            }
        }
        item {
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled && sourceText.trim().length >= 3,
                onClick = onExtract,
            ) {
                Text(stringResource(Res.string.schedule_import_extract_button))
            }
        }
    }
}

@Composable
private fun ImportReview(
    draft: ScheduleImportDraftUi,
    enabled: Boolean,
    onToggleEntry: (Int) -> Unit,
    onUpdateEntry: (ScheduleImportEntryUi) -> Unit,
    onApply: () -> Unit,
    onEditSource: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedCount = draft.entries.count(ScheduleImportEntryUi::included)
    LazyColumn(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = stringResource(Res.string.schedule_import_review_title),
                style = MaterialTheme.typography.headlineSmall,
            )
        }
        item {
            Text(
                text = stringResource(Res.string.schedule_import_review_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            Text(
                text = stringResource(
                    Res.string.schedule_import_selected_count,
                    selectedCount,
                    draft.entries.size,
                ),
                style = MaterialTheme.typography.titleSmall,
            )
        }
        if (draft.unparsedLines.isNotEmpty()) {
            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
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
        items(
            items = draft.entries,
            key = ScheduleImportEntryUi::id,
        ) { entry ->
            ImportEntryCard(
                entry = entry,
                enabled = enabled,
                onToggle = { onToggleEntry(entry.id) },
                onUpdate = onUpdateEntry,
            )
        }
        item {
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                onClick = onEditSource,
            ) {
                Text(stringResource(Res.string.schedule_import_edit_source_button))
            }
        }
        item {
            Button(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                enabled = enabled && selectedCount > 0,
                onClick = onApply,
            ) {
                Text(stringResource(Res.string.schedule_import_apply_button))
            }
        }
    }
}

@Composable
private fun ImportSuccess(
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(Res.string.schedule_import_success_title),
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = stringResource(Res.string.schedule_import_success_description),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onDone,
        ) {
            Text(stringResource(Res.string.schedule_import_done_button))
        }
    }
}

private const val MAX_SOURCE_TEXT_LENGTH = 30_000
