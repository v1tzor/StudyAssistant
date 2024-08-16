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

package ru.aleshin.studyassistant.info.impl.presentation.ui.subjects

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import ru.aleshin.studyassistant.core.common.navigation.root
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.info.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.info.impl.presentation.ui.subjects.contract.SubjectsDeps
import ru.aleshin.studyassistant.info.impl.presentation.ui.subjects.contract.SubjectsEffect
import ru.aleshin.studyassistant.info.impl.presentation.ui.subjects.contract.SubjectsEvent
import ru.aleshin.studyassistant.info.impl.presentation.ui.subjects.contract.SubjectsViewState
import ru.aleshin.studyassistant.info.impl.presentation.ui.subjects.screenmodel.rememberSubjectsScreenModel
import ru.aleshin.studyassistant.info.impl.presentation.ui.subjects.views.SubjectFiltersView
import ru.aleshin.studyassistant.info.impl.presentation.ui.subjects.views.SubjectsSearchTopBar
import ru.aleshin.studyassistant.info.impl.presentation.ui.theme.InfoThemeRes

/**
 * @author Stanislav Aleshin on 17.06.2024
 */
internal data class SubjectsScreen(val organizationId: UID) : Screen {

    @Composable
    override fun Content() = ScreenContent(
        screenModel = rememberSubjectsScreenModel(),
        initialState = SubjectsViewState(),
        dependencies = SubjectsDeps(organizationId = organizationId),
    ) { state ->
        val strings = InfoThemeRes.strings
        val navigator = LocalNavigator.currentOrThrow
        val snackbarState = remember { SnackbarHostState() }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            content = { paddingValues ->
                SubjectsContent(
                    state = state,
                    modifier = Modifier.padding(paddingValues),
                    onEditSubject = { dispatchEvent(SubjectsEvent.NavigateToEditor(it)) },
                    onDeleteSubject = { dispatchEvent(SubjectsEvent.DeleteSubject(it)) }
                )
            },
            topBar = {
                Column {
                    SubjectsSearchTopBar(
                        isLoading = state.isLoading,
                        onBackPress = { dispatchEvent(SubjectsEvent.NavigateToBack) },
                        onSearch = { dispatchEvent(SubjectsEvent.SearchSubjects(it)) }
                    )
                    SubjectFiltersView(
                        isLoading = state.isLoading,
                        sortedType = state.sortedType,
                        selectedOrganization = state.selectedOrganization,
                        allOrganizations = state.organizations,
                        onSelectOrganization = { dispatchEvent(SubjectsEvent.SelectedOrganization(it.uid)) },
                        onSelectSortedType = { dispatchEvent(SubjectsEvent.SelectedSortedType(it)) },
                    )
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { dispatchEvent(SubjectsEvent.NavigateToEditor(null)) },
                    shape = MaterialTheme.shapes.large,
                    backgroundColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            },
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarState,
                    snackbar = { ErrorSnackbar(it) },
                )
            },
            contentWindowInsets = WindowInsets.statusBars,
        )

        handleEffect { effect ->
            when (effect) {
                is SubjectsEffect.NavigateToGlobal -> navigator.root().push(effect.pushScreen)
                is SubjectsEffect.NavigateToBack -> navigator.pop()
                is SubjectsEffect.ShowError -> {
                    snackbarState.showSnackbar(
                        message = effect.failures.mapToMessage(strings),
                        withDismissAction = true,
                    )
                }
            }
        }
    }
}