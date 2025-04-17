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

package ru.aleshin.studyassistant.core.remote.datasources.message

import android.content.Context
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.callbackFlow
import ru.aleshin.studyassistant.core.common.messages.PushServiceType
import ru.aleshin.studyassistant.core.common.messages.UniversalPushToken
import ru.aleshin.studyassistant.core.common.platform.services.MessagingService
import ru.rustore.sdk.universalpush.RuStoreUniversalPushClient
import ru.rustore.sdk.universalpush.UNIVERSAL_FCM_PROVIDER
import ru.rustore.sdk.universalpush.UNIVERSAL_HMS_PROVIDER
import ru.rustore.sdk.universalpush.UNIVERSAL_RUSTORE_PROVIDER
import ru.rustore.sdk.universalpush.listener.OnNewTokenListener
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * @author Stanislav Aleshin on 07.08.2024.
 */
actual class MessagingServiceImpl(
    private val context: Context,
) : MessagingService {

    private val pushClient = RuStoreUniversalPushClient

    override fun fetchAvailablePushServices(): List<PushServiceType> {
        return buildList {
            val services = pushClient.checkAvailability(context).await()
            if (services[UNIVERSAL_FCM_PROVIDER] == true) add(PushServiceType.FCM)
            if (services[UNIVERSAL_RUSTORE_PROVIDER] == true) add(PushServiceType.RUSTORE)
            if (services[UNIVERSAL_HMS_PROVIDER] == true) add(PushServiceType.HMS)
            if (isEmpty()) add(PushServiceType.NONE)
        }
    }

    override suspend fun fetchToken() = callbackFlow {
        val currentToken = suspendCoroutine { continuation ->
            pushClient.getTokens().addOnSuccessListener { tokens ->
                val fcmToken = tokens[UNIVERSAL_FCM_PROVIDER].takeIf { it?.isNotBlank() == true }
                val rustoreToken = tokens[UNIVERSAL_RUSTORE_PROVIDER].takeIf { it?.isNotBlank() == true }
                val hmsToken = tokens[UNIVERSAL_HMS_PROVIDER].takeIf { it?.isNotBlank() == true }
                val universalPushToken = when {
                    rustoreToken != null -> UniversalPushToken.RuStore(rustoreToken)
                    hmsToken != null -> UniversalPushToken.HMS(hmsToken)
                    fcmToken != null -> UniversalPushToken.FCM(fcmToken)
                    else -> UniversalPushToken.None
                }
                continuation.resume(universalPushToken)
            }.addOnFailureListener { error ->
                continuation.resumeWithException(error)
            }
        }
        val newTokenListener = OnNewTokenListener { providerType, token ->
            if (token.isNotBlank()) {
                val universalPushToken = when (providerType) {
                    UNIVERSAL_RUSTORE_PROVIDER -> UniversalPushToken.RuStore(token)
                    UNIVERSAL_HMS_PROVIDER -> UniversalPushToken.HMS(token)
                    UNIVERSAL_FCM_PROVIDER -> UniversalPushToken.FCM(token)
                    else -> UniversalPushToken.None
                }
                trySendBlocking(universalPushToken)
            }
        }
        trySendBlocking(currentToken)
        pushClient.setOnNewTokenListener(newTokenListener)
        awaitClose()
    }

    override suspend fun deleteToken() {
        pushClient.getTokens().addOnSuccessListener { tokens ->
            pushClient.deleteTokens(tokens)
        }
    }
}