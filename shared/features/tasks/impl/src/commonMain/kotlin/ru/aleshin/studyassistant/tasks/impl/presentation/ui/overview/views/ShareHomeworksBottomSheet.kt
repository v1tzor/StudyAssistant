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

package ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import kotlinx.datetime.format.DateTimeComponents
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.common.extensions.formatByTimeZone
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.presentation.models.subjects.SubjectUi
import ru.aleshin.studyassistant.core.presentation.models.tasks.HomeworkTaskComponentUi
import ru.aleshin.studyassistant.core.ui.views.sheet.MediumDragHandle
import ru.aleshin.studyassistant.core.ui.views.shortWeekdayDayMonthFormat
import ru.aleshin.studyassistant.tasks.impl.presentation.models.share.HomeworkShareSelectionUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.convertToMediated
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.fetchAllTasks
import ru.aleshin.studyassistant.tasks.impl.resources.Res
import ru.aleshin.studyassistant.tasks.impl.resources.create_share_code_title
import ru.aleshin.studyassistant.tasks.impl.resources.selection_subject_step_header
import ru.aleshin.studyassistant.tasks.impl.resources.selection_subject_step_label
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.ic_book_study as core_ic_book_study
import ru.aleshin.studyassistant.core.ui.resources.ic_presentation as core_ic_presentation
import ru.aleshin.studyassistant.core.ui.resources.ic_tasks_circular as core_ic_tasks_circular
import ru.aleshin.studyassistant.core.ui.resources.none_title as core_none_title

/**
 * @author Stanislav Aleshin on 24.07.2024.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun ShareHomeworksBottomSheet(
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    targetDate: Instant,
    homeworks: List<HomeworkDetailsUi>,
    onDismissRequest: () -> Unit,
    onConfirm: (HomeworkShareSelectionUi) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier,
        dragHandle = { MediumDragHandle() },
        contentWindowInsets = { WindowInsets.navigationBars },
    ) {
        Column(
            modifier = Modifier.padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            var selectedHomeworks by remember(homeworks) {
                mutableStateOf(homeworks.take(MAX_SHARED_HOMEWORKS))
            }
            ShareHomeworksSheetSubjectsStep(
                selectedHomeworks = selectedHomeworks,
                allHomeworks = homeworks,
                onSelectedHomework = { homework ->
                    if (selectedHomeworks.size < MAX_SHARED_HOMEWORKS) {
                        selectedHomeworks = selectedHomeworks + homework
                    }
                },
                onUnselectedHomework = { homework ->
                    selectedHomeworks = selectedHomeworks - homework
                },
            )
            Button(
                enabled = selectedHomeworks.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                onClick = {
                    onConfirm(
                        HomeworkShareSelectionUi(
                            date = targetDate.startThisDay(),
                            homeworks = selectedHomeworks.map { it.convertToMediated() },
                        )
                    )
                },
                content = { Text(text = stringResource(Res.string.create_share_code_title)) },
            )
        }
    }
}

@Composable
private fun ShareHomeworksSheetSubjectsStep(
    modifier: Modifier = Modifier,
    selectedHomeworks: List<HomeworkDetailsUi>,
    allHomeworks: List<HomeworkDetailsUi>,
    onSelectedHomework: (HomeworkDetailsUi) -> Unit,
    onUnselectedHomework: (HomeworkDetailsUi) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = stringResource(Res.string.selection_subject_step_header),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(Res.string.selection_subject_step_label),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        LazyColumn(
            modifier = Modifier.fillMaxWidth().height(350.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(allHomeworks, key = { it.uid }) { homework ->
                ShareHomeworkView(
                    enabled = selectedHomeworks.contains(homework) ||
                        selectedHomeworks.size < MAX_SHARED_HOMEWORKS,
                    checked = selectedHomeworks.contains(homework),
                    deadline = homework.deadline,
                    subject = homework.subject,
                    theoreticalTasks = homework.theoreticalTasks.components,
                    practicalTasks = homework.practicalTasks.components,
                    presentationTasks = homework.presentationTasks.components,
                    onCheckedChange = { isAdd ->
                        if (isAdd) onSelectedHomework(homework) else onUnselectedHomework(homework)
                    }
                )
            }
        }
    }
}

private const val MAX_SHARED_HOMEWORKS = 20

@Composable
private fun ShareHomeworkView(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    checked: Boolean,
    deadline: Instant,
    subject: SubjectUi?,
    theoreticalTasks: List<HomeworkTaskComponentUi>,
    practicalTasks: List<HomeworkTaskComponentUi>,
    presentationTasks: List<HomeworkTaskComponentUi>,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth().height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Surface(
            modifier = Modifier.fillMaxHeight().width(4.dp),
            shape = MaterialTheme.shapes.small,
            color = subject?.color?.let { Color(it) } ?: MaterialTheme.colorScheme.outline,
            content = { Box(modifier = Modifier.fillMaxHeight()) }
        )
        ShareHomeworkViewContent(
            modifier = Modifier.weight(1f),
            deadline = deadline,
            subject = subject?.name,
            theoreticalTasks = theoreticalTasks,
            practicalTasks = practicalTasks,
            presentationTasks = presentationTasks,
        )
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.size(40.dp),
            enabled = enabled,
        )
    }
}

@Composable
private fun ShareHomeworkViewContent(
    modifier: Modifier = Modifier,
    deadline: Instant,
    subject: String?,
    theoreticalTasks: List<HomeworkTaskComponentUi>,
    practicalTasks: List<HomeworkTaskComponentUi>,
    presentationTasks: List<HomeworkTaskComponentUi>,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Column {
            Text(
                text = deadline.formatByTimeZone(
                    format = DateTimeComponents.Formats.shortWeekdayDayMonthFormat(),
                ),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.labelSmall,
            )
            Text(
                text = subject ?: stringResource(CoreRes.string.core_none_title),
                color = MaterialTheme.colorScheme.onSurface,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                style = MaterialTheme.typography.titleSmall,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            ShareHomeworkTaskCountView(
                painter = painterResource(CoreRes.drawable.core_ic_book_study),
                count = theoreticalTasks.fetchAllTasks().size,
            )
            ShareHomeworkTaskCountView(
                painter = painterResource(CoreRes.drawable.core_ic_tasks_circular),
                count = practicalTasks.fetchAllTasks().size,
            )
            ShareHomeworkTaskCountView(
                painter = painterResource(CoreRes.drawable.core_ic_presentation),
                count = presentationTasks.fetchAllTasks().size,
            )
        }
    }
}

@Composable
private fun ShareHomeworkTaskCountView(
    modifier: Modifier = Modifier,
    painter: Painter,
    count: Int,
    description: String? = null,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier.size(18.dp),
            painter = painter,
            contentDescription = description,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = count.toString(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}
