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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.subject

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import architecture.screen.ScreenContent
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import functional.UID
import navigation.nestedPop
import ru.aleshin.studyassistant.editor.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes
import ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.contract.SubjectEditorDeps
import ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.contract.SubjectEditorEffect
import ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.contract.SubjectEditorEvent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.contract.SubjectEditorViewState
import ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.screeenmodel.rememberSubjectEditorScreenModel
import ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.views.SubjectEditorTopBar
import views.ErrorSnackbar

/**
 * @author Stanislav Aleshin on 05.06.2024
 */
internal data class SubjectEditorScreen(
    private val subjectId: UID?,
    private val organizationId: UID,
) : Screen {

    @Composable
    override fun Content() = ScreenContent(
        screenModel = rememberSubjectEditorScreenModel(),
        initialState = SubjectEditorViewState(),
        dependencies = SubjectEditorDeps(
            subjectId = subjectId,
            organizationId = organizationId,
        )
    ) { state ->
        val strings = EditorThemeRes.strings
        val navigator = LocalNavigator.current
        val snackbarState = remember { SnackbarHostState() }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            content = { paddingValues ->
                SubjectEditorContent(
                    state = state,
                    modifier = Modifier.padding(paddingValues),
                    onAddTeacher = { dispatchEvent(SubjectEditorEvent.NavigateToEmployeeEditor) },
                    onUpdateLocations = { dispatchEvent(SubjectEditorEvent.UpdateLocations(it)) },
                    onUpdateOffices = { dispatchEvent(SubjectEditorEvent.UpdateOffices(it)) },
                    onSelectEventType = { dispatchEvent(SubjectEditorEvent.SelectEventType(it)) },
                    onPickColor = { dispatchEvent(SubjectEditorEvent.PickColor(it)) },
                    onSelectTeacher = { dispatchEvent(SubjectEditorEvent.SelectTeacher(it)) },
                    onEditName = { dispatchEvent(SubjectEditorEvent.EditName(it)) },
                    onSelectLocation = { location, office ->
                        dispatchEvent(SubjectEditorEvent.SelectLocation(location, office))
                    },
                )
            },
            topBar = {
                SubjectEditorTopBar(
                    enabledSave = state.editableSubject?.isValid() ?: false,
                    onBackClick = { dispatchEvent(SubjectEditorEvent.NavigateToBack) },
                    onSaveClick = { dispatchEvent(SubjectEditorEvent.SaveSubject) },
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
                is SubjectEditorEffect.NavigateToLocal -> navigator?.push(effect.pushScreen)
                is SubjectEditorEffect.NavigateToBack -> navigator?.nestedPop()
                is SubjectEditorEffect.ShowError -> {
                    snackbarState.showSnackbar(
                        message = effect.failures.mapToMessage(strings),
                        withDismissAction = true,
                    )
                }
            }
        }
    }
}