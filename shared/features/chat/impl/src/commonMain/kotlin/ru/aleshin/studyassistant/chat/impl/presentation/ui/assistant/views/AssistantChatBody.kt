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

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownTypography
import com.mikepenz.markdown.model.markdownAnimations
import com.mikepenz.markdown.model.parseMarkdown
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.chat.impl.presentation.models.ai.AiChatHistoryUi
import ru.aleshin.studyassistant.chat.impl.presentation.models.ai.AssistantMessageUi
import ru.aleshin.studyassistant.chat.impl.presentation.models.ai.ChatSuggestions
import ru.aleshin.studyassistant.chat.impl.presentation.models.ai.ResponseStatus
import ru.aleshin.studyassistant.chat.impl.presentation.models.ai.UserMessageUi
import ru.aleshin.studyassistant.chat.impl.resources.Res
import ru.aleshin.studyassistant.chat.impl.resources.ai_settings_button
import ru.aleshin.studyassistant.chat.impl.resources.assistant_empty_chat_title
import ru.aleshin.studyassistant.chat.impl.resources.failure_response_text
import ru.aleshin.studyassistant.chat.impl.resources.quota_expired_title
import ru.aleshin.studyassistant.chat.impl.resources.quota_reset_suggestion_text
import ru.aleshin.studyassistant.chat.impl.resources.quota_rewards_exhausted_text
import ru.aleshin.studyassistant.chat.impl.resources.try_again_button
import ru.aleshin.studyassistant.chat.impl.resources.watch_ad_reward_button
import ru.aleshin.studyassistant.core.common.extensions.floatSpring
import ru.aleshin.studyassistant.core.common.functional.Constants
import ru.aleshin.studyassistant.core.domain.entities.ai.AiSettings
import ru.aleshin.studyassistant.core.ui.views.PlaceholderBox
import ru.aleshin.studyassistant.core.ui.views.TypingDots

/**
 * @author Stanislav Aleshin on 17.08.2026.
 */
