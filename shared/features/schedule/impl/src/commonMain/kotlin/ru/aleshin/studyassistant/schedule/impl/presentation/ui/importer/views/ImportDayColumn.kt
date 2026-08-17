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
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.compose.dnd.reorder.ReorderContainer
import com.mohamedrejeb.compose.dnd.reorder.ReorderableItem
import com.mohamedrejeb.compose.dnd.reorder.rememberReorderState
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.common.extensions.handleLazyListScroll
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.presentation.models.subjects.SubjectUi
import ru.aleshin.studyassistant.core.presentation.models.users.EmployeeUi
import ru.aleshin.studyassistant.core.ui.mappers.mapToSting
import ru.aleshin.studyassistant.schedule.impl.presentation.models.importing.ScheduleImportClassUi
import ru.aleshin.studyassistant.schedule.impl.resources.Res
import ru.aleshin.studyassistant.schedule.impl.resources.empty_classes_title

/**
 * @author Stanislav Aleshin on 16.08.2026.
 */
@Composable
internal fun ImportDayColumn(
    modifier: Modifier = Modifier,
    dayOfWeek: DayOfWeek,
    classes: List<ScheduleImportClassUi>,
    subjects: List<SubjectUi>,
    employees: List<EmployeeUi>,
    onClassClick: (UID) -> Unit,
    onReorderClasses: (List<UID>) -> Unit,
) {
    Surface(
        modifier = modifier.size(176.dp, 320.dp),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainer,
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    text = dayOfWeek.mapToSting(),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleSmall,
                )
            }
            if (classes.isEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
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
                ImportDayReorderList(
                    classes = classes,
                    subjects = subjects,
                    employees = employees,
                    onClassClick = onClassClick,
                    onReorderClasses = onReorderClasses,
                )
            }
        }
    }
}

@Composable
private fun ImportDayReorderList(
    classes: List<ScheduleImportClassUi>,
    subjects: List<SubjectUi>,
    employees: List<EmployeeUi>,
    onClassClick: (UID) -> Unit,
    onReorderClasses: (List<UID>) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val reorderState = rememberReorderState<ScheduleImportClassUi>()
    var classItems by remember { mutableStateOf(classes) }
    val lazyListState = rememberLazyListState()

    LaunchedEffect(classes) {
        if (reorderState.draggedItem == null) classItems = classes
    }

    ReorderContainer(state = reorderState) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            state = lazyListState,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            items(
                items = classItems,
                key = { classModel -> classModel.uid },
            ) { classModel ->
                ReorderableItem(
                    state = reorderState,
                    key = classModel.uid,
                    data = classModel,
                    requireFirstDownUnconsumed = true,
                    onDrop = { onReorderClasses(classItems.map(ScheduleImportClassUi::uid)) },
                    onDragEnter = { state ->
                        classItems = classItems.toMutableList().apply {
                            val index = indexOfFirst { item -> item.uid == classModel.uid }
                            if (index == -1) return@ReorderableItem
                            removeAll { item -> item.uid == state.data.uid }
                            add(index, state.data)
                            scope.launch {
                                handleLazyListScroll(
                                    lazyListState = lazyListState,
                                    dropIndex = index,
                                )
                            }
                        }
                    },
                    modifier = Modifier.animateItem(),
                ) {
                    ImportClassCard(
                        classModel = classModel,
                        subject = subjects.firstOrNull { subject -> subject.uid == classModel.subjectId },
                        teacher = employees.firstOrNull { employee -> employee.uid == classModel.teacherId },
                        onClick = { onClassClick(classModel.uid) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ImportClassCard(
    modifier: Modifier = Modifier,
    classModel: ScheduleImportClassUi,
    subject: SubjectUi?,
    teacher: EmployeeUi?,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = subject?.color?.let { Color(it).copy(alpha = 0.16f) }
            ?: MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = listOfNotNull(
                    classModel.number?.toString(),
                    classModel.startTime.takeIf(String::isNotBlank),
                ).joinToString(" · "),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = subject?.name.orEmpty(),
                style = MaterialTheme.typography.labelLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (teacher != null) {
                Text(
                    text = teacher.officialName(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
