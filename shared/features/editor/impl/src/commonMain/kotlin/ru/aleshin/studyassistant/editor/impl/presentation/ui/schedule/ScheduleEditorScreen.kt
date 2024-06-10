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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import architecture.screen.ScreenContent
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import extensions.navigationBarsInDp
import extensions.safeNavigationBarsInPx
import navigation.root
import ru.aleshin.studyassistant.editor.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes
import ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.contract.ScheduleEditorEffect
import ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.contract.ScheduleEditorEvent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.contract.ScheduleEditorViewState
import ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.screenmodel.rememberScheduleEditorScreenModel
import ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.views.ScheduleEditorBottomSheet
import ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.views.ScheduleEditorTopBar
import theme.StudyAssistantRes
import views.BottomSheetScaffold
import views.ErrorSnackbar

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
internal class ScheduleEditorScreen : Screen {

    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    override fun Content() = ScreenContent(
        screenModel = rememberScheduleEditorScreenModel(),
        initialState = ScheduleEditorViewState(),
    ) { state ->
        val strings = EditorThemeRes.strings
        val navigator = LocalNavigator.currentOrThrow
        val snackbarState = remember { SnackbarHostState() }
        val sheetState = rememberStandardBottomSheetState(
            confirmValueChange = { it != SheetValue.Hidden }
        )
        val scaffoldState = rememberBottomSheetScaffoldState(sheetState, snackbarState)
        var layoutHeight by rememberSaveable { mutableIntStateOf(0) }
        val navBar = WindowInsets.safeNavigationBarsInPx(LocalDensity.current)

        Box(modifier = Modifier.onGloballyPositioned { layoutHeight = it.size.height - navBar }) {
            BottomSheetScaffold(
                modifier = Modifier.fillMaxSize(),
                sheetContent = {
                    ScheduleEditorBottomSheet(
                        sheetState = sheetState,
                        layoutHeight = layoutHeight,
                        isLoading = state.isLoading,
                        weekSchedule = state.weekSchedule,
                        organizations = state.organizations,
                        numberOfWeek = state.calendarSettings?.numberOfWeek,
                        currentWeek = state.currentWeek,
                        onSaveClick = { dispatchEvent(ScheduleEditorEvent.SaveSchedule) },
                        onUpdateOrganization = { dispatchEvent(ScheduleEditorEvent.UpdateOrganization(it)) },
                        onSelectedWeek = { dispatchEvent(ScheduleEditorEvent.ChangeWeek(it)) },
                    )
                },
                content = { paddingValues ->
                    ScheduleEditorContent(
                        state = state,
                        modifier = Modifier.padding(paddingValues),
                        onCreateClass = { weekDay, schedule ->
                            dispatchEvent(ScheduleEditorEvent.CreateClass(weekDay, schedule))
                        },
                        onEditClass = { editClass, weekDay ->
                            dispatchEvent(ScheduleEditorEvent.EditClass(editClass, weekDay))
                        },
                        onDeleteClass = { uid, schedule ->
                            dispatchEvent(ScheduleEditorEvent.DeleteClass(uid, schedule))
                        },
                    )
                },
                topBar = {
                    ScheduleEditorTopBar(
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

        handleEffect { effect ->
            when (effect) {
                is ScheduleEditorEffect.NavigateToBack -> navigator.root().pop()
                is ScheduleEditorEffect.NavigateToLocal -> navigator.push(effect.pushScreen)
                is ScheduleEditorEffect.ShowError -> {
                    snackbarState.showSnackbar(
                        message = effect.failures.mapToMessage(strings),
                        withDismissAction = true,
                    )
                }
            }
        }
    }
}