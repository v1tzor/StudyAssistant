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

package ru.aleshin.studyassistant.info.impl.presentation.ui.employee.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.info.impl.presentation.ui.theme.InfoThemeRes

/**
 * @author Stanislav Aleshin on 18.06.2024.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun EmployeeSearchTopBar(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    onBackPress: () -> Unit,
    onSearch: (String) -> Unit,
    searchInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val focusManager = LocalFocusManager.current
    val isFocus = searchInteractionSource.collectIsFocusedAsState().value
    var query by rememberSaveable { mutableStateOf("") }

    SearchBar(
        query = query,
        onQueryChange = {
            query = it
            onSearch(it)
        },
        onSearch = {
            focusManager.clearFocus()
            onSearch(it)
        },
        active = false,
        onActiveChange = {},
        modifier = modifier.fillMaxWidth().padding(top = 8.dp, start = 16.dp, end = 16.dp),
        placeholder = {
            Text(text = InfoThemeRes.strings.employeeSearchBarPlaceholder)
        },
        leadingIcon = {
            IconButton(onClick = onBackPress) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = StudyAssistantRes.strings.backIconDesc,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
        trailingIcon = {
            AnimatedVisibility(
                visible = isFocus,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
            ) {
                IconButton(
                    onClick = {
                        query = ""
                        onSearch("")
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
        content = {},
    )

    if (isLoading) {
        LaunchedEffect(true) { if (query.isNotBlank()) { query = "" } }
    }
}