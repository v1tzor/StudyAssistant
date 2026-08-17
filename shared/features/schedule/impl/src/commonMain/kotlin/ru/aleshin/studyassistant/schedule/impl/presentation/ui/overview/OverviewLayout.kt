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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview

import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.ui.theme.tokens.AdaptiveLayoutDefaults
import ru.aleshin.studyassistant.schedule.impl.presentation.models.classes.ClassDetailsUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.homework.HomeworkDetailsUi
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.contract.OverviewState
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.layouts.OverviewExpandedLayout
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.views.OverviewClassesSection

/**
 * @author Stanislav Aleshin on 17.08.2026.
 */
@Composable
internal fun OverviewLayout(
    modifier: Modifier = Modifier,
    state: OverviewState,
    windowAdaptiveInfo: WindowAdaptiveInfo,
    layoutMode: OverviewLayoutMode,
    useLargeLayout: Boolean,
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
    when (layoutMode) {
        OverviewLayoutMode.COMPACT -> OverviewClassesSection(
            modifier = modifier,
            isScheduleLoading = state.isScheduleLoading,
            selectedDate = state.selectedDate,
            schedule = state.schedule,
            activeClass = state.activeClass,
            onAddHomeworkClick = onAddHomeworkClick,
            onEditHomeworkClick = onEditHomeworkClick,
            onAgainHomeworkClick = onAgainHomeworkClick,
            onCompleteHomeworkClick = onCompleteHomeworkClick,
        )
        OverviewLayoutMode.MEDIUM -> OverviewClassesSection(
            modifier = modifier,
            isScheduleLoading = state.isScheduleLoading,
            selectedDate = state.selectedDate,
            schedule = state.schedule,
            activeClass = state.activeClass,
            contentMaxWidth = AdaptiveLayoutDefaults.MediumContentMaxWidth,
            onAddHomeworkClick = onAddHomeworkClick,
            onEditHomeworkClick = onEditHomeworkClick,
            onAgainHomeworkClick = onAgainHomeworkClick,
            onCompleteHomeworkClick = onCompleteHomeworkClick,
        )
        OverviewLayoutMode.SUPPORTING_PANE,
        OverviewLayoutMode.BOOK -> OverviewExpandedLayout(
            modifier = modifier,
            state = state,
            windowAdaptiveInfo = windowAdaptiveInfo,
            classListMaxWidth = if (layoutMode == OverviewLayoutMode.BOOK) null else 520.dp,
            supportingPanePreferredWidth = layoutMode.supportingPaneWidth(useLargeLayout = useLargeLayout),
            useTwoPanesOnMediumWidth = layoutMode == OverviewLayoutMode.BOOK,
            showPaneExpansionDragHandle = layoutMode == OverviewLayoutMode.SUPPORTING_PANE,
            onEditClick = onEditClick,
            onCurrentDay = onCurrentDay,
            onDetailsClick = onDetailsClick,
            onDateChange = onDateChange,
            onOpenCalendar = onOpenCalendar,
            onAddHomeworkClick = onAddHomeworkClick,
            onEditHomeworkClick = onEditHomeworkClick,
            onAgainHomeworkClick = onAgainHomeworkClick,
            onCompleteHomeworkClick = onCompleteHomeworkClick,
        )
    }
}
