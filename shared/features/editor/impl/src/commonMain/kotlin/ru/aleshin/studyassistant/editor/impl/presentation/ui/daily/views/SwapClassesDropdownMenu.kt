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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.daily.views

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.editor.impl.presentation.models.classes.ClassUi

/**
 * @author Stanislav Aleshin on 14.07.2024.
 */
@Composable
internal fun SwapClassesDropdownMenu(
    modifier: Modifier = Modifier,
    isExpand: Boolean,
    currentClass: ClassUi,
    allClasses: List<Pair<ClassUi, Int>>,
    onDismiss: () -> Unit,
    onSwapTo: (ClassUi) -> Unit,
    offset: DpOffset = DpOffset(0.dp, 6.dp),
) {
    DropdownMenu(
        expanded = isExpand,
        onDismissRequest = onDismiss,
        modifier = modifier.width(255.dp).sizeIn(maxHeight = 260.dp),
        offset = offset,
        shape = MaterialTheme.shapes.large,
    ) {
        allClasses.forEach { targetClass ->
            DropdownMenuItem(
                onClick = { onSwapTo(targetClass.first) },
                enabled = targetClass.first.uid != currentClass.uid,
                text = {
                    Text(
                        text = targetClass.first.subject?.name ?: StudyAssistantRes.strings.noneTitle,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                    )
                },
                leadingIcon = {
                    Text(
                        modifier = Modifier.width(24.dp),
                        text = targetClass.second.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            )
        }
    }
}