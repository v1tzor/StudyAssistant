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

package ru.aleshin.studyassistant.backend

import io.ktor.server.application.Application
import io.ktor.server.netty.EngineMain
import ru.aleshin.studyassistant.backend.ai.aiModule
import ru.aleshin.studyassistant.backend.ads.adRewardModule
import ru.aleshin.studyassistant.backend.database.databaseModule
import ru.aleshin.studyassistant.backend.health.healthModule
import ru.aleshin.studyassistant.backend.installation.installationModule
import ru.aleshin.studyassistant.backend.plugins.configureSerialization
import ru.aleshin.studyassistant.backend.plugins.configureStatusPages
import ru.aleshin.studyassistant.backend.plugins.configureMonitoring
import ru.aleshin.studyassistant.backend.plugins.configureHttpSecurity
import ru.aleshin.studyassistant.backend.security.securityModule
import ru.aleshin.studyassistant.backend.sharing.sharingModule

/**
 * @author Stanislav Aleshin on 11.08.2026.
 */
fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    configureSerialization()
    configureStatusPages()
    configureHttpSecurity()
    configureMonitoring()

    databaseModule()
    securityModule()

    healthModule()
    installationModule()
    aiModule()
    adRewardModule()
    sharingModule()
}
