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

package ru.aleshin.studyassistant.core.ui.views

import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.core.domain.entities.users.Gender
import ru.aleshin.studyassistant.core.ui.mappers.mapToSting
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes

/**
 * @author Stanislav Aleshin on 27.04.2024.
 */
@Composable
fun GenderDropdownMenu(
    modifier: Modifier = Modifier,
    isExpanded: Boolean,
    selected: Gender,
    onDismiss: () -> Unit,
    onSelect: (Gender) -> Unit,
) {
    DropdownMenu(
        expanded = isExpanded,
        onDismissRequest = onDismiss,
        modifier = modifier.sizeIn(maxHeight = 200.dp).clip(MaterialTheme.shapes.large),
        shape = MaterialTheme.shapes.large,
        offset = DpOffset(0.dp, 6.dp),
    ) {
        Gender.entries.forEach { gender ->
            val isSelected = gender == selected
            DropdownMenuItem(
                modifier = modifier.alpha(if (!isSelected) 1f else 0.6f),
                onClick = { onSelect(gender) },
                enabled = !isSelected,
                text = {
                    Text(
                        text = gender.mapToSting(StudyAssistantRes.strings),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
            )
        }
    }
}