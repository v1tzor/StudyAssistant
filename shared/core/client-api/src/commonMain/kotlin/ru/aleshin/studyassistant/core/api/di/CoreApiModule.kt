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

package ru.aleshin.studyassistant.core.api.di

import kotlinx.serialization.ExperimentalSerializationApi
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.api.AppwriteApi.Client.ENDPOINT
import ru.aleshin.studyassistant.core.api.AppwriteApi.Client.ENDPOINT_REALTIME
import ru.aleshin.studyassistant.core.api.AppwriteApi.Client.PROJECT_ID
import ru.aleshin.studyassistant.core.api.auth.AccountApi
import ru.aleshin.studyassistant.core.api.client.AppwriteClient
import ru.aleshin.studyassistant.core.api.databases.DatabaseApi
import ru.aleshin.studyassistant.core.api.realtime.RealtimeApi
import ru.aleshin.studyassistant.core.api.storage.StorageApi

/**
 * @author Stanislav Aleshin on 01.08.2024.
 */
@OptIn(ExperimentalSerializationApi::class)
val coreClintApiModule = DI.Module("CoreClientApi") {
    import(coreApiPlatformModule)

    bindSingleton<AppwriteClient> {
        AppwriteClient(
            headersProvider = instance(),
            coroutineManager = instance(),
            httpClientEngineFactory = instance(),
            cookiesStorage = instance(),
            connectionManager = instance(),
        ).setEndpoint(ENDPOINT)
            .setEndpointRealtime(ENDPOINT_REALTIME)
            .setProject(PROJECT_ID)
    }
    bindSingleton<AccountApi> { AccountApi(instance()) }
    bindSingleton<StorageApi> { StorageApi(instance()) }
    bindSingleton<DatabaseApi> { DatabaseApi(instance(), instance()) }
    bindSingleton<RealtimeApi> { RealtimeApi(instance(), instance()) }
}