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

package ru.aleshin.studyassistant.core.data.repositories

import kotlinx.coroutines.flow.Flow
import ru.aleshin.studyassistant.core.common.messages.UniversalPushToken
import ru.aleshin.studyassistant.core.data.mappers.message.mapToData
import ru.aleshin.studyassistant.core.domain.entities.message.Message
import ru.aleshin.studyassistant.core.domain.repositories.MessageRepository
import ru.aleshin.studyassistant.core.remote.api.message.MessageRemoteApi

/**
 * @author Stanislav Aleshin on 05.08.2024.
 */
class MessageRepositoryImpl(
    private val messageApi: MessageRemoteApi,
) : MessageRepository {

    override suspend fun fetchToken(): Flow<UniversalPushToken> {
        return messageApi.fetchToken()
    }

    override suspend fun sendMessage(message: Message) {
        messageApi.sendMessage(message.mapToData())
    }

    override suspend fun deleteToken() {
        messageApi.deleteToken()
    }
}