/*
 * Copyright 2026 Stanislav Aleshin
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.chat.impl.presentation.models.ai.ResponseStatus
import ru.aleshin.studyassistant.chat.impl.resources.Res
import ru.aleshin.studyassistant.chat.impl.resources.assistant_chat_text_field_placeholder
import ru.aleshin.studyassistant.chat.impl.resources.assistant_send_action_description
import ru.aleshin.studyassistant.core.ui.views.VoiceInputButton

/**
 * @author Stanislav Aleshin on 17.08.2026.
 */
@Composable
internal fun AssistantInputIsland(
    modifier: Modifier = Modifier,
    isLoadingChat: Boolean,
    responseStatus: ResponseStatus,
    isQuotaExpired: Boolean,
    isInputEnabled: Boolean,
    userQuery: String,
    onUpdateUserQuery: (String) -> Unit,
    onSendMessage: (String) -> Unit,
) {
    var textFieldState by remember {
        mutableStateOf(TextFieldValue(userQuery))
    }
    val canSend = !isLoadingChat &&
        responseStatus != ResponseStatus.LOADING &&
        !isQuotaExpired &&
        isInputEnabled &&
        textFieldState.text.isNotBlank()

    val sendMessage = {
        if (canSend) {
            onSendMessage(textFieldState.text)
            textFieldState = TextFieldValue()
            onUpdateUserQuery("")
        }
    }

    Surface(
        modifier = modifier.animateContentSize(
            animationSpec = spring(stiffness = Spring.StiffnessHigh)
        ),
        shape = AssistantIslandShape,
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 2.dp,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            VoiceInputButton(
                enabled = isInputEnabled && !isQuotaExpired,
                onResult = { spokenText ->
                    textFieldState = TextFieldValue(spokenText)
                    onUpdateUserQuery(spokenText)
                },
            )
            BasicTextField(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 10.dp),
                value = textFieldState,
                enabled = isInputEnabled,
                onValueChange = { value ->
                    textFieldState = value
                    onUpdateUserQuery(value.text)
                },
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                maxLines = ASSISTANT_ISLAND_MAX_LINES,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { sendMessage() }),
                decorationBox = { innerTextField ->
                    Box {
                        if (textFieldState.text.isEmpty()) {
                            Text(
                                text = stringResource(Res.string.assistant_chat_text_field_placeholder),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 1,
                            )
                        }
                        innerTextField()
                    }
                },
            )
            FilledIconButton(
                modifier = Modifier.size(40.dp).offset(y = (-4).dp),
                enabled = canSend,
                onClick = sendMessage,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = stringResource(Res.string.assistant_send_action_description),
                )
            }
        }
    }
}

private val AssistantIslandShape = RoundedCornerShape(28.dp)
private const val ASSISTANT_ISLAND_MAX_LINES = 6
