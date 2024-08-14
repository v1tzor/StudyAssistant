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

import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.common.functional.DeviceInfoProvider
import ru.aleshin.studyassistant.core.common.messages.GoogleAuthTokenProvider
import ru.aleshin.studyassistant.core.common.platform.IosUUIDProvider
import ru.aleshin.studyassistant.core.common.platform.Platform
import ru.aleshin.studyassistant.core.database.datasource.DriverFactory
import ru.aleshin.studyassistant.di.PlatformConfiguration

/**
 * @author Stanislav Aleshin on 24.04.2024.
 */
actual val platformModule = DI.Module("PlatformModule") {
    bindSingleton<Platform> { instance<PlatformConfiguration>().platform }
    bindSingleton<GoogleAuthTokenProvider> { instance<PlatformConfiguration>().serviceTokenProvider }
    bindSingleton<IosUUIDProvider> { instance<PlatformConfiguration>().uuidProvider }
    bindSingleton<DriverFactory> { DriverFactory() }
    bindSingleton<DeviceInfoProvider> { DeviceInfoProvider(instance()) }
}