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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import ru.aleshin.studyassistant.core.common.architecture.store.compose.handleEffects
import ru.aleshin.studyassistant.core.common.architecture.store.compose.stateAsState
import ru.aleshin.studyassistant.core.ui.utils.useLargeLayout
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.schedule.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.contract.OverviewEffect
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.contract.OverviewEvent
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.store.OverviewComponent
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.views.OverviewBottomBar
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.views.OverviewDatePicker
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.views.OverviewTopBar
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.views.OverviewTopSheet

/**
 * @author Stanislav Aleshin on 09.06.2024.
 */
@Composable
internal fun OverviewContent(
    overviewComponent: OverviewComponent,
    modifier: Modifier = Modifier,
) {
    val store = overviewComponent.store
    val state by store.stateAsState()
    val snackbarState = remember { SnackbarHostState() }
    val adaptiveInfo = currentWindowAdaptiveInfoV2()
    val layoutMode = adaptiveInfo.fetchOverviewLayoutMode()
    var isDatePickerOpen by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            if (layoutMode.showScreenTopBar) {
                Column {
                    OverviewTopBar(
                        enabledEdit = state.selectedDate != null,
                        onEditClick = {
                            store.dispatchEvent(OverviewEvent.ClickEdit)
                        },
                        onCurrentDay = {
                            store.dispatchEvent(OverviewEvent.SelectedCurrentDay)
                        },
                        onDetailsClick = {
                            store.dispatchEvent(OverviewEvent.ClickDetails)
                        },
                    )
                    OverviewTopSheet(
                        isLoadingSchedule = state.isScheduleLoading,
                        isLoadingAnalytics = state.isAnalyticsLoading,
                        selectedDate = state.selectedDate,
                        weekAnalysis = state.weekAnalysis,
                        activeClass = state.activeClass,
                    )
                }
            }
        },
        bottomBar = {
            if (layoutMode.showDateBottomBar) {
                OverviewBottomBar(
                    currentDate = state.currentDate,
                    selectedDate = state.selectedDate,
                    onSelectedDate = {
                        store.dispatchEvent(OverviewEvent.SelectedDate(it))
                    },
                )
            }
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarState,
                snackbar = { ErrorSnackbar(it) },
            )
        },
        contentWindowInsets = WindowInsets(),
    ) { paddingValues ->
        OverviewLayout(
            modifier = Modifier.padding(paddingValues),
            state = state,
            windowAdaptiveInfo = adaptiveInfo,
            layoutMode = layoutMode,
            useLargeLayout = adaptiveInfo.useLargeLayout,
            onEditClick = { store.dispatchEvent(OverviewEvent.ClickEdit) },
            onCurrentDay = { store.dispatchEvent(OverviewEvent.SelectedCurrentDay) },
            onDetailsClick = { store.dispatchEvent(OverviewEvent.ClickDetails) },
            onDateChange = { store.dispatchEvent(OverviewEvent.SelectedDate(it)) },
            onOpenCalendar = { isDatePickerOpen = true },
            onAddHomeworkClick = { homework, date ->
                store.dispatchEvent(OverviewEvent.ClickAddHomework(homework, date))
            },
            onEditHomeworkClick = {
                store.dispatchEvent(OverviewEvent.ClickEditHomework(it))
            },
            onAgainHomeworkClick = {
                store.dispatchEvent(OverviewEvent.ClickAgainHomework(it))
            },
            onCompleteHomeworkClick = {
                store.dispatchEvent(OverviewEvent.ClickCompleteHomework(it))
            },
        )
    }

    if (isDatePickerOpen) {
        OverviewDatePicker(
            selectedDate = state.selectedDate,
            onDismiss = { isDatePickerOpen = false },
            onDateSelect = { date ->
                store.dispatchEvent(OverviewEvent.SelectedDate(date))
            },
        )
    }

    store.handleEffects { effect ->
        when (effect) {
            is OverviewEffect.ShowError -> {
                snackbarState.showSnackbar(
                    message = effect.failures.mapToMessage(),
                    withDismissAction = true,
                )
            }
        }
    }
}
