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

package ru.aleshin.studyassistant.chat.impl.presentation.ui.assistant

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownTypography
import com.mikepenz.markdown.model.markdownAnimations
import com.mikepenz.markdown.model.rememberMarkdownState
import kotlinx.coroutines.launch
import ru.aleshin.studyassistant.chat.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.chat.impl.presentation.models.ai.AiChatHistoryUi
import ru.aleshin.studyassistant.chat.impl.presentation.models.ai.AssistantMessageUi
import ru.aleshin.studyassistant.chat.impl.presentation.models.ai.ChatSuggestions
import ru.aleshin.studyassistant.chat.impl.presentation.models.ai.ResponseStatus
import ru.aleshin.studyassistant.chat.impl.presentation.models.ai.UserMessageUi
import ru.aleshin.studyassistant.chat.impl.presentation.theme.ChatThemeRes
import ru.aleshin.studyassistant.chat.impl.presentation.ui.assistant.contract.AssistantEffect
import ru.aleshin.studyassistant.chat.impl.presentation.ui.assistant.contract.AssistantEvent
import ru.aleshin.studyassistant.chat.impl.presentation.ui.assistant.contract.AssistantState
import ru.aleshin.studyassistant.chat.impl.presentation.ui.assistant.store.AssistantComponent
import ru.aleshin.studyassistant.chat.impl.presentation.ui.assistant.views.AssistantBottomBar
import ru.aleshin.studyassistant.chat.impl.presentation.ui.assistant.views.AssistantSenderBadge
import ru.aleshin.studyassistant.chat.impl.presentation.ui.assistant.views.AssistantTopBar
import ru.aleshin.studyassistant.chat.impl.presentation.ui.assistant.views.ChatSuggestionsView
import ru.aleshin.studyassistant.core.common.architecture.store.compose.handleEffects
import ru.aleshin.studyassistant.core.common.architecture.store.compose.stateAsState
import ru.aleshin.studyassistant.core.common.extensions.floatSpring
import ru.aleshin.studyassistant.core.common.functional.Constants
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.core.ui.views.PlaceholderBox
import ru.aleshin.studyassistant.core.ui.views.TypingDots

/**
 * @author Stanislav Aleshin on 20.06.2025
 */
