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

package ru.aleshin.studyassistant.core.remote.ktor

/**
 * @author Stanislav Aleshin on 01.08.2024.
 */
object StudyAssistantKtor {

    object UniversalMessaging {
        const val HOST = "https://vkpns-universal.rustore.ru/v1/"
        const val SEND_TOKENS = "send"
        const val SEND_TOPIC = "send/topic"
    }

    object DeepSeek {
        const val HOST = "https://api.deepseek.com"
        const val CHAT_COMPLETIONS = "chat/completions"
    }

    object ChatGpt {
        const val RESPONSES_API = "https://api.openai.com/v1/responses"
        const val MODEL = "gpt-4o-mini"
    }
}