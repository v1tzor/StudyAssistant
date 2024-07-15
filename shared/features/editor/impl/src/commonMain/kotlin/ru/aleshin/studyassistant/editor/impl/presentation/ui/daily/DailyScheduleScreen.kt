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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.daily

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ru.aleshin.studyassistant.core.common.architecture.screen.ScreenContent
import ru.aleshin.studyassistant.core.common.extensions.navigationBarsInDp
import ru.aleshin.studyassistant.core.common.extensions.safeNavigationBarsInPx
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.navigation.nestedPop
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.BottomSheetScaffold
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.editor.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes
import ru.aleshin.studyassistant.editor.impl.presentation.ui.daily.contract.DailyScheduleDeps
import ru.aleshin.studyassistant.editor.impl.presentation.ui.daily.contract.DailyScheduleEffect
import ru.aleshin.studyassistant.editor.impl.presentation.ui.daily.contract.DailyScheduleEvent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.daily.contract.DailyScheduleViewState
import ru.aleshin.studyassistant.editor.impl.presentation.ui.daily.screenmodel.rememberDailyScheduleScreenModel
import ru.aleshin.studyassistant.editor.impl.presentation.ui.daily.views.DailyScheduleBottomSheet
import ru.aleshin.studyassistant.editor.impl.presentation.ui.daily.views.DailyScheduleTopBar

/**
 * @author Stanislav Aleshin on 14.07.2024
 */
internal data class DailyScheduleScreen(
    val date: Long,
    val baseScheduleId: UID?,
    val customScheduleId: UID?,
) : Screen {

    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    override fun Content() = ScreenContent(
        screenModel = rememberDailyScheduleScreenModel(),
        initialState = DailyScheduleViewState(),
        dependencies = DailyScheduleDeps(
            date = date,
            baseScheduleId = baseScheduleId,
            customScheduleId = customScheduleId,
        )
    ) { state ->
        val strings = EditorThemeRes.strings
        val navigator = LocalNavigator.currentOrThrow
        val snackbarState = remember { SnackbarHostState() }
        val sheetState = rememberStandardBottomSheetState(confirmValueChange = { it != SheetValue.Hidden })
        val scaffoldState = rememberBottomSheetScaffoldState(sheetState, snackbarState)
        var layoutHeight by rememberSaveable { mutableIntStateOf(0) }
        val navBar = WindowInsets.safeNavigationBarsInPx(LocalDensity.current)

        Box(modifier = Modifier.onGloballyPositioned { layoutHeight = it.size.height - navBar }) {
            BottomSheetScaffold(
                modifier = Modifier.fillMaxSize(),
                sheetContent = {
                    DailyScheduleBottomSheet(
                        sheetState = sheetState,
                        layoutHeight = layoutHeight,
                        isLoading = state.isLoading,
                        editMode = state.customSchedule != null,
                        targetDate = state.targetDate,
                        customSchedule = state.customSchedule,
                        onEditClick = { dispatchEvent(DailyScheduleEvent.CreateCustomSchedule) },
                        onSaveClick = { dispatchEvent(DailyScheduleEvent.NavigateToBack) },
                        onReturnScheduleClick = { dispatchEvent(DailyScheduleEvent.DeleteCustomSchedule) },
                        onEditStartOfDay = { dispatchEvent(DailyScheduleEvent.FastEditStartOfDay(it)) },
                        onEditClassesDuration = { dispatchEvent(DailyScheduleEvent.FastEditClassesDuration(it)) },
                        onEditBreaksDuration = { dispatchEvent(DailyScheduleEvent.FastEditBreaksDuration(it)) },
                    )
                },
                content = { paddingValues ->
                    DailyScheduleContent(
                        state = state,
                        modifier = Modifier.padding(paddingValues),
                        onEditClass = { dispatchEvent(DailyScheduleEvent.EditClassInEditor(it)) },
                        onCreateClass = { dispatchEvent(DailyScheduleEvent.CreateClassInEditor) },
                        onDeleteClass = { dispatchEvent(DailyScheduleEvent.DeleteClass(it.uid)) },
                        onSwapClasses = { from, to -> dispatchEvent(DailyScheduleEvent.SwapClasses(from, to)) },
                    )
                },
                topBar = {
                    DailyScheduleTopBar(
                        onBackClick = { dispatchEvent(DailyScheduleEvent.NavigateToBack) },
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
                is DailyScheduleEffect.NavigateToLocal -> navigator.push(effect.pushScreen)
                is DailyScheduleEffect.NavigateToBack -> navigator.nestedPop()
                is DailyScheduleEffect.ShowError -> {
                    snackbarState.showSnackbar(
                        message = effect.failures.mapToMessage(strings),
                        withDismissAction = true,
                    )
                }
            }
        }
    }
}