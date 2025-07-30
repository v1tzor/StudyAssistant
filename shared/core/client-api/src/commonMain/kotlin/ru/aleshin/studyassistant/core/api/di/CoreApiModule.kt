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
import ru.aleshin.studyassistant.core.api.auth.AccountService
import ru.aleshin.studyassistant.core.api.auth.AuthUserStorage
import ru.aleshin.studyassistant.core.api.client.AppwriteClient
import ru.aleshin.studyassistant.core.api.cookies.PreferencesCookiesStorage
import ru.aleshin.studyassistant.core.api.databases.DatabaseService
import ru.aleshin.studyassistant.core.api.realtime.RealtimeService
import ru.aleshin.studyassistant.core.api.storage.StorageService

/**
 * @author Stanislav Aleshin on 01.08.2024.
 */
@OptIn(ExperimentalSerializationApi::class)
val coreClintApiModule = DI.Module("CoreClientApi") {
    import(coreApiPlatformModule)

    bindSingleton<PreferencesCookiesStorage> { PreferencesCookiesStorage.Base(instance()) }

    bindSingleton<AppwriteClient> {
        val creator = AppwriteClient.Companion.Creator(
            headersProvider = instance(),
            coroutineManager = instance(),
            httpClientEngineFactory = instance(),
            cookiesStorage = instance(),
            connectionManager = instance(),
        )
        return@bindSingleton creator.setup()
    }
    bindSingleton<AccountService> { AccountService(instance(), instance(), instance(), instance()) }
    bindSingleton<StorageService> { StorageService(instance()) }
    bindSingleton<DatabaseService> { DatabaseService(instance(), instance()) }
    bindSingleton<RealtimeService> { RealtimeService(instance(), instance(), instance()) }

    bindSingleton<AuthUserStorage> { AuthUserStorage.Base(instance()) }
}