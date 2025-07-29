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

import com.russhwolf.settings.Settings
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.common.functional.Constants.App.LOGGER_TAG
import ru.aleshin.studyassistant.core.remote.BuildKonfig
import ru.aleshin.studyassistant.core.remote.api.ai.AiRemoteApi
import ru.aleshin.studyassistant.core.remote.api.auth.AuthRemoteApi
import ru.aleshin.studyassistant.core.remote.api.billing.ProductsRemoteApi
import ru.aleshin.studyassistant.core.remote.api.message.HmsAuthTokenProvider
import ru.aleshin.studyassistant.core.remote.api.message.MessageRemoteApi
import ru.aleshin.studyassistant.core.remote.api.message.PushServiceAuthTokenFactory
import ru.aleshin.studyassistant.core.remote.api.message.PushServiceAuthTokenProvider
import ru.aleshin.studyassistant.core.remote.datasources.employee.EmployeeRemoteDataSource
import ru.aleshin.studyassistant.core.remote.datasources.goals.DailyGoalsRemoteDataSource
import ru.aleshin.studyassistant.core.remote.datasources.organizations.OrganizationsRemoteDataSource
import ru.aleshin.studyassistant.core.remote.datasources.requests.FriendRequestsRemoteDataSource
import ru.aleshin.studyassistant.core.remote.datasources.schedules.BaseScheduleRemoteDataSource
import ru.aleshin.studyassistant.core.remote.datasources.schedules.CustomScheduleRemoteDataSource
import ru.aleshin.studyassistant.core.remote.datasources.settings.CalendarSettingsRemoteDataSource
import ru.aleshin.studyassistant.core.remote.datasources.share.SharedHomeworksRemoteDataSource
import ru.aleshin.studyassistant.core.remote.datasources.share.SharedSchedulesRemoteDataSource
import ru.aleshin.studyassistant.core.remote.datasources.subjects.SubjectsRemoteDataSource
import ru.aleshin.studyassistant.core.remote.datasources.tasks.HomeworksRemoteDataSource
import ru.aleshin.studyassistant.core.remote.datasources.tasks.TodoRemoteDataSource
import ru.aleshin.studyassistant.core.remote.datasources.users.UsersRemoteDataSource
import ru.aleshin.studyassistant.core.remote.ktor.HttpEngineFactory
import ru.aleshin.studyassistant.core.remote.ktor.StudyAssistantKtor.DeepSeek
import ru.aleshin.studyassistant.core.remote.ktor.StudyAssistantKtor.UniversalMessaging
import kotlin.random.Random

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
    bindSingleton<HttpEngineFactory> { HttpEngineFactory() }
    bindProvider<HttpClientEngineFactory<HttpClientEngineConfig>> { instance<HttpEngineFactory>().createEngine() }
    bindSingleton<HttpClient>(tag = "HmsToken") {
        HttpClient(instance<HttpEngineFactory>().createEngine()) {
            defaultRequest {
                url(HmsAuthTokenProvider.OAUTH_URL)
                contentType(ContentType.Application.FormUrlEncoded)
            }
            install(Logging) {
                level = if (BuildKonfig.IS_DEBUG) LogLevel.ALL else LogLevel.NONE
                logger = object : Logger {
                    override fun log(message: String) {
                        co.touchlab.kermit.Logger.i(LOGGER_TAG) { message }
                    }
                }
            }
            install(HttpTimeout) {
                connectTimeoutMillis = 15_000
                requestTimeoutMillis = 15_000
                socketTimeoutMillis = 15_000
            }
            install(ContentNegotiation) { json(instance<Json>()) }
        }
    }
    bindSingleton<HttpClient>(tag = "DeepSeek") {
        HttpClient(instance<HttpEngineFactory>().createEngine()) {
            defaultRequest {
                url(DeepSeek.HOST)
                contentType(ContentType.Application.Json)
            }
            install(Auth) {
                bearer {
                    loadTokens {
                        BearerTokens(BuildKonfig.DEEP_SEEK_KEY, "")
                    }
                }
            }
            install(HttpRequestRetry) {
                maxRetries = 3
                retryIf { _, response -> !response.status.isSuccess() }
                delayMillis { retry ->
                    val delay = (retry * 0.2).toLong().coerceAtLeast(1L)
                    retry + Random.nextLong(delay)
                }
            }
            install(HttpTimeout) {
                connectTimeoutMillis = 300_000
                requestTimeoutMillis = 300_000
                socketTimeoutMillis = 300_000
            }
            install(Logging) {
                level = if (BuildKonfig.IS_DEBUG) LogLevel.ALL else LogLevel.NONE
                logger = object : Logger {
                    override fun log(message: String) {
                        co.touchlab.kermit.Logger.i(LOGGER_TAG) { message }
                    }
                }
            }
            install(ContentNegotiation) { json(instance<Json>()) }
            install(HttpCookies)
        }
    }
    bindSingleton<HttpClient>(tag = "Messages") {
        HttpClient(instance<HttpEngineFactory>().createEngine()) {
            defaultRequest {
                url(UniversalMessaging.HOST + UniversalMessaging.SEND_TOKENS)
                contentType(ContentType.Application.Json)
            }
            install(Logging) {
                level = if (BuildKonfig.IS_DEBUG) LogLevel.ALL else LogLevel.NONE
                logger = object : Logger {
                    override fun log(message: String) {
                        co.touchlab.kermit.Logger.i(LOGGER_TAG) { message }
                    }
                }
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 15000
                socketTimeoutMillis = 15000
                connectTimeoutMillis = 15000
            }
            install(ContentNegotiation) {
                json(instance<Json>())
            }
        }
    }

    bindSingleton<AiRemoteApi> { AiRemoteApi.Base(instance(tag = "DeepSeek"), instance()) }
    bindSingleton<AuthRemoteApi> { AuthRemoteApi.Base(instance()) }
    bindSingleton<ProductsRemoteApi> { ProductsRemoteApi.Base(instance()) }
    bindSingleton<MessageRemoteApi> { MessageRemoteApi.Base(instance(tag = "Messages"), instance(), instance(), instance(), instance()) }

    bindSingleton<UsersRemoteDataSource> { UsersRemoteDataSource.Base(instance(), instance(), instance(), instance(), instance(), instance()) }
    bindSingleton<CalendarSettingsRemoteDataSource> { CalendarSettingsRemoteDataSource.Base(instance(), instance(), instance()) }
    bindSingleton<FriendRequestsRemoteDataSource> { FriendRequestsRemoteDataSource.Base(instance(), instance(), instance()) }
    bindSingleton<SharedHomeworksRemoteDataSource> { SharedHomeworksRemoteDataSource.Base(instance(), instance(), instance()) }
    bindSingleton<SharedSchedulesRemoteDataSource> { SharedSchedulesRemoteDataSource.Base(instance(), instance(), instance()) }
    bindSingleton<BaseScheduleRemoteDataSource> { BaseScheduleRemoteDataSource.Base(instance(), instance(), instance(), instance()) }
    bindSingleton<CustomScheduleRemoteDataSource> { CustomScheduleRemoteDataSource.Base(instance(), instance(), instance(), instance()) }
    bindSingleton<SubjectsRemoteDataSource> { SubjectsRemoteDataSource.Base(instance(), instance(), instance(), instance()) }
    bindSingleton<EmployeeRemoteDataSource> { EmployeeRemoteDataSource.Base(instance(), instance(), instance(), instance(), instance()) }
    bindSingleton<HomeworksRemoteDataSource> { HomeworksRemoteDataSource.Base(instance(), instance(), instance(), instance()) }
    bindSingleton<DailyGoalsRemoteDataSource> { DailyGoalsRemoteDataSource.Base(instance(), instance(), instance(), instance()) }
    bindSingleton<TodoRemoteDataSource> { TodoRemoteDataSource.Base(instance(), instance(), instance(), instance()) }
    bindSingleton<OrganizationsRemoteDataSource> { OrganizationsRemoteDataSource.Base(instance(), instance(), instance(), instance(), instance()) }

    bindProvider<PushServiceAuthTokenFactory> { PushServiceAuthTokenFactory.Base(instance(), instance(), instance()) }
    bindProvider<PushServiceAuthTokenProvider.Firebase> { PushServiceAuthTokenProvider.Firebase(instance()) }
    bindProvider<PushServiceAuthTokenProvider.RuStore> { PushServiceAuthTokenProvider.RuStore() }
    bindProvider<PushServiceAuthTokenProvider.Huawei> { PushServiceAuthTokenProvider.Huawei(instance()) }
}