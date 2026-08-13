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

package ru.aleshin.studyassistant.core.remote.di

import com.russhwolf.settings.Settings
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.remote.BuildKonfig
import ru.aleshin.studyassistant.core.remote.api.ads.AdRewardRemoteApi
import ru.aleshin.studyassistant.core.remote.api.ai.AiRemoteApi
import ru.aleshin.studyassistant.core.remote.api.ai.ScheduleExtractionRemoteApi
import ru.aleshin.studyassistant.core.remote.api.installation.InstallationRemoteApi
import ru.aleshin.studyassistant.core.remote.api.share.BackendShareApi
import ru.aleshin.studyassistant.core.remote.datasources.ads.AdRewardRemoteDataSource
import ru.aleshin.studyassistant.core.remote.datasources.ai.AiAssistantRemoteDataSource
import ru.aleshin.studyassistant.core.remote.datasources.ai.ScheduleExtractionRemoteDataSource
import ru.aleshin.studyassistant.core.remote.datasources.installation.InstallationRemoteDataSource
import ru.aleshin.studyassistant.core.remote.datasources.share.HomeworkShareRemoteDataSource
import ru.aleshin.studyassistant.core.remote.datasources.share.ScheduleShareRemoteDataSource
import ru.aleshin.studyassistant.core.remote.ktor.HttpEngineFactory
import ru.aleshin.studyassistant.core.remote.ktor.NetworkConnectionChecker

/**
 * @author Stanislav Aleshin on 01.08.2024.
 */
val coreRemoteModule = DI.Module("CoreRemote") {
    import(coreRemotePlatformModule)

    bindSingleton<Settings> { Settings() }
    bindSingleton<Json>(tag = "Backend") {
        Json {
            isLenient = false
            ignoreUnknownKeys = true
            explicitNulls = false
            useAlternativeNames = false
        }
    }

    bindSingleton<HttpEngineFactory> { HttpEngineFactory() }
    bindSingleton<NetworkConnectionChecker> {
        NetworkConnectionChecker.Base(connectionManager = instance())
    }
    bindProvider<HttpClientEngineFactory<HttpClientEngineConfig>> {
        instance<HttpEngineFactory>().createEngine()
    }
    bindSingleton<HttpClient>(tag = "Backend") {
        HttpClient(instance<HttpEngineFactory>().createEngine()) {
            defaultRequest {
                url(BuildKonfig.BACKEND_BASE_URL)
                contentType(ContentType.Application.Json)
            }
            install(HttpTimeout) {
                connectTimeoutMillis = 10_000
                requestTimeoutMillis = 45_000
                socketTimeoutMillis = 45_000
            }
            install(ContentNegotiation) { json(instance<Json>(tag = "Backend")) }
        }
    }

    bindSingleton<AiRemoteApi> {
        AiRemoteApi.Backend(
            httpClient = instance(tag = "Backend"),
            connectionChecker = instance(),
            json = instance(tag = "Backend"),
        )
    }
    bindSingleton<AdRewardRemoteApi> {
        AdRewardRemoteApi.Backend(
            httpClient = instance(tag = "Backend"),
            connectionChecker = instance(),
            json = instance(tag = "Backend"),
        )
    }
    bindSingleton<AdRewardRemoteDataSource> {
        AdRewardRemoteDataSource.Base(api = instance())
    }
    bindSingleton<InstallationRemoteApi> {
        InstallationRemoteApi.Backend(
            httpClient = instance(tag = "Backend"),
            connectionChecker = instance(),
            json = instance(tag = "Backend"),
        )
    }
    bindSingleton<InstallationRemoteDataSource> {
        InstallationRemoteDataSource.Base(api = instance())
    }
    bindSingleton<AiAssistantRemoteDataSource> {
        AiAssistantRemoteDataSource.Base(api = instance())
    }
    bindSingleton<ScheduleExtractionRemoteApi> {
        ScheduleExtractionRemoteApi.Backend(
            httpClient = instance(tag = "Backend"),
            connectionChecker = instance(),
            json = instance(tag = "Backend"),
        )
    }
    bindSingleton<ScheduleExtractionRemoteDataSource> {
        ScheduleExtractionRemoteDataSource.Base(api = instance())
    }

    bindSingleton<BackendShareApi> {
        BackendShareApi(
            httpClient = instance(tag = "Backend"),
            connectionChecker = instance(),
            json = instance(tag = "Backend"),
        )
    }

    bindProvider<ScheduleShareRemoteDataSource> {
        ScheduleShareRemoteDataSource.Base(instance(), instance(tag = "Backend"))
    }
    bindProvider<HomeworkShareRemoteDataSource> {
        HomeworkShareRemoteDataSource.Base(instance(), instance(tag = "Backend"))
    }
}
