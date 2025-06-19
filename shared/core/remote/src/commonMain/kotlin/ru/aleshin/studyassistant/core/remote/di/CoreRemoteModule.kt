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

import com.aallam.openai.api.http.Timeout
import com.aallam.openai.client.LoggingConfig
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIHost
import com.aallam.openai.client.RetryStrategy
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.storage.FirebaseStorage
import dev.gitlive.firebase.storage.storage
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.common.functional.Constants.App.LOGGER_TAG
import ru.aleshin.studyassistant.core.remote.BuildKonfig
import ru.aleshin.studyassistant.core.remote.datasources.auth.AuthRemoteDataSource
import ru.aleshin.studyassistant.core.remote.datasources.billing.ProductsRemoteDataSource
import ru.aleshin.studyassistant.core.remote.datasources.billing.SubscriptionChecker
import ru.aleshin.studyassistant.core.remote.datasources.employee.EmployeeRemoteDataSource
import ru.aleshin.studyassistant.core.remote.datasources.goals.DailyGoalsRemoteDataSource
import ru.aleshin.studyassistant.core.remote.datasources.message.MessageRemoteDataSource
import ru.aleshin.studyassistant.core.remote.datasources.message.PushServiceAuthTokenFactory
import ru.aleshin.studyassistant.core.remote.datasources.message.PushServiceAuthTokenProvider
import ru.aleshin.studyassistant.core.remote.datasources.organizations.OrganizationsRemoteDataSource
import ru.aleshin.studyassistant.core.remote.datasources.requests.FriendRequestsRemoteDataSource
import ru.aleshin.studyassistant.core.remote.datasources.schedules.BaseScheduleRemoteDataSource
import ru.aleshin.studyassistant.core.remote.datasources.schedules.CustomScheduleRemoteDataSource
import ru.aleshin.studyassistant.core.remote.datasources.settings.CalendarSettingsRemoteDataSource
import ru.aleshin.studyassistant.core.remote.datasources.share.ShareHomeworksRemoteDataSource
import ru.aleshin.studyassistant.core.remote.datasources.share.ShareSchedulesRemoteDataSource
import ru.aleshin.studyassistant.core.remote.datasources.subjects.SubjectsRemoteDataSource
import ru.aleshin.studyassistant.core.remote.datasources.tasks.HomeworksRemoteDataSource
import ru.aleshin.studyassistant.core.remote.datasources.tasks.TodoRemoteDataSource
import ru.aleshin.studyassistant.core.remote.datasources.users.UsersRemoteDataSource
import ru.aleshin.studyassistant.core.remote.ktor.HttpEngineFactory
import kotlin.time.Duration.Companion.seconds

/**
 * @author Stanislav Aleshin on 01.08.2024.
 */
val coreRemoteModule = DI.Module("CoreRemote") {
    import(coreRemotePlatformModule)

    bindProvider<FirebaseAuth> { Firebase.auth }
    bindProvider<FirebaseFirestore> { Firebase.firestore }
    bindProvider<FirebaseStorage> { Firebase.storage }

    bindSingleton<Json> {
        Json {
            isLenient = true
            ignoreUnknownKeys = true
            useAlternativeNames = false
        }
    }
    bindSingleton<HttpEngineFactory> { HttpEngineFactory() }
    bindSingleton<HttpClient> {
        HttpClient(instance<HttpEngineFactory>().createEngine()) {
            install(Logging) {
                level = if (BuildKonfig.IS_DEBUG) LogLevel.ALL else LogLevel.NONE
                logger = object : Logger {
                    override fun log(message: String) {
                        co.touchlab.kermit.Logger.i(LOGGER_TAG) { message }
                    }
                }
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 3000
                socketTimeoutMillis = 3000
                connectTimeoutMillis = 3000
            }

            install(ContentNegotiation) {
                json(instance<Json>())
            }
        }
    }
    bindSingleton<OpenAI> {
        OpenAI(
            token = BuildKonfig.CHAT_GPT_KEY,
            logging = LoggingConfig(),
            timeout = Timeout(socket = 30.seconds),
            organization = null,
            headers = emptyMap(),
            host = OpenAIHost.OpenAI,
            proxy = null,
            retry = RetryStrategy(),
        )
    }

    bindSingleton<SubscriptionChecker> { SubscriptionChecker.Base(instance(), instance(), instance()) }
    bindSingleton<AuthRemoteDataSource> { AuthRemoteDataSource.Base(instance(), instance()) }
    bindSingleton<UsersRemoteDataSource> { UsersRemoteDataSource.Base(instance(), instance(), instance(), instance()) }
    bindSingleton<CalendarSettingsRemoteDataSource> { CalendarSettingsRemoteDataSource.Base(instance()) }
    bindSingleton<FriendRequestsRemoteDataSource> { FriendRequestsRemoteDataSource.Base(instance()) }
    bindSingleton<ShareHomeworksRemoteDataSource> { ShareHomeworksRemoteDataSource.Base(instance()) }
    bindSingleton<ShareSchedulesRemoteDataSource> { ShareSchedulesRemoteDataSource.Base(instance()) }
    bindSingleton<BaseScheduleRemoteDataSource> { BaseScheduleRemoteDataSource.Base(instance()) }
    bindSingleton<CustomScheduleRemoteDataSource> { CustomScheduleRemoteDataSource.Base(instance()) }
    bindSingleton<SubjectsRemoteDataSource> { SubjectsRemoteDataSource.Base(instance()) }
    bindSingleton<EmployeeRemoteDataSource> { EmployeeRemoteDataSource.Base(instance(), instance()) }
    bindSingleton<HomeworksRemoteDataSource> { HomeworksRemoteDataSource.Base(instance()) }
    bindSingleton<DailyGoalsRemoteDataSource> { DailyGoalsRemoteDataSource.Base(instance()) }
    bindSingleton<TodoRemoteDataSource> { TodoRemoteDataSource.Base(instance()) }
    bindSingleton<OrganizationsRemoteDataSource> { OrganizationsRemoteDataSource.Base(instance(), instance()) }
    bindSingleton<ProductsRemoteDataSource> { ProductsRemoteDataSource.Base(instance()) }
    bindSingleton<MessageRemoteDataSource> { MessageRemoteDataSource.Base(instance(), instance(), instance(), instance()) }

    bindProvider<PushServiceAuthTokenFactory> { PushServiceAuthTokenFactory.Base(instance(), instance(), instance()) }
    bindProvider<PushServiceAuthTokenProvider.Firebase> { PushServiceAuthTokenProvider.Firebase(instance()) }
    bindProvider<PushServiceAuthTokenProvider.RuStore> { PushServiceAuthTokenProvider.RuStore() }
    bindProvider<PushServiceAuthTokenProvider.Huawei> { PushServiceAuthTokenProvider.Huawei(instance()) }
}