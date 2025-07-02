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

import ru.aleshin.studyassistant.core.remote.appwrite.databases.AppwriteRealtime
import ru.aleshin.studyassistant.core.remote.appwrite.databases.RealtimeResponseEvent
import ru.aleshin.studyassistant.core.remote.appwrite.databases.RealtimeSubscription
import ru.aleshin.studyassistant.core.remote.appwrite.databases.mapData
import kotlin.reflect.KClass

/**
 * @author Stanislav Aleshin on 30.06.2025.
 */
abstract class AppwriteRealtimeApple : AppwriteRealtime {

    override suspend fun <T : Any> subscribe(
        channels: List<String>,
        payloadType: KClass<T>,
        callback: (RealtimeResponseEvent<T>) -> Unit
    ): RealtimeSubscription {
        val anyCallback: (RealtimeResponseEvent<Any>) -> Unit = { response ->
            callback(response.mapData(nestedType = payloadType))
        }
        val realtime = subscribe(channels, callback = anyCallback)

        return realtime
    }
}