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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DayOfWeek
import ru.aleshin.studyassistant.core.common.architecture.store.compose.handleEffects
import ru.aleshin.studyassistant.core.common.architecture.store.compose.stateAsState
import ru.aleshin.studyassistant.core.common.extensions.navigationBarsInDp
import ru.aleshin.studyassistant.core.common.extensions.safeNavigationBarsInPx
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.core.ui.views.sheet.BottomSheetScaffold
import ru.aleshin.studyassistant.editor.api.DayOfNumberedWeekUi
import ru.aleshin.studyassistant.editor.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.editor.impl.presentation.models.classes.ClassUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.schedules.BaseScheduleUi
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes
import ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.contract.WeekScheduleEffect
import ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.contract.WeekScheduleEvent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.contract.WeekScheduleState
import ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.store.WeekScheduleComponent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.views.ScheduleView
import ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.views.ScheduleViewPlaceholder
import ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.views.WeekScheduleBottomSheet
import ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.views.WeekScheduleTopBar

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun WeekScheduleContent(
    weekScheduleComponent: WeekScheduleComponent,
    modifier: Modifier = Modifier,
) {
    val store = weekScheduleComponent.store
    val state by store.stateAsState()
    val strings = EditorThemeRes.strings
    val coreStrings = StudyAssistantRes.strings
    val snackbarState = remember { SnackbarHostState() }
    val sheetState = rememberStandardBottomSheetState(confirmValueChange = { it != SheetValue.Hidden })
    val scaffoldState = rememberBottomSheetScaffoldState(sheetState, snackbarState)
    var layoutHeight by rememberSaveable { mutableIntStateOf(0) }
    val navBar = WindowInsets.safeNavigationBarsInPx(LocalDensity.current)

    Box(modifier = modifier.onGloballyPositioned { layoutHeight = it.size.height - navBar }) {
        BottomSheetScaffold(
            modifier = Modifier.fillMaxSize(),
            sheetContent = {
                WeekScheduleBottomSheet(
                    sheetState = sheetState,
                    layoutHeight = layoutHeight,
                    isLoading = state.isLoading,
                    weekSchedule = state.weekSchedule,
                    organizations = state.organizations,
                    maxNumberOfWeek = state.calendarSettings?.numberOfWeek,
                    selectedWeek = state.selectedWeek,
                    onSaveClick = { store.dispatchEvent(WeekScheduleEvent.NavigateToBack) },
                    onUpdateOrganization = { store.dispatchEvent(WeekScheduleEvent.UpdateOrganization(it)) },
                    onAddOrganization = { store.dispatchEvent(WeekScheduleEvent.NavigateToOrganizationEditor) },
                    onSelectedWeek = { store.dispatchEvent(WeekScheduleEvent.ChangeWeek(it)) },
                )
            },
            content = { paddingValues ->
                BaseWeekScheduleContent(
                    state = state,
                    modifier = Modifier.padding(paddingValues),
                    onRefresh = {
                        store.dispatchEvent(WeekScheduleEvent.Refresh)
                    },
                    onCreateClass = { weekDay, schedule ->
                        store.dispatchEvent(WeekScheduleEvent.CreateClassInEditor(weekDay, schedule))
                    },
                    onEditClass = { editClass, weekDay ->
                        store.dispatchEvent(WeekScheduleEvent.EditClassInEditor(editClass, weekDay))
                    },
                    onDeleteClass = { uid, schedule ->
                        store.dispatchEvent(WeekScheduleEvent.DeleteClass(uid, schedule))
                    },
                )
            },
            topBar = {
                WeekScheduleTopBar(
                    modifier = Modifier.statusBarsPadding(),
                )
            },
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarState,
                    snackbar = { ErrorSnackbar(it) },
                )
            },
            sheetDragHandle = null,
            scaffoldState = scaffoldState,
            sheetContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            containerColor = MaterialTheme.colorScheme.background,
            sheetTonalElevation = StudyAssistantRes.elevations.levelZero,
            sheetPeekHeight = 150.dp + WindowInsets.navigationBarsInDp(),
        )
    }

    store.handleEffects { effect ->
        when (effect) {
            is WeekScheduleEffect.ShowError -> {
                snackbarState.showSnackbar(
                    message = effect.failures.mapToMessage(strings, coreStrings),
                    withDismissAction = true,
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun BaseWeekScheduleContent(
    state: WeekScheduleState,
    modifier: Modifier = Modifier,
    refreshState: PullToRefreshState = rememberPullToRefreshState(),
    onRefresh: () -> Unit,
    onCreateClass: (DayOfNumberedWeekUi, BaseScheduleUi?) -> Unit,
    onEditClass: (ClassUi, DayOfNumberedWeekUi) -> Unit,
    onDeleteClass: (UID, BaseScheduleUi) -> Unit,
) {
    PullToRefreshBox(
        modifier = modifier,
        isRefreshing = state.isLoading,
        onRefresh = onRefresh,
        state = refreshState,
    ) {
        Crossfade(
            modifier = Modifier.fillMaxSize().padding(start = 16.dp, top = 16.dp, end = 16.dp),
            targetState = state.isLoading,
            animationSpec = spring(stiffness = Spring.StiffnessLow),
        ) { loading ->
            if (!loading) {
                val listState = rememberLazyListState()
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(DayOfWeek.entries.toTypedArray(), key = { it.name }) { dayOfWeek ->
                        val dayOfWeekSchedule = state.weekSchedule?.weekDaySchedules?.get(dayOfWeek)
                        ScheduleView(
                            dayOfWeek = dayOfWeek,
                            schedule = dayOfWeekSchedule,
                            onCreateClass = {
                                onCreateClass(DayOfNumberedWeekUi(dayOfWeek, state.selectedWeek), dayOfWeekSchedule)
                            },
                            onEditClass = { editClass ->
                                onEditClass(editClass, DayOfNumberedWeekUi(dayOfWeek, state.selectedWeek))
                            },
                            onDeleteClass = { targetClass ->
                                if (dayOfWeekSchedule != null) onDeleteClass(targetClass.uid, dayOfWeekSchedule)
                            },
                        )
                    }
                    item { Spacer(modifier = Modifier.height(60.dp)) }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(DayOfWeek.entries.size) { ScheduleViewPlaceholder() }
                }
            }
        }
    }
}