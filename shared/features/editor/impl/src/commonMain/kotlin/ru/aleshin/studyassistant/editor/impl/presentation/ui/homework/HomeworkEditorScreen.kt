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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.homework

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
import cafe.adriel.voyager.navigator.currentOrThrow
import functional.UID
import navigation.nestedPop
import ru.aleshin.studyassistant.editor.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.contarct.HomeworkEditorDeps
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.contarct.HomeworkEditorEffect
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.contarct.HomeworkEditorEvent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.contarct.HomeworkEditorViewState
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.screenmodel.rememberHomeworkEditorScreenModel
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.views.HomeworkEditorBottomActions
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.views.HomeworkEditorTopBar
import views.ErrorSnackbar

/**
 * @author Stanislav Aleshin on 23.06.2024
 */
internal data class HomeworkEditorScreen(
    val homeworkId: UID?,
    val date: Long?,
    val subjectId: UID?,
    val organizationId: UID?,
) : Screen {

    @Composable
    override fun Content() = ScreenContent(
        screenModel = rememberHomeworkEditorScreenModel(),
        initialState = HomeworkEditorViewState(),
        dependencies = HomeworkEditorDeps(
            homeworkId = homeworkId,
            date = date,
            subjectId = subjectId,
            organizationId = organizationId,
        )
    ) { state ->
        val strings = EditorThemeRes.strings
        val navigator = LocalNavigator.currentOrThrow
        val snackbarState = remember { SnackbarHostState() }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            content = { paddingValues ->
                HomeworkEditorContent(
                    state = state,
                    modifier = Modifier.padding(paddingValues),
                    onAddOrganization = { dispatchEvent(HomeworkEditorEvent.NavigateToOrganizationEditor(null)) },
                    onAddSubject = { dispatchEvent(HomeworkEditorEvent.NavigateToSubjectEditor(null)) },
                    onSelectedOrganization = { dispatchEvent(HomeworkEditorEvent.UpdateOrganization(it)) },
                    onSelectedSubject = { dispatchEvent(HomeworkEditorEvent.UpdateSubject(it)) },
                    onSelectedDate = { dispatchEvent(HomeworkEditorEvent.UpdateDate(it)) },
                    onSelectedClass = { classModel, date ->
                        dispatchEvent(HomeworkEditorEvent.UpdateLinkedClass(classModel?.uid, date))
                    },
                    onTaskChange = { theory, practice, presentations ->
                        dispatchEvent(HomeworkEditorEvent.UpdateTask(theory, practice, presentations))
                    },
                    onTestChange = { isTest, topic ->
                        dispatchEvent(HomeworkEditorEvent.UpdateTestTopic(isTest, topic))
                    },
                    onChangePriority = { dispatchEvent(HomeworkEditorEvent.UpdatePriority(it)) },
                )
            },
            topBar = {
                HomeworkEditorTopBar(
                    onBackPressed = { dispatchEvent(HomeworkEditorEvent.NavigateToBack) },
                )
            },
            bottomBar = {
                HomeworkEditorBottomActions(
                    saveEnabled = state.editableHomework?.isValid() == true,
                    showDeleteAction = homeworkId != null,
                    onCancelClick = { dispatchEvent(HomeworkEditorEvent.NavigateToBack) },
                    onSaveClick = { dispatchEvent(HomeworkEditorEvent.SaveHomework) },
                    onDeleteClick = { dispatchEvent(HomeworkEditorEvent.DeleteHomework) },
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
                is HomeworkEditorEffect.NavigateToBack -> navigator.nestedPop()
                is HomeworkEditorEffect.NavigateToLocal -> navigator.push(effect.pushScreen)
                is HomeworkEditorEffect.ShowError -> {
                    snackbarState.showSnackbar(
                        message = effect.failures.mapToMessage(strings),
                        withDismissAction = true,
                    )
                }
            }
        }
    }
}