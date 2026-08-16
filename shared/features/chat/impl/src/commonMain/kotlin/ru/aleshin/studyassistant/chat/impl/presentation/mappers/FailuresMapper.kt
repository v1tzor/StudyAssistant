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

package ru.aleshin.studyassistant.chat.impl.presentation.mappers

import org.jetbrains.compose.resources.getString
import ru.aleshin.studyassistant.chat.impl.domain.entities.ChatFailures
import ru.aleshin.studyassistant.chat.impl.resources.Res
import ru.aleshin.studyassistant.chat.impl.resources.chat_assistant_error_message
import ru.aleshin.studyassistant.chat.impl.resources.invalid_request_error_message
import ru.aleshin.studyassistant.chat.impl.resources.offline_error_message
import ru.aleshin.studyassistant.chat.impl.resources.other_error_message
import ru.aleshin.studyassistant.chat.impl.resources.quota_error_message
import ru.aleshin.studyassistant.chat.impl.resources.rate_limit_error_message
import ru.aleshin.studyassistant.chat.impl.resources.reward_unavailable_error_message
import ru.aleshin.studyassistant.chat.impl.resources.server_unavailable_error_message
import ru.aleshin.studyassistant.core.domain.entities.ai.AiSettings

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
internal suspend fun ChatFailures.mapToMessage() = when (this) {
    is ChatFailures.ChatAssistantError -> getString(Res.string.chat_assistant_error_message)
    is ChatFailures.QuotaExceeded -> getString(
        Res.string.quota_error_message,
        AiSettings.DAILY_QUOTA.toString(),
    )
    is ChatFailures.InvalidRequest -> getString(Res.string.invalid_request_error_message)
    is ChatFailures.Offline -> getString(Res.string.offline_error_message)
    is ChatFailures.RateLimited -> getString(Res.string.rate_limit_error_message)
    is ChatFailures.ServerUnavailable -> getString(Res.string.server_unavailable_error_message)
    is ChatFailures.RewardUnavailable -> getString(Res.string.reward_unavailable_error_message)
    is ChatFailures.OtherError -> getString(Res.string.other_error_message)
}
