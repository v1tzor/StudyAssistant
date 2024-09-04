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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.core.domain.entities.subject.EventType
import ru.aleshin.studyassistant.core.ui.mappers.mapToString
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.theme.material.full
import ru.aleshin.studyassistant.core.ui.views.SwipeToDismissBackground
import ru.aleshin.studyassistant.core.ui.views.dialog.SelectorAddItemView
import ru.aleshin.studyassistant.core.ui.views.dialog.SelectorNotSelectedItemView
import ru.aleshin.studyassistant.core.ui.views.dialog.SelectorSwipeItemView
import ru.aleshin.studyassistant.core.ui.views.sheet.BaseSelectorBottomSheet
import ru.aleshin.studyassistant.editor.impl.presentation.models.subjects.SubjectUi
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes

/**
 * @author Stanislav Aleshin on 02.06.2024.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun SubjectSelectorBottomSheet(
    enabledAdd: Boolean,
    modifier: Modifier = Modifier,
    eventType: EventType?,
    selected: SubjectUi?,
    subjects: List<SubjectUi>,
    onAddSubject: () -> Unit,
    onEditSubject: (SubjectUi) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (SubjectUi?) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val searchInteractionSource = remember { MutableInteractionSource() }
    val isFocus = searchInteractionSource.collectIsFocusedAsState().value
    var searchQuery by rememberSaveable { mutableStateOf<String?>(null) }
    val searchedSubjects = remember(searchQuery, subjects) {
        subjects.filter { subject ->
            searchQuery == null || subject.name.contains(searchQuery ?: "", true)
        }
    }
    val subjectsByEventType = remember(searchedSubjects, eventType) {
        searchedSubjects.filter { it.eventType == eventType }
    }
    val otherSubjects = remember(searchedSubjects, eventType) {
        searchedSubjects.filter { it.eventType != eventType }.sortedBy { it.eventType }
    }
    var selectedSubject by remember { mutableStateOf(selected) }

    BaseSelectorBottomSheet(
        modifier = modifier,
        selected = selectedSubject,
        items = subjectsByEventType + otherSubjects,
        header = EditorThemeRes.strings.subjectSelectorHeader,
        title = EditorThemeRes.strings.subjectSelectorTitle,
        itemView = { subject ->
            val dismissState = rememberSwipeToDismissBoxState(
                confirmValueChange = { dismissBoxValue ->
                    when (dismissBoxValue) {
                        SwipeToDismissBoxValue.StartToEnd -> Unit
                        SwipeToDismissBoxValue.EndToStart -> onEditSubject(subject)
                        SwipeToDismissBoxValue.Settled -> Unit
                    }
                    return@rememberSwipeToDismissBoxState false
                },
                positionalThreshold = { it * .60f },
            )
            SelectorSwipeItemView(
                onClick = {
                    if (isFocus) focusManager.clearFocus()
                    selectedSubject = subject
                },
                state = dismissState,
                selected = subject.uid == selectedSubject?.uid,
                title = subject.name,
                label = subject.eventType.mapToString(StudyAssistantRes.strings),
                leadingIcon = {
                    Surface(
                        modifier = Modifier.height(IntrinsicSize.Min),
                        shape = MaterialTheme.shapes.full,
                        color = Color(subject.color),
                        content = { Box(modifier = Modifier.size(8.dp, 24.dp)) },
                    )
                },
                enableDismissFromStartToEnd = false,
                backgroundContent = {
                    SwipeToDismissBackground(
                        dismissState = dismissState,
                        endToStartContent = {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                            )
                        },
                        endToStartColor = StudyAssistantRes.colors.accents.orangeContainer,
                    )
                },
            )
        },
        addItemView = if (searchQuery == null) {
            {
                SelectorAddItemView(
                    enabled = enabledAdd,
                    onClick = onAddSubject
                )
            }
        } else {
            null
        },
        notSelectedItem = if (searchQuery == null) {
            {
                SelectorNotSelectedItemView(
                    selected = selectedSubject == null,
                    onClick = { selectedSubject = null },
                )
            }
        } else {
            null
        },
        searchBar = {
            SearchBar(
                inputField = {
                    SearchBarDefaults.InputField(
                        query = searchQuery ?: "",
                        onQueryChange = { searchQuery = it.takeIf { it.isNotBlank() } },
                        onSearch = {
                            focusManager.clearFocus()
                            searchQuery = it.takeIf { it.isNotBlank() }
                        },
                        expanded = false,
                        onExpandedChange = {},
                        enabled = true,
                        placeholder = {
                            Text(text = EditorThemeRes.strings.subjectsSearchBarPlaceholder)
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = StudyAssistantRes.strings.backIconDesc,
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        },
                        trailingIcon = {
                            AnimatedVisibility(
                                visible = isFocus,
                                enter = fadeIn() + scaleIn(),
                                exit = fadeOut() + scaleOut(),
                            ) {
                                IconButton(
                                    onClick = {
                                        searchQuery = null
                                        focusManager.clearFocus()
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = StudyAssistantRes.strings.clearSearchBarDesk,
                                        tint = MaterialTheme.colorScheme.error,
                                    )
                                }
                            }
                        },
                        interactionSource = searchInteractionSource,
                    )
                },
                expanded = false,
                onExpandedChange = {},
                modifier = Modifier.fillMaxWidth(),
                windowInsets = WindowInsets(0.dp),
                content = {},
            )
        },
        onDismissRequest = onDismiss,
        onConfirm = onConfirm,
    )
}