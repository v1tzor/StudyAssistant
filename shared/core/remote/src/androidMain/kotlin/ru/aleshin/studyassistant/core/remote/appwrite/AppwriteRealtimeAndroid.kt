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

package ru.aleshin.studyassistant.core.remote.appwrite

import io.appwrite.services.Realtime
import ru.aleshin.studyassistant.core.remote.appwrite.databases.AppwriteRealtime
import ru.aleshin.studyassistant.core.remote.appwrite.databases.RealtimeResponseEvent
import ru.aleshin.studyassistant.core.remote.appwrite.databases.RealtimeSubscription
import kotlin.reflect.KClass

/**
 * @author Stanislav Aleshin on 30.06.2025.
 */
class AppwriteRealtimeAndroid(
    private val realtime: Realtime,
) : AppwriteRealtime {

    override suspend fun subscribe(
        channels: List<String>,
        callback: (RealtimeResponseEvent<Any>) -> Unit
    ): RealtimeSubscription {
        val mappedCallback: (io.appwrite.models.RealtimeResponseEvent<Any>) -> Unit = {
            callback(it.convertToCommon())
        }
        return realtime.subscribe(
            channels = channels.toTypedArray(),
            callback = mappedCallback
        ).convertToCommon()
    }

    override suspend fun <T : Any> subscribe(
        channels: List<String>,
        payloadType: KClass<T>,
        callback: (RealtimeResponseEvent<T>) -> Unit
    ): RealtimeSubscription {
        val mappedCallback: (io.appwrite.models.RealtimeResponseEvent<T>) -> Unit = {
            callback(it.convertToCommon())
        }
        return realtime.subscribe(
            channels = channels.toTypedArray(),
            payloadType = payloadType.javaObjectType,
            callback = mappedCallback
        ).convertToCommon()
    }
}