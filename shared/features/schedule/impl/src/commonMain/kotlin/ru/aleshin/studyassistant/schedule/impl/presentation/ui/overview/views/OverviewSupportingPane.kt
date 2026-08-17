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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.common.extensions.equalsDay
import ru.aleshin.studyassistant.schedule.impl.presentation.models.analysis.DailyAnalysisUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.classes.ActiveClassUi
import ru.aleshin.studyassistant.schedule.impl.resources.Res
import ru.aleshin.studyassistant.schedule.impl.resources.analysis_workload_title

/**
 * @author Stanislav Aleshin on 17.08.2026.
 */
@Composable
internal fun OverviewSupportingPane(
    modifier: Modifier = Modifier,
    isLoadingSchedule: Boolean,
    isLoadingAnalytics: Boolean,
    selectedDate: Instant?,
    weekAnalysis: List<DailyAnalysisUi>?,
    activeClass: ActiveClassUi?,
    onDateChange: (Instant) -> Unit,
    onOpenCalendar: () -> Unit,
) {
    val currentAnalysis = remember(weekAnalysis, selectedDate) {
        weekAnalysis?.find { it.date.equalsDay(selectedDate) }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(
                    insets = WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical + WindowInsetsSides.End),
                )
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                OverviewTopSheetAnalysis(
                    modifier = Modifier.fillMaxWidth(),
                    isLoading = isLoadingAnalytics,
                    analysis = currentAnalysis,
                    useContainer = false,
                    useContentContainer = true,
                    useCompactStyle = false,
                )
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = stringResource(Res.string.analysis_workload_title),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surfaceContainer,
                    ) {
                        OverviewTopSheetChart(
                            modifier = Modifier.padding(16.dp),
                            selectedDate = selectedDate,
                            weekAnalysis = weekAnalysis,
                        )
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                ) {
                    OverviewTopSheetClassTime(
                        modifier = Modifier.padding(12.dp),
                        isLoading = isLoadingSchedule || isLoadingAnalytics,
                        activeClass = activeClass,
                        homeworksProgressList = currentAnalysis?.numberOfHomeworks ?: emptyList(),
                        tasksProgressList = currentAnalysis?.numberOfTasks ?: emptyList(),
                    )
                }
                OverviewDateChooser(
                    modifier = Modifier.fillMaxWidth(),
                    selectedDate = selectedDate,
                    onDateChange = onDateChange,
                    onOpenCalendar = onOpenCalendar,
                )
            }
        }
    }
}
