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
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.remote.api.message.GoogleAuthTokenProvider
import ru.aleshin.studyassistant.core.remote.api.message.GoogleAuthTokenProviderImpl

/**
 * @author Stanislav Aleshin on 08.08.2024.
 */
actual val coreRemotePlatformModule = DI.Module("CoreRemotePlatform") {
    bindSingleton<GoogleAuthTokenProvider> { GoogleAuthTokenProviderImpl(instance()) }
    bindSingleton<Konnection> { Konnection.createInstance(instance<Context>()) }
}