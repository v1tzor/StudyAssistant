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

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.chat.impl.presentation.models.ai.ResponseStatus
import ru.aleshin.studyassistant.chat.impl.presentation.theme.ChatThemeRes
import ru.aleshin.studyassistant.core.common.extensions.pxToDp
import ru.aleshin.studyassistant.core.ui.theme.material.full
import ru.aleshin.studyassistant.core.ui.theme.material.topSide
import ru.aleshin.studyassistant.core.ui.views.VoiceInputButton

/**
 * @author Stanislav Aleshin on 22.06.2025.
 */
@Composable
internal fun AssistantBottomBar(
    modifier: Modifier = Modifier,
    isLoadingChat: Boolean,
    responseStatus: ResponseStatus,
    isQuotaExpired: Boolean,
    userQuery: String,
    onUpdateUserQuery: (String) -> Unit,
    onSendMessage: (String) -> Unit,
) {
    var textFieldState by remember {
        mutableStateOf(TextFieldValue(userQuery))
    }
    Box(
        modifier = modifier
            .animateContentSize(spring(stiffness = Spring.StiffnessHigh))
            .clip(MaterialTheme.shapes.extraLarge.topSide)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .bottomBarPadding(),
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = textFieldState,
                singleLine = true,
                onValueChange = {
                    textFieldState = it
                    onUpdateUserQuery(it.text)
                },
                shape = MaterialTheme.shapes.full,
                placeholder = {
                    Text(
                        text = ChatThemeRes.strings.assistantChatTextFieldPlaceholder,
                        maxLines = 1,
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    focusedBorderColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ),
                trailingIcon = {
                    VoiceInputButton(onResult = { textFieldState = TextFieldValue(it) })
                }
            )
            IconButton(
                modifier = Modifier.size(32.dp),
                enabled = !isLoadingChat &&
                    responseStatus == ResponseStatus.SUCCESS &&
                    !isQuotaExpired &&
                    textFieldState.text.isNotBlank(),
                onClick = {
                    onSendMessage(textFieldState.text)
                    textFieldState = TextFieldValue()
                    onUpdateUserQuery("")
                },
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null)
            }
        }
    }
}

@Composable
private fun Modifier.bottomBarPadding(): Modifier {
    val density = LocalDensity.current
    val imeHeight = WindowInsets.ime.getBottom(density).pxToDp(density)
    val bottomBarHeight = 70.dp + WindowInsets.navigationBars.getBottom(density).pxToDp(density)
    val bottomPadding = remember(imeHeight, bottomBarHeight) {
        if (imeHeight != 0.dp && imeHeight - bottomBarHeight > 0.dp) {
            imeHeight - bottomBarHeight
        } else {
            0.dp
        }
    }
    return padding(bottom = bottomPadding)
}