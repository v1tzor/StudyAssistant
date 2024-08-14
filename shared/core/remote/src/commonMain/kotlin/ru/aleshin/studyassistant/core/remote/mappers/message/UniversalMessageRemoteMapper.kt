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

package ru.aleshin.studyassistant.core.remote.mappers.message

import ru.aleshin.studyassistant.core.common.messages.PushServiceType
import ru.aleshin.studyassistant.core.remote.models.message.PushProviderPojo
import ru.aleshin.studyassistant.core.remote.models.message.UniversalMessageData
import ru.aleshin.studyassistant.core.remote.models.message.UniversalPushMessagePojo
import ru.aleshin.studyassistant.core.remote.models.message.UniversalPushProvidersPojo
import ru.aleshin.studyassistant.core.remote.models.message.UniversalPushTokensPojo

/**
 * @author Stanislav Aleshin on 11.08.2024.
 */
suspend fun UniversalMessageData.mapToRemote(
    providerMapper: suspend (PushServiceType) -> PushProviderPojo,
) = UniversalPushMessagePojo(
    providers = if (tokens != null) {
        UniversalPushProvidersPojo(
            rustore = if (tokens.containsKey(PushServiceType.RUSTORE)) {
                providerMapper(PushServiceType.RUSTORE)
            } else {
                null
            },
            fcm = if (tokens.containsKey(PushServiceType.FCM)) {
                providerMapper(PushServiceType.FCM)
            } else {
                null
            },
            hms = if (tokens.containsKey(PushServiceType.HMS)) {
                providerMapper(PushServiceType.HMS)
            } else {
                null
            },
            apns = if (tokens.containsKey(PushServiceType.APNS)) {
                providerMapper(PushServiceType.APNS)
            } else {
                null
            },
        )
    } else if (topic != null) {
        UniversalPushProvidersPojo(
            rustore = providerMapper(PushServiceType.RUSTORE),
            fcm = providerMapper(PushServiceType.FCM),
            hms = providerMapper(PushServiceType.HMS),
            apns = providerMapper(PushServiceType.APNS),
        )
    } else {
        throw IllegalArgumentException("Require not empty tokens or topic")
    },
    tokens = if (tokens != null) {
        UniversalPushTokensPojo(
            rustore = tokens[PushServiceType.RUSTORE]?.filterNotNull()?.takeIf { it.isNotEmpty() },
            fcm = tokens[PushServiceType.FCM]?.filterNotNull()?.takeIf { it.isNotEmpty() },
            hms = tokens[PushServiceType.HMS]?.filterNotNull()?.takeIf { it.isNotEmpty() },
            apns = tokens[PushServiceType.APNS]?.filterNotNull()?.takeIf { it.isNotEmpty() },
        )
    } else {
        null
    },
    topic = topic,
    message = message,
)