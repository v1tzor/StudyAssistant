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

package ru.aleshin.studyassistant.di

import ru.aleshin.studyassistant.core.common.platform.IosUUIDProvider
import ru.aleshin.studyassistant.core.common.platform.services.AnalyticsService
import ru.aleshin.studyassistant.core.common.platform.services.AppService
import ru.aleshin.studyassistant.core.common.platform.services.CrashlyticsService
import ru.aleshin.studyassistant.core.common.platform.services.MessagingService
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapService
import ru.aleshin.studyassistant.core.remote.appwrite.AppwriteApple
import ru.aleshin.studyassistant.core.remote.datasources.message.MessagingServiceImpl

/**
 * @author Stanislav Aleshin on 14.04.2024.
 */
actual data class PlatformConfiguration(
    actual val appService: AppService,
    actual val analyticsService: AnalyticsService,
    actual val crashlyticsService: CrashlyticsService,
    actual val iapService: IapService,
    val appwrite: AppwriteApple,
    val serviceTokenProvider: PlatformGoogleAuthTokenProvider,
    val uuidProvider: IosUUIDProvider,
) {
    actual val messagingService: MessagingService = MessagingServiceImpl()
}