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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.layouts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.ui.theme.tokens.AdaptiveLayoutDefaults
import ru.aleshin.studyassistant.core.ui.views.adaptive.AdaptiveSupportingPaneScaffold
import ru.aleshin.studyassistant.schedule.impl.presentation.models.classes.ClassDetailsUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.homework.HomeworkDetailsUi
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.contract.OverviewState
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.views.OverviewClassesSection
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.views.OverviewMainPaneTopBar
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.views.OverviewSupportingPane

/**
 * @author Stanislav Aleshin on 17.08.2026.
 */
@Composable
internal fun OverviewExpandedLayout(
    modifier: Modifier = Modifier,
    state: OverviewState,
    windowAdaptiveInfo: WindowAdaptiveInfo,
    classListMaxWidth: Dp?,
    supportingPanePreferredWidth: Dp,
    useTwoPanesOnMediumWidth: Boolean,
    showPaneExpansionDragHandle: Boolean,
    onEditClick: () -> Unit,
    onCurrentDay: () -> Unit,
    onDetailsClick: () -> Unit,
    onDateChange: (Instant) -> Unit,
    onOpenCalendar: () -> Unit,
    onAddHomeworkClick: (ClassDetailsUi, Instant) -> Unit,
    onEditHomeworkClick: (HomeworkDetailsUi) -> Unit,
    onAgainHomeworkClick: (HomeworkDetailsUi) -> Unit,
    onCompleteHomeworkClick: (HomeworkDetailsUi) -> Unit,
) {
    AdaptiveSupportingPaneScaffold(
        modifier = modifier,
        windowAdaptiveInfo = windowAdaptiveInfo,
        mainPaneMinWidth = AdaptiveLayoutDefaults.OverviewMainPaneMinWidth,
        supportingPaneMinWidth = AdaptiveLayoutDefaults.OverviewSupportingPaneMinWidth,
        supportingPaneMaxWidth = AdaptiveLayoutDefaults.OverviewSupportingPaneMaxWidth,
        supportingPanePreferredWidth = supportingPanePreferredWidth,
        useTwoPanesOnMediumWidth = useTwoPanesOnMediumWidth,
        showPaneExpansionDragHandle = showPaneExpansionDragHandle,
        mainPane = {
            Column(modifier = Modifier.fillMaxSize()) {
                OverviewMainPaneTopBar(
                    modifier = Modifier.fillMaxWidth(),
                    selectedDate = state.selectedDate,
                    enabledEdit = state.selectedDate != null,
                    onEditClick = onEditClick,
                    onCurrentDay = onCurrentDay,
                    onDetailsClick = onDetailsClick,
                )
                OverviewClassesSection(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    isScheduleLoading = state.isScheduleLoading,
                    selectedDate = state.selectedDate,
                    schedule = state.schedule,
                    activeClass = state.activeClass,
                    contentMaxWidth = classListMaxWidth,
                    showBottomSpacer = false,
                    onAddHomeworkClick = onAddHomeworkClick,
                    onEditHomeworkClick = onEditHomeworkClick,
                    onAgainHomeworkClick = onAgainHomeworkClick,
                    onCompleteHomeworkClick = onCompleteHomeworkClick,
                )
            }
        },
        supportingPane = {
            OverviewSupportingPane(
                modifier = Modifier.fillMaxSize(),
                isLoadingSchedule = state.isScheduleLoading,
                isLoadingAnalytics = state.isAnalyticsLoading,
                selectedDate = state.selectedDate,
                weekAnalysis = state.weekAnalysis,
                activeClass = state.activeClass,
                onDateChange = onDateChange,
                onOpenCalendar = onOpenCalendar,
            )
        },
    )
}
