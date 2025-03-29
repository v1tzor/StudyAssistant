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

package ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.DailyHomeworksUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkErrorsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.theme.TasksThemeRes

/**
 * @author Stanislav Aleshin on 26.03.2025.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun HomeworksExecutionAnalysisView(
    modifier: Modifier = Modifier,
    isLoadingHomeworks: Boolean,
    isLoadingErrors: Boolean,
    currentDate: Instant,
    dailyHomeworks: Map<Instant, DailyHomeworksUi>,
    homeworkErrors: HomeworkErrorsUi?,
    onEditHomework: (HomeworkDetailsUi) -> Unit,
    onDoHomework: (HomeworkDetailsUi) -> Unit,
    onSkipHomework: (HomeworkDetailsUi) -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            var taskErrorsSheetState by remember { mutableStateOf(false) }
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = TasksThemeRes.strings.homeworkExecutionAnalysisHeader,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                ComingHomeworksExecutionAnalysisView(
                    isLoading = isLoadingHomeworks,
                    currentDate = currentDate,
                    homeworks = dailyHomeworks,
                )
                AllHomeworksExecutionAnalysisView(
                    modifier = Modifier.weight(1f),
                    isLoading = isLoadingErrors,
                    totalCompleted = 120, // todo
                    homeworkErrors = homeworkErrors,
                    onShowErrors = { taskErrorsSheetState = true },
                )
            }

//            if (taskErrorsSheetState) {
//                TaskErrorsBottomSheet(
//                    sheetState = sheetState,
//                    homeworkErrors = homeworkErrors,
//                    onDismissRequest = { taskErrorsSheetState = false },
//                    onEditHomework = onEditHomework,
//                    onDoHomework = onDoHomework,
//                    onSkipHomework = onSkipHomework,
//                )
//            }
        }
    }
}