@Composable
internal fun AssistantContent(
    assistantComponent: AssistantComponent,
    modifier: Modifier = Modifier,
) {
    val store = assistantComponent.store
    val state by store.stateAsState()
    val strings = ChatThemeRes.strings
    val snackbarState = remember { SnackbarHostState() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        content = { paddingValues ->
            BaseAssistantContent(
                state = state,
                modifier = Modifier.padding(paddingValues),
                onSendMessageSuggestion = { store.dispatchEvent(AssistantEvent.SendMessage(it)) },
                onTryAgain = { store.dispatchEvent(AssistantEvent.RetryAttempt) },
                onDeleteMessage = { store.dispatchEvent(AssistantEvent.ClearUnsendMessage) },
                onPaidFunctionClick = { store.dispatchEvent(AssistantEvent.ClickPaidFunction) },
            )
        },
        topBar = {
            AssistantTopBar(
                isVisibleClearButton = state.responseStatus != ResponseStatus.LOADING &&
                    !state.chatHistory?.messages.isNullOrEmpty(),
                onClearChatHistory = { store.dispatchEvent(AssistantEvent.ClearHistory) },
            )
        },
        bottomBar = {
            AssistantBottomBar(
                isLoadingChat = state.isLoadingChat,
                responseStatus = state.responseStatus,
                isQuotaExpired = state.isQuotaExpired,
                userQuery = state.userQuery.query,
                onUpdateUserQuery = { store.dispatchEvent(AssistantEvent.UpdateUserQuery(it)) },
                onSendMessage = { store.dispatchEvent(AssistantEvent.SendMessage(it)) },
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarState,
                snackbar = { ErrorSnackbar(it) },
            )
        },
    )

    store.handleEffects { effect ->
        when (effect) {
            is AssistantEffect.ShowError -> {
                store.dispatchEvent(AssistantEvent.StopResponseLoading)
                snackbarState.showSnackbar(
                    message = effect.failures.mapToMessage(strings),
                    withDismissAction = true,
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun BaseAssistantContent(
    state: AssistantState,
    modifier: Modifier,
    onSendMessageSuggestion: (String) -> Unit,
    onTryAgain: () -> Unit,
    onDeleteMessage: () -> Unit,
    onPaidFunctionClick: () -> Unit,
) {
    Crossfade(
        modifier = modifier.fillMaxSize(),
        targetState = state.isLoadingChat,
        label = "init loading",
        animationSpec = floatSpring(),
    ) { loading ->
        if (loading) {
            PlaceholdersAssistantChat()
        } else {
            Crossfade(
                modifier = Modifier.fillMaxSize(),
                targetState = state.chatHistory?.messages.isNullOrEmpty(),
                label = "chat",
                animationSpec = floatSpring(),
            ) { isEmptyChat ->
                if (isEmptyChat) {
                    EmptyAssistantChat(
                        isQuotaExpired = state.isQuotaExpired,
                        onSendMessageSuggestion = onSendMessageSuggestion
                    )
                } else if (state.chatHistory != null) {
                    val chatListState = rememberLazyListState()
                    AssistantChat(
                        chatListState = chatListState,
                        responseStatus = state.responseStatus,
                        isQuotaExpired = state.isQuotaExpired,
                        chatHistory = state.chatHistory,
                        onTryAgain = onTryAgain,
                        onDeleteMessage = onDeleteMessage,
                        navigateToBilling = onPaidFunctionClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyAssistantChat(
    modifier: Modifier = Modifier,
    isQuotaExpired: Boolean,
    onSendMessageSuggestion: (String) -> Unit,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = ChatThemeRes.strings.assistantEmptyChatTitle,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        ChatSuggestionsView(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
            enabled = !isQuotaExpired,
            suggestions = ChatSuggestions.entries,
            onSelectSuggestion = { onSendMessageSuggestion(it) },
        )
    }
}

@Composable
private fun AssistantChat(
    modifier: Modifier = Modifier,
    chatListState: LazyListState,
    isQuotaExpired: Boolean,
    responseStatus: ResponseStatus,
    chatHistory: AiChatHistoryUi,
    onTryAgain: () -> Unit,
    onDeleteMessage: () -> Unit,
    navigateToBilling: () -> Unit,
) {
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val messages = chatHistory.messages

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = chatListState,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        reverseLayout = true,
    ) {
        item(key = "spacer", contentType = "spacer") {
            Spacer(modifier = Modifier.height(16.dp))
        }
        if (!isQuotaExpired && responseStatus != ResponseStatus.SUCCESS) {
            item(key = "response status", contentType = responseStatus.name) {
                when (responseStatus) {
                    ResponseStatus.LOADING -> AssistantLoadingMessageItem(
                        modifier = Modifier.animateItem(placementSpec = null),
                    )
                    ResponseStatus.FAILURE -> AssistantErrorMessageItem(
                        modifier = Modifier.animateItem(placementSpec = null),
                        onTryAgain = onTryAgain,
                        onDeleteMessage = onDeleteMessage,
                    )
                    ResponseStatus.SUCCESS -> Unit
                }
            }
        } else if (isQuotaExpired) {
            item(key = "isQuotaExpired", contentType = "quota") {
                QuotaExpiredItem(
                    modifier = Modifier.animateItem(placementSpec = null),
                    navigateToBilling = navigateToBilling
                )
            }
        }
        items(messages, key = { it.id }, contentType = { it.messageType.name }) { message ->
            when (message) {
                is AssistantMessageUi -> {
                    AssistantMessageItem(
                        message = message,
                        onCopyText = {
                            val text = AnnotatedString(message.content ?: "")
                            coroutineScope.launch { clipboardManager.setText(text) }
                        },
                    )
                }
                is UserMessageUi -> {
                    UserMessageItem(
                        message = message,
                    )
                }
            }
        }
    }
}

@Composable
private fun LazyItemScope.AssistantMessageItem(
    modifier: Modifier = Modifier,
    message: AssistantMessageUi,
    onCopyText: () -> Unit,
) {
    Box(
        modifier = modifier.padding(start = 8.dp, end = 12.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Surface(
            shape = RoundedCornerShape(0.dp, 24.dp, 24.dp, 24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistantSenderBadge()
                Column {
                    val state = rememberMarkdownState(content = message.content ?: "")
                    Markdown(
                        markdownState = state,
                        modifier = Modifier.padding(start = 8.dp).fillMaxWidth(),
                        typography = markdownTypography(
                            h1 = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            h2 = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            h3 = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            h4 = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            h5 = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            h6 = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            text = MaterialTheme.typography.bodyLarge,
                            code = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
                            quote = MaterialTheme.typography.bodyLarge.plus(SpanStyle(fontStyle = FontStyle.Italic)),
                            paragraph = MaterialTheme.typography.bodyLarge,
                            ordered = MaterialTheme.typography.bodyLarge,
                            bullet = MaterialTheme.typography.bodyLarge,
                            list = MaterialTheme.typography.bodyLarge,
                            link = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                textDecoration = TextDecoration.Underline
                            ),
                        ),
                        animations = markdownAnimations { this },
                        loading = { internalModifier ->
                            Text(
                                modifier = internalModifier,
                                text = message.content ?: "",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        },
                    )
                    Box(modifier = Modifier.fillMaxWidth()) {
                        IconButton(
                            modifier = Modifier.align(Alignment.CenterEnd).size(28.dp),
                            onClick = onCopyText,
                        ) {
                            Icon(
                                modifier = Modifier.size(18.dp),
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LazyItemScope.AssistantErrorMessageItem(
    modifier: Modifier = Modifier,
    onTryAgain: () -> Unit,
    onDeleteMessage: () -> Unit,
) {
    Box(
        modifier = modifier.padding(start = 8.dp, end = 12.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Surface(
            shape = RoundedCornerShape(0.dp, 24.dp, 24.dp, 24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
        ) {
            Column(
                modifier = Modifier.width(IntrinsicSize.Min).padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistantSenderBadge(
                    background = MaterialTheme.colorScheme.surfaceContainerHigh,
                    iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    textColor = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    modifier = Modifier,
                    text = ChatThemeRes.strings.failureResponseText,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.titleSmall,
                )
                Row(
                    modifier = Modifier.width(IntrinsicSize.Max),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        modifier = Modifier.height(40.dp),
                        onClick = onTryAgain,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                    ) {
                        Text(text = ChatThemeRes.strings.tryAgainButton, maxLines = 1)
                    }
                    IconButton(
                        modifier = Modifier.size(40.dp),
                        onClick = onDeleteMessage,
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LazyItemScope.AssistantLoadingMessageItem(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.padding(start = 8.dp, end = 12.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Surface(
            shape = RoundedCornerShape(0.dp, 24.dp, 24.dp, 16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
        ) {
            Column(
                modifier = Modifier.padding(start = 8.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AssistantSenderBadge()
                TypingDots(
                    modifier = Modifier.padding(6.dp),
                    dotSize = 10.dp,
                    dotColor = MaterialTheme.colorScheme.tertiary,
                )
            }
        }
    }
}

@Composable
private fun LazyItemScope.UserMessageItem(
    modifier: Modifier = Modifier,
    message: UserMessageUi,
) {
    Box(
        modifier = modifier.padding(start = 32.dp).fillMaxWidth(),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(
                    text = message.content?.trim()?.replaceFirstChar { it.titlecase() } ?: "",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun LazyItemScope.QuotaExpiredItem(
    modifier: Modifier = Modifier,
    navigateToBilling: () -> Unit,
) {
    Box(
        modifier = modifier.padding(start = 8.dp, end = 12.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Surface(
            shape = RoundedCornerShape(0.dp, 24.dp, 24.dp, 24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistantSenderBadge()
                Text(
                    text = ChatThemeRes.strings.quotaExpiredTitle,
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = ChatThemeRes.strings.subscriptionSuggestionText,
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                FilledTonalButton(
                    modifier = Modifier.height(40.dp),
                    onClick = navigateToBilling,
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                ) {
                    Text(text = ChatThemeRes.strings.subscriptionInfoButton, maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun PlaceholdersAssistantChat(
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        reverseLayout = true,
        userScrollEnabled = false,
    ) {
        items(Constants.Placeholder.CHAT_MESSAGES) {
            Column(
                modifier = Modifier.fillParentMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PlaceholderBox(
                    modifier = Modifier.size(240.dp, 36.dp).wrapContentSize(Alignment.CenterEnd),
                    shape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                )
                PlaceholderBox(
                    modifier = Modifier.size(240.dp, 120.dp).wrapContentSize(Alignment.CenterStart),
                    shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                )
                PlaceholderBox(
                    modifier = Modifier.size(240.dp, 86.dp).wrapContentSize(Alignment.CenterEnd),
                    shape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                )
                PlaceholderBox(
                    modifier = Modifier.size(240.dp, 68.dp).wrapContentSize(Alignment.CenterStart),
                    shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                )
            }
        }
    }
}