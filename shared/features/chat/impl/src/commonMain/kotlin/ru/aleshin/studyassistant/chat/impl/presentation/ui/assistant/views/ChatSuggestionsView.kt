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

package ru.aleshin.studyassistant.chat.impl.presentation.ui.assistant.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.chat.impl.presentation.models.ai.ChatSuggestion

/**
 * @author Stanislav Aleshin on 22.06.2025.
 */
@Composable
internal fun ChatSuggestionsView(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    suggestions: List<ChatSuggestion>,
    onSelectSuggestion: (String) -> Unit,
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items(suggestions) { suggestion ->
            val suggestionContent = suggestion.content
            SuggestionChip(
                onClick = { onSelectSuggestion(suggestionContent) },
                label = { Text(text = suggestion.content) },
                enabled = enabled,
            )
        }
    }
}