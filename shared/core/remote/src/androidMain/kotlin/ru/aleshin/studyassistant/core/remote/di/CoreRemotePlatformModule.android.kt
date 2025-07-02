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

package ru.aleshin.studyassistant.core.remote.di

import android.content.Context
import dev.tmapps.konnection.Konnection
import io.appwrite.Client
import io.appwrite.services.Account
import io.appwrite.services.Databases
import io.appwrite.services.Realtime
import io.appwrite.services.Storage
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.remote.appwrite.Appwrite
import ru.aleshin.studyassistant.core.remote.appwrite.AppwriteAndroid
import ru.aleshin.studyassistant.core.remote.appwrite.AppwriteAuthAndroid
import ru.aleshin.studyassistant.core.remote.appwrite.AppwriteDatabaseAndroid
import ru.aleshin.studyassistant.core.remote.appwrite.AppwriteRealtimeAndroid
import ru.aleshin.studyassistant.core.remote.appwrite.AppwriteStorageAndroid
import ru.aleshin.studyassistant.core.remote.datasources.StudyAssistantAppwrite
import ru.aleshin.studyassistant.core.remote.datasources.StudyAssistantAppwrite.Client.ENDPOINT
import ru.aleshin.studyassistant.core.remote.datasources.StudyAssistantAppwrite.Client.ENDPOINT_REALTIME
import ru.aleshin.studyassistant.core.remote.datasources.StudyAssistantAppwrite.Client.PROJECT_ID
import ru.aleshin.studyassistant.core.remote.datasources.message.GoogleAuthTokenProvider
import ru.aleshin.studyassistant.core.remote.datasources.message.GoogleAuthTokenProviderImpl
import ru.aleshin.studyassistant.core.remote.datasources.message.HmsAuthTokenProvider
import ru.aleshin.studyassistant.core.remote.datasources.message.HmsAuthTokenProviderImpl

/**
 * @author Stanislav Aleshin on 08.08.2024.
 */
actual val coreRemotePlatformModule = DI.Module("CoreRemotePlatform") {
    bindSingleton<HmsAuthTokenProvider> { HmsAuthTokenProviderImpl() }
    bindSingleton<GoogleAuthTokenProvider> { GoogleAuthTokenProviderImpl(instance()) }
    bindSingleton<AppwriteDatabaseAndroid> { AppwriteDatabaseAndroid(instance()) }
    bindSingleton<AppwriteAuthAndroid> { AppwriteAuthAndroid(instance()) }
    bindSingleton<AppwriteRealtimeAndroid> { AppwriteRealtimeAndroid(instance()) }
    bindSingleton<AppwriteStorageAndroid> { AppwriteStorageAndroid(instance()) }
    bindSingleton<Appwrite> { AppwriteAndroid(instance(), instance(), instance(), instance()) }
    bindSingleton<Databases> { Databases(instance()) }
    bindSingleton<Account> { Account(instance()) }
    bindSingleton<Realtime> { Realtime(instance()) }
    bindSingleton<Storage> { Storage(instance()) }
    bindSingleton<Konnection> { Konnection.createInstance(instance<Context>()) }
    bindSingleton<Client> {
        StudyAssistantAppwrite
        Client(instance<Context>()).apply {
            setEndpoint(ENDPOINT)
            setEndpointRealtime(ENDPOINT_REALTIME)
            setProject(PROJECT_ID)
        }
    }
}