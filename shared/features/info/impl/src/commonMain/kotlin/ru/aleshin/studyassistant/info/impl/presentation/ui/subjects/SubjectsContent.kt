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
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.core.common.architecture.store.compose.handleEffects
import ru.aleshin.studyassistant.core.common.architecture.store.compose.stateAsState
import ru.aleshin.studyassistant.core.ui.theme.tokens.AdaptiveLayoutDefaults
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.info.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.info.impl.presentation.ui.InfoLayoutMode
import ru.aleshin.studyassistant.info.impl.presentation.ui.fetchInfoLayoutMode
import ru.aleshin.studyassistant.info.impl.presentation.ui.subjects.contract.SubjectsEffect
import ru.aleshin.studyassistant.info.impl.presentation.ui.subjects.contract.SubjectsEvent
import ru.aleshin.studyassistant.info.impl.presentation.ui.subjects.store.SubjectsComponent
import ru.aleshin.studyassistant.info.impl.presentation.ui.subjects.views.SubjectFiltersView
import ru.aleshin.studyassistant.info.impl.presentation.ui.subjects.views.SubjectsSearchTopBar

/**
 * @author Stanislav Aleshin on 17.06.2024
 */
@Composable
internal fun SubjectsContent(
    subjectsComponent: SubjectsComponent,
    modifier: Modifier = Modifier,
) {
    val store = subjectsComponent.store
    val state by store.stateAsState()
    val snackbarState = remember { SnackbarHostState() }
    val layoutMode = currentWindowAdaptiveInfoV2().fetchInfoLayoutMode()
    val chromePadding = if (layoutMode == InfoLayoutMode.EXPANDED) {
        AdaptiveLayoutDefaults.ExpandedHorizontalPadding
    } else {
        16.dp
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        content = { paddingValues ->
            SubjectsLayout(
                modifier = Modifier.padding(paddingValues),
                layoutMode = layoutMode,
                state = state,
                onEditSubject = {
                    store.dispatchEvent(SubjectsEvent.ClickEditSubject(it))
                },
                onDeleteSubject = {
                    store.dispatchEvent(SubjectsEvent.ClickDeleteSubject(it))
                }
            )
        },
        topBar = {
            Column {
                SubjectsSearchTopBar(
                    isLoading = state.isLoading,
                    onBackPress = {
                        store.dispatchEvent(SubjectsEvent.ClickBack)
                    },
                    onSearch = {
                        store.dispatchEvent(SubjectsEvent.SearchSubjects(it))
                    },
                    horizontalPadding = chromePadding,
                )
                SubjectFiltersView(
                    isLoading = state.isLoading,
                    sortedType = state.sortedType,
                    selectedOrganization = state.selectedOrganization,
                    allOrganizations = state.organizations,
                    onSelectOrganization = {
                        store.dispatchEvent(SubjectsEvent.SelectedOrganization(it.uid))
                    },
                    onSelectSortedType = {
                        store.dispatchEvent(SubjectsEvent.SelectedSortedType(it))
                    },
                    horizontalPadding = chromePadding,
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { store.dispatchEvent(SubjectsEvent.ClickEditSubject(null)) },
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

    store.handleEffects { effect ->
        when (effect) {
            is SubjectsEffect.ShowError -> {
                snackbarState.showSnackbar(
                    message = effect.failures.mapToMessage(),
                    withDismissAction = true,
                )
            }
        }
    }
}
