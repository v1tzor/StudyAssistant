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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import kotlinx.datetime.DayOfWeek
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.ui.mappers.mapToSting
import ru.aleshin.studyassistant.schedule.impl.presentation.models.importing.ScheduleImportEntryUi
import ru.aleshin.studyassistant.schedule.impl.resources.Res
import ru.aleshin.studyassistant.schedule.impl.resources.empty_classes_title
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_move_to_day
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_swap_class

/**
 * @author Stanislav Aleshin on 16.08.2026.
 */
@Composable
internal fun ImportDayColumn(
    modifier: Modifier = Modifier,
    dayOfWeek: DayOfWeek,
    entries: List<ScheduleImportEntryUi>,
    onClassClick: (Int) -> Unit,
    onMoveClass: (Int, Int) -> Unit,
    onSwapClasses: (Int, Int) -> Unit,
) {
    Surface(
        modifier = modifier.size(170.dp, 300.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceContainer,
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                    text = dayOfWeek.mapToSting(),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            if (entries.isEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(Res.string.empty_classes_title),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(
                        items = entries,
                        key = { entry -> entry.id },
                    ) { entry ->
                        ImportClassCard(
                            entry = entry,
                            siblings = entries,
                            onClick = { onClassClick(entry.id) },
                            onMoveClass = onMoveClass,
                            onSwapClasses = onSwapClasses,
                        )
                    }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun ImportClassCard(
    modifier: Modifier = Modifier,
    entry: ScheduleImportEntryUi,
    siblings: List<ScheduleImportEntryUi>,
    onClick: () -> Unit,
    onMoveClass: (Int, Int) -> Unit,
    onSwapClasses: (Int, Int) -> Unit,
) {
    var isMenuOpen by remember { mutableStateOf(false) }

    Box {
        Surface(
            modifier = modifier.fillMaxWidth().combinedClickable(
                onClick = onClick,
                onLongClick = { isMenuOpen = true },
            ),
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = listOfNotNull(
                        entry.classNumber?.toString(),
                        entry.startTime.takeIf(String::isNotBlank),
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = entry.subject,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        DropdownMenu(
            expanded = isMenuOpen,
            onDismissRequest = { isMenuOpen = false },
        ) {
            DayOfWeek.entries.forEach { day ->
                if (day.ordinal + 1 != entry.dayOfWeek) {
                    DropdownMenuItem(
                        text = {
                            Text("${stringResource(Res.string.schedule_import_move_to_day)}: ${day.mapToSting()}")
                        },
                        onClick = {
                            onMoveClass(entry.id, day.ordinal + 1)
                            isMenuOpen = false
                        },
                    )
                }
            }
            siblings.filter { sibling -> sibling.id != entry.id }.forEach { sibling ->
                DropdownMenuItem(
                    text = {
                        Text("${stringResource(Res.string.schedule_import_swap_class)}: ${sibling.subject}")
                    },
                    onClick = {
                        onSwapClasses(entry.id, sibling.id)
                        isMenuOpen = false
                    },
                )
            }
        }
    }
}