@Composable
@OptIn(ExperimentalFoundationApi::class)
internal fun AssistantChatBody(
    modifier: Modifier = Modifier,
    isLoadingChat: Boolean,
    responseStatus: ResponseStatus,
    isQuotaExpired: Boolean,
    quotaLimit: Int,
    rewardedResetsRemaining: Int,
    isRewardInProgress: Boolean,
    chatHistory: AiChatHistoryUi?,
    contentMaxWidth: Dp? = null,
    onSendMessageSuggestion: (String) -> Unit,
    onTryAgain: () -> Unit,
    onDeleteMessage: () -> Unit,
    onOpenAiSettings: () -> Unit,
    onRequestQuotaReward: () -> Unit,
    onResolveToolCall: (String, Boolean) -> Unit,
) {
    AssistantConstrainedPane(
        modifier = modifier.fillMaxSize(),
        contentMaxWidth = contentMaxWidth,
    ) {
        Crossfade(
            modifier = Modifier.fillMaxSize(),
            targetState = isLoadingChat,
            label = "init loading",
            animationSpec = floatSpring(),
        ) { loading ->
            if (loading) {
                PlaceholdersAssistantChat()
            } else {
                Crossfade(
                    modifier = Modifier.fillMaxSize(),
                    targetState = chatHistory?.messages.isNullOrEmpty(),
                    label = "chat",
                    animationSpec = floatSpring(),
                ) { isEmptyChat ->
                    if (isEmptyChat) {
                        EmptyAssistantChat(
                            suggestionsEnabled = chatHistory != null &&
                                responseStatus == ResponseStatus.SUCCESS,
                            isQuotaExpired = isQuotaExpired,
                            quotaLimit = quotaLimit,
                            rewardedResetsRemaining = rewardedResetsRemaining,
                            isRewardInProgress = isRewardInProgress,
                            onSendMessageSuggestion = onSendMessageSuggestion,
                            onRequestQuotaReward = onRequestQuotaReward,
                            onOpenAiSettings = onOpenAiSettings,
                        )
                    } else if (chatHistory != null) {
                        val chatListState = rememberLazyListState()
                        AssistantChat(
                            chatListState = chatListState,
                            responseStatus = responseStatus,
                            isQuotaExpired = isQuotaExpired,
                            quotaLimit = quotaLimit,
                            rewardedResetsRemaining = rewardedResetsRemaining,
                            isRewardInProgress = isRewardInProgress,
                            chatHistory = chatHistory,
                            onTryAgain = onTryAgain,
                            onDeleteMessage = onDeleteMessage,
                            openAiSettings = onOpenAiSettings,
                            onRequestQuotaReward = onRequestQuotaReward,
                            onResolveToolCall = onResolveToolCall,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AssistantConstrainedPane(
    modifier: Modifier = Modifier,
    contentMaxWidth: Dp?,
    content: @Composable () -> Unit,
) {
    if (contentMaxWidth == null) {
        Box(modifier = modifier) {
            content()
        }
    } else {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.TopCenter,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = contentMaxWidth)
                    .fillMaxWidth(),
            ) {
                content()
            }
        }
    }
}

@Composable
private fun EmptyAssistantChat(
    modifier: Modifier = Modifier,
    suggestionsEnabled: Boolean,
    isQuotaExpired: Boolean,
    quotaLimit: Int,
    rewardedResetsRemaining: Int,
    isRewardInProgress: Boolean,
    onSendMessageSuggestion: (String) -> Unit,
    onRequestQuotaReward: () -> Unit,
    onOpenAiSettings: () -> Unit,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.assistant_empty_chat_title),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        if (isQuotaExpired) {
            QuotaExpiredItem(
                modifier = Modifier.padding(start = 8.dp, end = 12.dp, bottom = 12.dp),
                quotaLimit = quotaLimit,
                rewardedResetsRemaining = rewardedResetsRemaining,
                isRewardInProgress = isRewardInProgress,
                openAiSettings = onOpenAiSettings,
                onRequestQuotaReward = onRequestQuotaReward,
            )
        } else {
            ChatSuggestionsView(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                enabled = suggestionsEnabled,
                suggestions = ChatSuggestions.entries,
                onSelectSuggestion = { onSendMessageSuggestion(it) },
            )
        }
    }
}

@Composable
private fun AssistantChat(
    modifier: Modifier = Modifier,
    chatListState: LazyListState,
    isQuotaExpired: Boolean,
    quotaLimit: Int,
    rewardedResetsRemaining: Int,
    isRewardInProgress: Boolean,
    responseStatus: ResponseStatus,
    chatHistory: AiChatHistoryUi,
    onTryAgain: () -> Unit,
    onDeleteMessage: () -> Unit,
    openAiSettings: () -> Unit,
    onRequestQuotaReward: () -> Unit,
    onResolveToolCall: (String, Boolean) -> Unit,
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
        items(
            items = chatHistory.pendingMutations,
            key = { confirmation -> "tool-confirmation-${confirmation.id}" },
            contentType = { "tool-confirmation" },
        ) { confirmation ->
            AiToolConfirmationCard(
                modifier = Modifier.padding(horizontal = 12.dp).animateItem(placementSpec = null),
                confirmation = confirmation,
                enabled = responseStatus != ResponseStatus.LOADING,
                onConfirm = { onResolveToolCall(confirmation.id, true) },
                onReject = { onResolveToolCall(confirmation.id, false) },
            )
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
                    quotaLimit = quotaLimit,
                    rewardedResetsRemaining = rewardedResetsRemaining,
                    isRewardInProgress = isRewardInProgress,
                    openAiSettings = openAiSettings,
                    onRequestQuotaReward = onRequestQuotaReward,
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
                    AssistantMarkdownMessage(
                        modifier = Modifier.padding(start = 8.dp).fillMaxWidth(),
                        content = message.content.orEmpty(),
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
private fun AssistantMarkdownMessage(
    modifier: Modifier = Modifier,
    content: String,
) {
    val parsedState = remember(content) { parseMarkdown(content) }
    val headingStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
    val bodyStyle = MaterialTheme.typography.bodyLarge

    Markdown(
        state = parsedState,
        modifier = modifier,
        typography = markdownTypography(
            h1 = headingStyle,
            h2 = headingStyle,
            h3 = headingStyle,
            h4 = headingStyle,
            h5 = headingStyle,
            h6 = headingStyle,
            text = bodyStyle,
            code = bodyStyle.copy(fontFamily = FontFamily.Monospace),
            quote = bodyStyle.plus(SpanStyle(fontStyle = FontStyle.Italic)),
            paragraph = bodyStyle,
            ordered = bodyStyle,
            bullet = bodyStyle,
            list = bodyStyle,
        ),
        animations = markdownAnimations { this },
        error = { internalModifier ->
            Text(
                modifier = internalModifier,
                text = content,
                style = bodyStyle,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
    )
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
                    text = stringResource(Res.string.failure_response_text),
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
                        Text(text = stringResource(Res.string.try_again_button), maxLines = 1)
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
private fun QuotaExpiredItem(
    modifier: Modifier = Modifier,
    quotaLimit: Int,
    rewardedResetsRemaining: Int,
    isRewardInProgress: Boolean,
    openAiSettings: () -> Unit,
    onRequestQuotaReward: () -> Unit,
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
                    text = stringResource(Res.string.quota_expired_title, quotaLimit),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = if (rewardedResetsRemaining > 0) {
                        stringResource(
                            Res.string.quota_reset_suggestion_text,
                            AiSettings.REWARDED_QUOTA,
                            rewardedResetsRemaining,
                            AiSettings.MAX_REWARDED_RESETS,
                        )
                    } else {
                        stringResource(
                            Res.string.quota_rewards_exhausted_text,
                            AiSettings.MAX_REWARDED_RESETS,
                        )
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (rewardedResetsRemaining > 0) {
                    FilledTonalButton(
                        modifier = Modifier.height(40.dp),
                        enabled = !isRewardInProgress,
                        onClick = onRequestQuotaReward,
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    ) {
                        if (isRewardInProgress) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 3.dp,
                            )
                        } else {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.PlayCircle,
                                    contentDescription = null,
                                )
                                Text(
                                    text = stringResource(
                                        Res.string.watch_ad_reward_button,
                                        AiSettings.REWARDED_QUOTA,
                                    ),
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                } else {
                    FilledTonalButton(
                        modifier = Modifier.height(40.dp),
                        onClick = openAiSettings,
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                    ) {
                        Text(text = stringResource(Res.string.ai_settings_button), maxLines = 1)
                    }
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
