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

package ru.aleshin.studyassistant.application

import android.content.Context
import ru.aleshin.studyassistant.android.BuildConfig
import ru.aleshin.studyassistant.core.common.functional.Constants
import ru.rustore.sdk.pushclient.common.logger.DefaultLogger
import ru.rustore.sdk.universalpush.RuStoreUniversalPushClient
import ru.rustore.sdk.universalpush.firebase.provides.FirebasePushProvider
import ru.rustore.sdk.universalpush.rustore.providers.RuStorePushProvider

/**
 * @author Stanislav Aleshin on 07.08.2024.
 */
interface UniversalPushClientFactory {

    fun createPushClient(): RuStoreUniversalPushClient

    class Base(private val applicationContext: Context) : UniversalPushClientFactory {
        override fun createPushClient() = RuStoreUniversalPushClient.apply {
            init(
                context = applicationContext,
                rustore = RuStorePushProvider(
                    application = checkNotNull(applicationContext as? StudyAssistantApp) {
                        "Application class was null."
                    },
                    projectId = BuildConfig.PROJECT_ID,
                    logger = DefaultLogger(tag = Constants.App.LOGGER_TAG),
                ),
                firebase = FirebasePushProvider(
                    context = applicationContext,
                )
            )
        }
    }
}