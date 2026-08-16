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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DayOfWeek
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.ui.mappers.mapToSting
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
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
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        AnimatedContent(
            targetState = when {
                state.isApplied -> ImportContentState.SUCCESS
                state.draft != null -> ImportContentState.REVIEW
                else -> ImportContentState.SOURCE
            },
            transitionSpec = { fadeIn().togetherWith(fadeOut()) },
        ) { contentState ->
            when (contentState) {
                ImportContentState.SOURCE -> ImportSource(
                    sourceText = state.sourceText,
                    numberOfWeeks = state.numberOfWeeks,
                    enabled = !state.isLoading,
                    onSourceTextChanged = onSourceTextChanged,
                    onNumberOfWeeksChanged = onNumberOfWeeksChanged,
                    onSelectPhoto = onSelectPhoto,
                    onTakePhoto = onTakePhoto,
                    onExtract = onExtract,
                    modifier = Modifier.widthIn(max = 600.dp),
                )
                ImportContentState.REVIEW -> ImportReview(
                    state = state,
                    enabled = !state.isLoading,
                    onToggleEntry = onToggleEntry,
                    onUpdateEntry = onUpdateEntry,
                    onApply = onApply,
                    onEditSource = onEditSource,
                    modifier = Modifier.widthIn(max = 800.dp),
                )
                ImportContentState.SUCCESS -> ImportSuccess(
                    onDone = onDone,
                    modifier = Modifier.widthIn(max = 600.dp),
                )
            }
        }
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
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
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item(key = "header") {
            Column(
                modifier = Modifier.padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(Res.string.schedule_import_source_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(Res.string.schedule_import_source_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item(key = "actions") {
            ImportSourceActions(
                enabled = enabled,
                onSelectPhoto = onSelectPhoto,
                onTakePhoto = onTakePhoto,
            )
        }
        item(key = "input") {
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
        item(key = "weeks") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(Res.string.schedule_import_weeks_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
        item(key = "extract") {
            Button(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                enabled = enabled && sourceText.trim().length >= 3,
                onClick = onExtract,
            ) {
                Text(stringResource(Res.string.schedule_import_extract_button))
            }
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun ImportReview(
    state: ImportState,
    enabled: Boolean,
    onToggleEntry: (Int) -> Unit,
    onUpdateEntry: (ScheduleImportEntryUi) -> Unit,
    onApply: () -> Unit,
    onEditSource: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val draft = state.draft!!
    val selectedCount = draft.entries.count(ScheduleImportEntryUi::included)
    val groupedEntries = remember(draft.entries) {
        draft.entries.groupBy { it.repeatWeek to it.dayOfWeek }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        state = rememberLazyListState(),
    ) {
        item(key = "review_header") {
            Column(
                modifier = Modifier.padding(top = 16.dp),
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
                Text(
                    text = stringResource(
                        Res.string.schedule_import_selected_count,
                        selectedCount,
                        draft.entries.size,
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = StudyAssistantRes.colors.accents.orange,
                )
            }
        }
        if (draft.unparsedLines.isNotEmpty()) {
            item(key = "unparsed") {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
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

        groupedEntries.forEach { (weekDay, entries) ->
            stickyHeader(key = "header_${weekDay.first}_${weekDay.second}") {
                val dayName = DayOfWeek.entries[weekDay.second - 1].mapToSting()
                val weekName = stringResource(Res.string.schedule_import_week_value, weekDay.first)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "$dayName ($weekName)",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            items(
                items = entries,
                key = { it.id },
            ) { entry ->
                ImportEntryCard(
                    entry = entry,
                    enabled = enabled,
                    organizations = state.organizations,
                    subjects = state.subjects,
                    employees = state.employees,
                    onToggle = { onToggleEntry(entry.id) },
                    onUpdate = onUpdateEntry,
                )
            }
        }

        item(key = "review_actions") {
            Column(
                modifier = Modifier.padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = enabled,
                    onClick = onEditSource,
                ) {
                    Text(stringResource(Res.string.schedule_import_edit_source_button))
                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = enabled && selectedCount > 0,
                    onClick = onApply,
                ) {
                    Text(stringResource(Res.string.schedule_import_apply_button))
                }
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
    SOURCE, REVIEW, SUCCESS
}

private const val MAX_SOURCE_TEXT_LENGTH = 30_000
