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
import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize
import ru.aleshin.studyassistant.core.common.architecture.screen.ScreenContent
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.navigation.nestedPop
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.editor.api.presentation.DayOfNumberedWeekUi
import ru.aleshin.studyassistant.editor.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.contract.ClassDeps
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.contract.ClassEffect
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.contract.ClassEvent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.contract.ClassViewState
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.screenmodel.rememberClassScreenModel
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.views.ClassTopBar

/**
 * @author Stanislav Aleshin on 02.06.2024
 */
@Parcelize
internal class ClassScreen(
    private val classId: UID?,
    private val scheduleId: UID?,
    private val organizationId: UID?,
    private val customSchedule: Boolean,
    private val weekDay: DayOfNumberedWeekUi,
) : Screen, Parcelable {

    @Composable
    override fun Content() = ScreenContent(
        screenModel = rememberClassScreenModel(),
        initialState = ClassViewState(),
        dependencies = ClassDeps(
            classId = classId,
            scheduleId = scheduleId,
            organizationId = organizationId,
            customSchedule = customSchedule,
            weekDay = weekDay,
        ),
    ) { state ->
        val strings = EditorThemeRes.strings
        val coreStrings = StudyAssistantRes.strings
        val navigator = LocalNavigator.currentOrThrow
        val snackbarState = remember { SnackbarHostState() }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            content = { paddingValues ->
                ClassContent(
                    state = state,
                    modifier = Modifier.padding(paddingValues),
                    onAddOrganization = { dispatchEvent(ClassEvent.NavigateToOrganizationEditor(null)) },
                    onAddSubject = { dispatchEvent(ClassEvent.NavigateToSubjectEditor(null)) },
                    onAddTeacher = { dispatchEvent(ClassEvent.NavigateToEmployeeEditor(null)) },
                    onEditSubject = { dispatchEvent(ClassEvent.NavigateToSubjectEditor(it.uid)) },
                    onEditEmployee = { dispatchEvent(ClassEvent.NavigateToEmployeeEditor(it.uid)) },
                    onUpdateLocations = { dispatchEvent(ClassEvent.UpdateOrganizationLocations(it)) },
                    onUpdateOffices = { dispatchEvent(ClassEvent.UpdateOrganizationOffices(it)) },
                    onSelectOrganization = { dispatchEvent(ClassEvent.UpdateOrganization(it)) },
                    onSelectTeacher = { dispatchEvent(ClassEvent.UpdateTeacher(it)) },
                    onSelectSubject = { type, subject ->
                        dispatchEvent(ClassEvent.UpdateSubject(type, subject))
                    },
                    onSelectLocation = { location, office ->
                        dispatchEvent(ClassEvent.UpdateLocation(location, office))
                    },
                    onSelectTime = { start, end ->
                        dispatchEvent(ClassEvent.UpdateTime(start, end))
                    },
                )
            },
            topBar = {
                ClassTopBar(
                    enabledSave = state.editableClass?.isValid() ?: false,
                    onSaveClick = { dispatchEvent(ClassEvent.SaveClass) },
                    onBackClick = { dispatchEvent(ClassEvent.NavigateToBack) },
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
                is ClassEffect.NavigateToLocal -> navigator.push(effect.pushScreen)
                is ClassEffect.NavigateToBack -> navigator.nestedPop()
                is ClassEffect.ShowError -> {
                    snackbarState.showSnackbar(
                        message = effect.failures.mapToMessage(strings, coreStrings),
                        withDismissAction = true,
                    )
                }
            }
        }
    }
}