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
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ru.aleshin.studyassistant.core.common.architecture.screen.ScreenContent
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.navigation.nestedPop
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.editor.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.contarct.HomeworkDeps
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.contarct.HomeworkEffect
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.contarct.HomeworkEvent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.contarct.HomeworkViewState
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.screenmodel.rememberHomeworkScreenModel
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.views.HomeworkBottomActions
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.views.HomeworkTopBar

/**
 * @author Stanislav Aleshin on 23.06.2024
 */
internal data class HomeworkScreen(
    private val homeworkId: UID?,
    private val date: Long?,
    private val subjectId: UID?,
    private val organizationId: UID?,
) : Screen {

    @Composable
    override fun Content() = ScreenContent(
        screenModel = rememberHomeworkScreenModel(),
        initialState = HomeworkViewState(),
        dependencies = HomeworkDeps(
            homeworkId = homeworkId,
            date = date,
            subjectId = subjectId,
            organizationId = organizationId,
        )
    ) { state ->
        val strings = EditorThemeRes.strings
        val coreStrings = StudyAssistantRes.strings
        val navigator = LocalNavigator.currentOrThrow
        val snackbarState = remember { SnackbarHostState() }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            content = { paddingValues ->
                HomeworkContent(
                    state = state,
                    modifier = Modifier.padding(paddingValues),
                    onAddOrganization = { dispatchEvent(HomeworkEvent.NavigateToOrganizationEditor(null)) },
                    onAddSubject = { dispatchEvent(HomeworkEvent.NavigateToSubjectEditor(null)) },
                    onEditSubject = { dispatchEvent(HomeworkEvent.NavigateToSubjectEditor(it.uid)) },
                    onSelectedOrganization = { dispatchEvent(HomeworkEvent.UpdateOrganization(it)) },
                    onSelectedSubject = { dispatchEvent(HomeworkEvent.UpdateSubject(it)) },
                    onSelectedDate = { dispatchEvent(HomeworkEvent.UpdateDate(it)) },
                    onSelectedClass = { classModel, date ->
                        dispatchEvent(HomeworkEvent.UpdateLinkedClass(classModel?.uid, date))
                    },
                    onTaskChange = { theory, practice, presentations ->
                        dispatchEvent(HomeworkEvent.UpdateTask(theory, practice, presentations))
                    },
                    onTestChange = { isTest, topic ->
                        dispatchEvent(HomeworkEvent.UpdateTestTopic(isTest, topic))
                    },
                    onChangePriority = { dispatchEvent(HomeworkEvent.UpdatePriority(it)) },
                )
            },
            topBar = {
                HomeworkTopBar(
                    onBackClick = { dispatchEvent(HomeworkEvent.NavigateToBack) },
                )
            },
            bottomBar = {
                HomeworkBottomActions(
                    isLoadingSave = state.isLoadingSave,
                    saveEnabled = state.editableHomework?.isValid() == true,
                    showDeleteAction = homeworkId != null,
                    onCancelClick = { dispatchEvent(HomeworkEvent.NavigateToBack) },
                    onSaveClick = { dispatchEvent(HomeworkEvent.SaveHomework) },
                    onDeleteClick = { dispatchEvent(HomeworkEvent.DeleteHomework) },
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
                is HomeworkEffect.NavigateToBack -> navigator.nestedPop()
                is HomeworkEffect.NavigateToLocal -> navigator.push(effect.pushScreen)
                is HomeworkEffect.ShowError -> {
                    snackbarState.showSnackbar(
                        message = effect.failures.mapToMessage(strings, coreStrings),
                        withDismissAction = true,
                    )
                }
            }
        }
    }
}