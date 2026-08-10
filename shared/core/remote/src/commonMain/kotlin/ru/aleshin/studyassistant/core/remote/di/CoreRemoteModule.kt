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
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.remote.api.ai.AiRemoteApi
import ru.aleshin.studyassistant.core.remote.api.ai.SharedAiRemoteApi
import ru.aleshin.studyassistant.core.remote.datasources.ai.AiAssistantRemoteDataSource
import ru.aleshin.studyassistant.core.remote.datasources.share.HomeworkShareRemoteDataSource
import ru.aleshin.studyassistant.core.remote.datasources.share.ScheduleShareRemoteDataSource
import ru.aleshin.studyassistant.core.remote.ktor.HttpEngineFactory
import ru.aleshin.studyassistant.core.remote.ktor.StudyAssistantKtor.DeepSeek

/**
 * @author Stanislav Aleshin on 01.08.2024.
 */
@OptIn(ExperimentalSerializationApi::class)
val coreRemoteModule = DI.Module("CoreRemote") {
    import(coreRemotePlatformModule)

    bindSingleton<Settings> { Settings() }
    bindSingleton<Json> {
        Json {
            isLenient = true
            ignoreUnknownKeys = true
            useAlternativeNames = false
            namingStrategy = JsonNamingStrategy.SnakeCase
        }
    }
    bindSingleton<Json>(tag = "Functions") {
        Json {
            isLenient = true
            ignoreUnknownKeys = true
            useAlternativeNames = false
        }
    }

    bindSingleton<HttpEngineFactory> { HttpEngineFactory() }
    bindProvider<HttpClientEngineFactory<HttpClientEngineConfig>> {
        instance<HttpEngineFactory>().createEngine()
    }
    bindSingleton<HttpClient>(tag = "DeepSeek") {
        HttpClient(instance<HttpEngineFactory>().createEngine()) {
            defaultRequest {
                url(DeepSeek.HOST)
                contentType(ContentType.Application.Json)
            }
            install(HttpTimeout) {
                connectTimeoutMillis = 300_000
                requestTimeoutMillis = 300_000
                socketTimeoutMillis = 300_000
            }
            install(ContentNegotiation) { json(instance<Json>()) }
        }
    }

    bindSingleton<AiRemoteApi>(tag = "SharedAi") {
        SharedAiRemoteApi(instance(), instance(tag = "Functions"))
    }
    bindSingleton<AiRemoteApi>(tag = "PersonalAi") {
        AiRemoteApi.DeepSeek(instance(tag = "DeepSeek"), instance())
    }
    bindSingleton<AiAssistantRemoteDataSource> {
        AiAssistantRemoteDataSource.Base(
            sharedApi = instance(tag = "SharedAi"),
            personalApi = instance(tag = "PersonalAi"),
        )
    }

    bindProvider<ScheduleShareRemoteDataSource> {
        ScheduleShareRemoteDataSource.Base(instance(), instance(tag = "Functions"))
    }
    bindProvider<HomeworkShareRemoteDataSource> {
        HomeworkShareRemoteDataSource.Base(instance(), instance(tag = "Functions"))
    }
}
