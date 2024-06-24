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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.views

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import functional.Constants.Text.TEST_TOPIC_MAX_LENGTH
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes
import theme.StudyAssistantRes
import views.InfoTextField

/**
 * @author Stanislav Aleshin on 23.06.2024.
 */
@Composable
internal fun HomeworkTestInfoField(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    testTopic: String,
    isTest: Boolean,
    onTestChange: (isTest: Boolean, topic: String) -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(start = 16.dp, end = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val focusManager = LocalFocusManager.current
        val testInteraction = remember { MutableInteractionSource() }
        var editableTestTopicTheory by remember { mutableStateOf(testTopic) }

        InfoTextField(
            enabled = !isLoading,
            modifier = Modifier.weight(1f),
            value = editableTestTopicTheory,
            maxLength = TEST_TOPIC_MAX_LENGTH,
            onValueChange = {
                editableTestTopicTheory = it
                onTestChange(isTest, it)
            },
            label = EditorThemeRes.strings.testFieldLabel,
            leadingInfoIcon = painterResource(StudyAssistantRes.icons.testsOutline),
            placeholder = { Text(text = EditorThemeRes.strings.testFieldPlaceholder) },
            trailingIcon = {
                if (testInteraction.collectIsFocusedAsState().value) {
                    IconButton(onClick = { focusManager.clearFocus() }) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = StudyAssistantRes.colors.accents.green,
                        )
                    }
                }
            },
            interactionSource = testInteraction,
        )

        Switch(
            enabled = !isLoading,
            checked = isTest,
            onCheckedChange = {
                editableTestTopicTheory = ""
                onTestChange(it, "")
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onError,
                checkedTrackColor = MaterialTheme.colorScheme.error,
                checkedIconColor = MaterialTheme.colorScheme.error,
            )
        )

        LaunchedEffect(isLoading) {
            if (editableTestTopicTheory != testTopic) editableTestTopicTheory = testTopic
        }
    }
}