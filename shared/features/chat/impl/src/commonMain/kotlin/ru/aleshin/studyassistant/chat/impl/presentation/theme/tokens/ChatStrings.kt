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

package ru.aleshin.studyassistant.chat.impl.presentation.theme.tokens

import androidx.compose.runtime.staticCompositionLocalOf
import ru.aleshin.studyassistant.core.ui.theme.tokens.StudyAssistantLanguage

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
internal data class ChatStrings(
    val assistantTopBarTitle: String,
    val chatAssistantErrorMessage: String,
    val functionalChatSuggestion: String,
    val homeworkChatSuggestion: String,
    val assistantChatTextFieldPlaceholder: String,
    val assistantEmptyChatTitle: String,
    val otherErrorMessage: String,
) {
    companion object Companion {
        val RUSSIAN = ChatStrings(
            assistantTopBarTitle = "Ассистент (Beta)",
            chatAssistantErrorMessage = "Ошибка при работе ассистента",
            assistantChatTextFieldPlaceholder = "Отправьте любой ваш запрос",
            functionalChatSuggestion = "Что ты умеешь?",
            homeworkChatSuggestion = "Что задали на завтра?",
            assistantEmptyChatTitle = "Чем я пому помочь вам?",
            otherErrorMessage = "Ошибка! Обратитесь к разработчику!",
        )
        val ENGLISH = ChatStrings(
            assistantTopBarTitle = "Assistant (Beta)",
            chatAssistantErrorMessage = "Error in assistant work",
            assistantChatTextFieldPlaceholder = "Enter your query",
            functionalChatSuggestion = "What can you do?",
            homeworkChatSuggestion = "What's the assignment for tomorrow?",
            assistantEmptyChatTitle = "How can I help you?",
            otherErrorMessage = "Error! Contact the developer!",
        )
    }
}

internal val LocalChatStrings = staticCompositionLocalOf<ChatStrings> {
    error("Editor Strings is not provided")
}

internal fun fetchChatStrings(language: StudyAssistantLanguage) = when (language) {
    StudyAssistantLanguage.EN -> ChatStrings.ENGLISH
    StudyAssistantLanguage.RU -> ChatStrings.RUSSIAN
}