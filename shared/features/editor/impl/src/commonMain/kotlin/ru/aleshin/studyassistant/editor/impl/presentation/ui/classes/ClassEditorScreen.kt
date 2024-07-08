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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.classes

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ru.aleshin.studyassistant.core.common.architecture.screen.ScreenContent
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.navigation.nestedPop
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.editor.api.ui.DayOfNumberedWeekUi
import ru.aleshin.studyassistant.editor.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.contract.ClassEditorDeps
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.contract.ClassEditorEffect
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.contract.ClassEditorEvent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.contract.ClassEditorViewState
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.screenmodel.rememberClassEditorScreenModel
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.views.ClassEditorTopBar

/**
 * @author Stanislav Aleshin on 02.06.2024
 */
internal class ClassEditorScreen(
    private val classId: UID?,
    private val scheduleId: UID?,
    private val organizationId: UID?,
    private val customSchedule: Boolean,
    private val weekDay: DayOfNumberedWeekUi,
) : Screen {

    @Composable
    override fun Content() = ScreenContent(
        screenModel = rememberClassEditorScreenModel(),
        initialState = ClassEditorViewState(),
        dependencies = ClassEditorDeps(
            classId = classId,
            scheduleId = scheduleId,
            organizationId = organizationId,
            customSchedule = customSchedule,
            weekDay = weekDay,
        ),
    ) { state ->
        val strings = EditorThemeRes.strings
        val navigator = LocalNavigator.currentOrThrow
        val snackbarState = remember { SnackbarHostState() }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            content = { paddingValues ->
                ClassEditorContent(
                    state = state,
                    modifier = Modifier.padding(paddingValues),
                    onAddOrganization = {},
                    onAddSubject = { dispatchEvent(ClassEditorEvent.NavigateToSubjectEditor(null)) },
                    onAddTeacher = { dispatchEvent(ClassEditorEvent.NavigateToEmployeeEditor(null)) },
                    onUpdateLocations = { dispatchEvent(ClassEditorEvent.UpdateOrganizationLocations(it)) },
                    onUpdateOffices = { dispatchEvent(ClassEditorEvent.UpdateOrganizationOffices(it)) },
                    onSelectOrganization = { dispatchEvent(ClassEditorEvent.UpdateOrganization(it)) },
                    onSelectTeacher = { dispatchEvent(ClassEditorEvent.UpdateTeacher(it)) },
                    onChangeNotifyParams = { dispatchEvent(ClassEditorEvent.UpdateNotifyParams(it)) },
                    onSelectSubject = { type, subject ->
                        dispatchEvent(ClassEditorEvent.UpdateSubject(type, subject))
                    },
                    onSelectLocation = { location, office ->
                        dispatchEvent(ClassEditorEvent.UpdateLocation(location, office))
                    },
                    onSelectTime = { start, end ->
                        dispatchEvent(ClassEditorEvent.UpdateTime(start, end))
                    },
                )
            },
            topBar = {
                ClassEditorTopBar(
                    enabledSave = state.editableClass?.isValid() ?: false,
                    onSaveClick = { dispatchEvent(ClassEditorEvent.SaveClass) },
                    onBackClick = { dispatchEvent(ClassEditorEvent.NavigateToBack) },
                )
            },
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarState,
                    snackbar = { ErrorSnackbar(it) },
                )
            },
        )

        handleEffect { effect ->
            when (effect) {
                is ClassEditorEffect.NavigateToLocal -> navigator.push(effect.pushScreen)
                is ClassEditorEffect.NavigateToBack -> navigator.nestedPop()
                is ClassEditorEffect.ShowError -> {
                    snackbarState.showSnackbar(
                        message = effect.failures.mapToMessage(strings),
                        withDismissAction = true,
                    )
                }
            }
        }
    }
}