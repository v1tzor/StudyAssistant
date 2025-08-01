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

package ru.aleshin.studyassistant.core.remote.api.message

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import ru.aleshin.studyassistant.core.common.messages.PushServiceType
import ru.aleshin.studyassistant.core.common.messages.UniversalPushToken
import ru.aleshin.studyassistant.core.common.platform.services.MessagingService

/**
 * @author Stanislav Aleshin on 07.08.2024.
 */
actual class MessagingServiceImpl(
) : MessagingService {

    actual override fun fetchAvailablePushServices(): List<PushServiceType> {
        return listOf(PushServiceType.NONE)
    }

    actual override suspend fun fetchToken(): Flow<UniversalPushToken> {
        // return flowOf(UniversalPushToken.FCM(token = firebaseMessaging.getToken()))
        return flowOf(UniversalPushToken.None)
    }

    actual override suspend fun deleteToken() {
        // firebaseMessaging.deleteToken()
    }
}