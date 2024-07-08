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

package ru.aleshin.studyassistant.core.ui.views.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.core.common.functional.Constants
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.DialogButtons
import ru.aleshin.studyassistant.core.ui.views.DialogHeader

/**
 * @author Stanislav Aleshin on 06.06.2024.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ContactInfoEditorDialog(
    modifier: Modifier = Modifier,
    header: String,
    label: String?,
    value: String?,
    onDelete: () -> Unit,
    onConfirm: (label: String?, value: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var editableLabel by remember { mutableStateOf(label) }
    var editableValue by remember { mutableStateOf(value) }

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier.width(312.dp).wrapContentHeight(),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainer,
            shadowElevation = 4.dp,
        ) {
            Column {
                DialogHeader(header = header)
                HorizontalDivider()
                Column(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    OutlinedTextField(
                        modifier = modifier.fillMaxWidth(),
                        value = editableLabel ?: "",
                        onValueChange = { text ->
                            if (text.length <= Constants.Text.DEFAULT_MAX_TEXT_LENGTH) {
                                editableLabel = text
                            }
                        },
                        label = { Text(text = StudyAssistantRes.strings.contactInfoLabel) },
                        singleLine = true,
                        shape = MaterialTheme.shapes.large,
                    )
                    OutlinedTextField(
                        modifier = modifier.fillMaxWidth(),
                        value = editableValue ?: "",
                        onValueChange = { text ->
                            if (text.length <= Constants.Text.DEFAULT_MAX_TEXT_LENGTH) {
                                editableValue = text
                            }
                        },
                        label = { Text(text = StudyAssistantRes.strings.contactInfoValue) },
                        singleLine = true,
                        shape = MaterialTheme.shapes.large,
                    )
                }
                DialogButtons(
                    enabledConfirmFirst = editableValue?.isNotEmpty() == true,
                    enabledConfirmSecond = value != null,
                    confirmFirstTitle = StudyAssistantRes.strings.saveConfirmTitle,
                    confirmSecondTitle = StudyAssistantRes.strings.deleteConfirmTitle,
                    onCancelClick = onDismiss,
                    onConfirmFirstClick = {
                        if (editableValue != null) onConfirm(editableLabel, editableValue!!)
                    },
                    onConfirmSecondClick = onDelete,
                )
            }
        }
    }
}