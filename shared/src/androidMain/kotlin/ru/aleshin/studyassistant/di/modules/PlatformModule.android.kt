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

package ru.aleshin.studyassistant.di.modules

import android.content.Context
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.common.messages.RemoteMessageHandler
import ru.aleshin.studyassistant.core.common.platform.services.AnalyticsService
import ru.aleshin.studyassistant.core.common.platform.services.AppService
import ru.aleshin.studyassistant.core.common.platform.services.CrashlyticsService
import ru.aleshin.studyassistant.core.common.platform.services.MessagingService
import ru.aleshin.studyassistant.di.PlatformConfiguration
import ru.aleshin.studyassistant.presentation.services.RemoteMessageHandlerImpl

/**
 * @author Stanislav Aleshin on 24.04.2024.
 */
actual val platformModule = DI.Module("PlatformModule") {
    bindSingleton<Context> { instance<PlatformConfiguration>().applicationContext }
    bindSingleton<AppService> { instance<PlatformConfiguration>().appService }
    bindSingleton<AnalyticsService> { instance<PlatformConfiguration>().analyticsService }
    bindSingleton<MessagingService> { instance<PlatformConfiguration>().messagingService }
    bindSingleton<CrashlyticsService> { instance<PlatformConfiguration>().crashlyticsService }
    bindSingleton<RemoteMessageHandler> { RemoteMessageHandlerImpl(instance()) }
}