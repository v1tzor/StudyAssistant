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

package ru.aleshin.studyassistant.backend.sharing

import io.ktor.server.config.ApplicationConfig
import java.time.Duration

/**
 * @author Stanislav Aleshin on 11.08.2026.
 */
data class SharingConfig(
    val maxPayloadBytes: Int,
    val maxItemsPerShare: Int,
    val homeworkLifetime: Duration,
    val scheduleLifetime: Duration,
    val scheduleClaimLifetime: Duration,
    val createLimitPerHour: Int,
    val createdItemsLimitPerDay: Int,
    val activeHomeworkItemsLimit: Int,
    val activePayloadBytesLimitPerInstallation: Long,
    val createdPayloadBytesLimitPerDay: Int,
    val globalActivePayloadBytesLimit: Long,
    val globalCreatedPayloadBytesLimitPerDay: Int,
    val codeLookupLimit: Int,
    val codeLookupWindow: Duration,
    val maxRequestBodyBytes: Long = 1_100_000L,
) {

    init {
        require(maxPayloadBytes > 0)
        require(maxItemsPerShare > 0)
        require(!homeworkLifetime.isZero && !homeworkLifetime.isNegative)
        require(!scheduleLifetime.isZero && !scheduleLifetime.isNegative)
        require(!scheduleClaimLifetime.isZero && !scheduleClaimLifetime.isNegative)
        require(scheduleClaimLifetime < scheduleLifetime)
        require(createLimitPerHour > 0)
        require(createdItemsLimitPerDay >= maxItemsPerShare)
        require(activeHomeworkItemsLimit >= maxItemsPerShare)
        require(activePayloadBytesLimitPerInstallation >= maxPayloadBytes)
        require(createdPayloadBytesLimitPerDay >= maxPayloadBytes)
        require(globalActivePayloadBytesLimit >= activePayloadBytesLimitPerInstallation)
        require(globalCreatedPayloadBytesLimitPerDay >= createdPayloadBytesLimitPerDay)
        require(codeLookupLimit > 0)
        require(!codeLookupWindow.isZero && !codeLookupWindow.isNegative)
        require(maxRequestBodyBytes >= maxPayloadBytes)
    }

    companion object {
        fun from(applicationConfig: ApplicationConfig): SharingConfig {
            val config = applicationConfig.config("sharing")

            return SharingConfig(
                maxPayloadBytes = config.property("maxPayloadBytes").getString().toInt(),
                maxItemsPerShare = config.property("maxItemsPerShare").getString().toInt(),
                homeworkLifetime = Duration.ofHours(config.property("homeworkLifetimeHours").getString().toLong()),
                scheduleLifetime = Duration.ofMinutes(config.property("scheduleLifetimeMinutes").getString().toLong()),
                scheduleClaimLifetime = Duration.ofMinutes(config.property("scheduleClaimLifetimeMinutes").getString().toLong()),
                createLimitPerHour = config.property("createLimitPerHour").getString().toInt(),
                createdItemsLimitPerDay = config.property("createdItemsLimitPerDay").getString().toInt(),
                activeHomeworkItemsLimit = config.property("activeHomeworkItemsLimit").getString().toInt(),
                activePayloadBytesLimitPerInstallation = config
                    .property("activePayloadBytesLimitPerInstallation")
                    .getString()
                    .toLong(),
                createdPayloadBytesLimitPerDay = config
                    .property("createdPayloadBytesLimitPerDay")
                    .getString()
                    .toInt(),
                globalActivePayloadBytesLimit = config
                    .property("globalActivePayloadBytesLimit")
                    .getString()
                    .toLong(),
                globalCreatedPayloadBytesLimitPerDay = config
                    .property("globalCreatedPayloadBytesLimitPerDay")
                    .getString()
                    .toInt(),
                codeLookupLimit = config.property("codeLookupLimit").getString().toInt(),
                codeLookupWindow = Duration.ofMinutes(config.property("codeLookupWindowMinutes").getString().toLong()),
                maxRequestBodyBytes = config.property("maxRequestBodyBytes").getString().toLong(),
            )
        }
    }
}
