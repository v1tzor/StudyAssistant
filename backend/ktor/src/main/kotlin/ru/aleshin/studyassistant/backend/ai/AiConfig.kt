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

package ru.aleshin.studyassistant.backend.ai

import io.ktor.server.config.ApplicationConfig
import java.time.Duration

/**
 * @author Stanislav Aleshin on 11.08.2026.
 */
data class AiConfig(
    val dailyMessageLimit: Int,
    val rewardedMessageAmount: Int,
    val maxRewardedResetsPerDay: Int,
    val rewardChallengeLifetime: Duration,
    val globalDailyExecutionLimit: Int,
    val executionLimit: Int,
    val maxExecutionsPerMessage: Int,
    val maxConcurrentExecutionsPerInstallation: Int,
    val executionWindow: Duration,
    val reservationTimeout: Duration,
    val maxRequestBodyBytes: Long,
    val maxMessages: Int,
    val maxMessageCharacters: Int,
    val maxTotalContentCharacters: Int,
    val maxTools: Int,
    val maxToolDescriptionCharacters: Int,
    val maxToolSchemaCharacters: Int,
    val maxToolCallsPerMessage: Int,
    val maxToolArgumentsCharacters: Int,
    val maxScheduleRequestBodyBytes: Long,
    val maxScheduleImageBytes: Int,
    val maxScheduleNoteCharacters: Int,
    val maxScheduleEntries: Int,
    val maxScheduleFieldCharacters: Int,
    val maxScheduleUnparsedLines: Int,
) {

    init {
        require(dailyMessageLimit > 0)
        require(rewardedMessageAmount > 0)
        require(maxRewardedResetsPerDay > 0)
        require(!rewardChallengeLifetime.isZero && !rewardChallengeLifetime.isNegative)
        require(globalDailyExecutionLimit > 0)
        require(executionLimit > 0)
        require(maxExecutionsPerMessage > 0)
        require(maxConcurrentExecutionsPerInstallation > 0)
        require(!executionWindow.isZero && !executionWindow.isNegative)
        require(!reservationTimeout.isZero && !reservationTimeout.isNegative)
        require(maxRequestBodyBytes > 0)
        require(maxMessages > 0)
        require(maxMessageCharacters > 0)
        require(maxTotalContentCharacters >= maxMessageCharacters)
        require(maxTools > 0)
        require(maxToolDescriptionCharacters > 0)
        require(maxToolSchemaCharacters > 0)
        require(maxToolCallsPerMessage > 0)
        require(maxToolArgumentsCharacters > 0)
        require(maxScheduleRequestBodyBytes > 0)
        require(maxScheduleImageBytes > 0)
        require(maxScheduleImageBytes < maxScheduleRequestBodyBytes)
        require(maxScheduleNoteCharacters > 0)
        require(maxScheduleEntries > 0)
        require(maxScheduleFieldCharacters > 0)
        require(maxScheduleUnparsedLines > 0)
    }

    companion object {

        fun from(applicationConfig: ApplicationConfig): AiConfig {
            val config = applicationConfig.config("ai")

            return AiConfig(
                dailyMessageLimit = config.property("dailyMessageLimit").getString().toInt(),
                rewardedMessageAmount = config.property("rewardedMessageAmount").getString().toInt(),
                maxRewardedResetsPerDay = config.property("maxRewardedResetsPerDay").getString().toInt(),
                rewardChallengeLifetime = Duration.ofMinutes(
                    config.property("rewardChallengeLifetimeMinutes").getString().toLong(),
                ),
                globalDailyExecutionLimit = config
                    .property("globalDailyExecutionLimit")
                    .getString()
                    .toInt(),
                executionLimit = config.property("executionLimit").getString().toInt(),
                maxExecutionsPerMessage = config.property("maxExecutionsPerMessage").getString().toInt(),
                maxConcurrentExecutionsPerInstallation = config
                    .property("maxConcurrentExecutionsPerInstallation")
                    .getString()
                    .toInt(),
                executionWindow = Duration.ofMinutes(config.property("executionWindowMinutes").getString().toLong()),
                reservationTimeout = Duration.ofMinutes(
                    config.property("reservationTimeoutMinutes").getString().toLong(),
                ),
                maxRequestBodyBytes = config.property("maxRequestBodyBytes").getString().toLong(),
                maxMessages = config.property("maxMessages").getString().toInt(),
                maxMessageCharacters = config.property("maxMessageCharacters").getString().toInt(),
                maxTotalContentCharacters = config.property("maxTotalContentCharacters").getString().toInt(),
                maxTools = config.property("maxTools").getString().toInt(),
                maxToolDescriptionCharacters = config
                    .property("maxToolDescriptionCharacters")
                    .getString()
                    .toInt(),
                maxToolSchemaCharacters = config.property("maxToolSchemaCharacters").getString().toInt(),
                maxToolCallsPerMessage = config.property("maxToolCallsPerMessage").getString().toInt(),
                maxToolArgumentsCharacters = config
                    .property("maxToolArgumentsCharacters")
                    .getString()
                    .toInt(),
                maxScheduleRequestBodyBytes = config
                    .property("maxScheduleRequestBodyBytes")
                    .getString()
                    .toLong(),
                maxScheduleImageBytes = config.property("maxScheduleImageBytes").getString().toInt(),
                maxScheduleNoteCharacters = config.property("maxScheduleNoteCharacters").getString().toInt(),
                maxScheduleEntries = config.property("maxScheduleEntries").getString().toInt(),
                maxScheduleFieldCharacters = config.property("maxScheduleFieldCharacters").getString().toInt(),
                maxScheduleUnparsedLines = config.property("maxScheduleUnparsedLines").getString().toInt(),
            )
        }
    }
}
