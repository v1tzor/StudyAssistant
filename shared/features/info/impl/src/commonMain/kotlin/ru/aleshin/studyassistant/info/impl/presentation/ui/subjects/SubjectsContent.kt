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

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.core.common.architecture.store.compose.handleEffects
import ru.aleshin.studyassistant.core.common.architecture.store.compose.stateAsState
import ru.aleshin.studyassistant.core.common.extensions.floatSpring
import ru.aleshin.studyassistant.core.common.functional.Constants.Placeholder
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.core.ui.views.PlaceholderBox
import ru.aleshin.studyassistant.info.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.info.impl.presentation.ui.subjects.contract.SubjectsEffect
import ru.aleshin.studyassistant.info.impl.presentation.ui.subjects.contract.SubjectsEvent
import ru.aleshin.studyassistant.info.impl.presentation.ui.subjects.contract.SubjectsState
import ru.aleshin.studyassistant.info.impl.presentation.ui.subjects.store.SubjectsComponent
import ru.aleshin.studyassistant.info.impl.presentation.ui.subjects.views.DetailsSubjectViewItem
import ru.aleshin.studyassistant.info.impl.presentation.ui.subjects.views.SubjectFiltersView
import ru.aleshin.studyassistant.info.impl.presentation.ui.subjects.views.SubjectsSearchTopBar
import ru.aleshin.studyassistant.info.impl.presentation.ui.theme.InfoThemeRes

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
    val strings = InfoThemeRes.strings
    val snackbarState = remember { SnackbarHostState() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        content = { paddingValues ->
            BaseSubjectsContent(
                state = state,
                modifier = Modifier.padding(paddingValues),
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
                    }
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
                    message = effect.failures.mapToMessage(strings),
                    withDismissAction = true,
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun BaseSubjectsContent(
    state: SubjectsState,
    modifier: Modifier,
    gridState: LazyStaggeredGridState = rememberLazyStaggeredGridState(),
    onEditSubject: (UID) -> Unit,
    onDeleteSubject: (UID) -> Unit,
) = with(state) {
    Crossfade(
        modifier = modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
        targetState = isLoading,
        animationSpec = floatSpring(),
    ) { loading ->
        if (loading) {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Adaptive(160.dp),
                modifier = Modifier.fillMaxSize(),
                verticalItemSpacing = 12.dp,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(Placeholder.EMPLOYEES_OR_SUBJECTS) {
                    PlaceholderBox(
                        modifier = Modifier.fillMaxWidth().height(180.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = MaterialTheme.shapes.large,
                    )
                }
            }
        } else if (subjects.isNotEmpty()) {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Adaptive(160.dp),
                modifier = Modifier.fillMaxSize(),
                state = gridState,
                verticalItemSpacing = 12.dp,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(subjects, key = { it.uid }) { subject ->
                    DetailsSubjectViewItem(
                        modifier = Modifier.animateItem(),
                        eventType = subject.eventType,
                        office = subject.office,
                        color = Color(subject.color),
                        name = subject.name,
                        teacher = subject.teacher,
                        location = subject.location,
                        onEdit = { onEditSubject(subject.uid) },
                        onDelete = { onDeleteSubject(subject.uid) },
                    )
                }
            }
        } else {
            Text(
                modifier = Modifier.fillMaxSize(),
                text = StudyAssistantRes.strings.noResultTitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}