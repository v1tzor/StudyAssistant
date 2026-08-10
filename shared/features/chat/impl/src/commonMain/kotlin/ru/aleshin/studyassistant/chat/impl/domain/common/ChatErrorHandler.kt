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

package ru.aleshin.studyassistant.chat.impl.domain.common

import ru.aleshin.studyassistant.chat.impl.domain.entities.ChatFailures
import ru.aleshin.studyassistant.core.common.exceptions.InternetConnectionException
import ru.aleshin.studyassistant.core.common.handlers.ErrorHandler
import ru.aleshin.studyassistant.core.domain.entities.ai.AiServiceException

/**
 * @author Stanislav Aleshin on 27.05.2024
 */
internal interface ChatErrorHandler : ErrorHandler<ChatFailures> {
    class Base : ChatErrorHandler {
        override fun handle(throwable: Throwable) = when (throwable) {
            is AiServiceException.QuotaExceeded -> ChatFailures.QuotaExceeded
            is AiServiceException.InvalidKey -> ChatFailures.InvalidKey
            is AiServiceException.InsufficientBalance -> ChatFailures.InsufficientBalance
            is AiServiceException.RateLimited -> ChatFailures.RateLimited
            is AiServiceException.ServerUnavailable -> ChatFailures.ServerUnavailable
            is InternetConnectionException -> ChatFailures.Offline
            else -> ChatFailures.OtherError(throwable)
        }
    }
}
