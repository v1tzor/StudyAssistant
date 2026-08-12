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

package ru.aleshin.studyassistant.backend.installation

import io.ktor.server.config.ApplicationConfig

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
data class InstallationConfig(
    val registrationLimitPerNetworkPerDay: Int,
    val globalRegistrationLimitPerDay: Int,
) {

    init {
        require(registrationLimitPerNetworkPerDay > 0)
        require(globalRegistrationLimitPerDay >= registrationLimitPerNetworkPerDay)
    }

    companion object {

        fun from(applicationConfig: ApplicationConfig): InstallationConfig {
            val config = applicationConfig.config("installation")

            return InstallationConfig(
                registrationLimitPerNetworkPerDay = config
                    .property("registrationLimitPerNetworkPerDay")
                    .getString()
                    .toInt(),
                globalRegistrationLimitPerDay = config
                    .property("globalRegistrationLimitPerDay")
                    .getString()
                    .toInt(),
            )
        }
    }
}
