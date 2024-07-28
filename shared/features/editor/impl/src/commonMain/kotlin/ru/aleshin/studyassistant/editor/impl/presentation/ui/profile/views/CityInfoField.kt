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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.profile.views

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.HomeWork
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.InfoTextField
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes

/**
 * @author Stanislav Aleshin on 05.06.2024.
 */
@Composable
internal fun CityInfoField(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    city: String?,
    onUpdateCity: (String?) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val cityInteraction = remember { MutableInteractionSource() }
    var editableCity by remember { mutableStateOf(city) }
    var isEditCity by remember { mutableStateOf(false) }

    InfoTextField(
        modifier = modifier.padding(start = 16.dp, end = 24.dp),
        enabled = !isLoading,
        readOnly = !isEditCity,
        value = editableCity,
        onValueChange = { editableCity = it.takeIf { it.isNotBlank() } },
        label = EditorThemeRes.strings.cityFieldLabel,
        placeholder = { Text(text = EditorThemeRes.strings.cityFieldPlaceholder) },
        leadingInfoIcon = Icons.Outlined.HomeWork,
        trailingIcon = {
            if (!isEditCity) {
                IconButton(onClick = { isEditCity = true }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                Row(modifier = Modifier.padding(end = 8.dp)) {
                    IconButton(
                        modifier = Modifier.size(32.dp),
                        onClick = {
                            onUpdateCity(editableCity)
                            focusManager.clearFocus()
                            isEditCity = false
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = StudyAssistantRes.colors.accents.green
                        )
                    }
                    IconButton(
                        modifier = Modifier.size(32.dp),
                        onClick = {
                            editableCity = city
                            focusManager.clearFocus()
                            isEditCity = false
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = StudyAssistantRes.colors.accents.red
                        )
                    }
                }
            }
        },
        interactionSource = cityInteraction,
    )

    LaunchedEffect(city) {
        if (editableCity != city) editableCity = city
    }
}