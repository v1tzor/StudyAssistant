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

package ru.aleshin.studyassistant.backend.ads.infrastructure

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.date
import org.jetbrains.exposed.v1.javatime.timestampWithTimeZone

/**
 * @author Stanislav Aleshin on 13.08.2026.
 */
object AdRewardChallengesTable : Table(name = "ad_reward_challenges") {
    val id = uuid("id")
    val installationHash = binary("installation_hash")
    val purpose = varchar(name = "purpose", length = 40)
    val subjectHash = binary("subject_hash").nullable()
    val createdAt = timestampWithTimeZone("created_at")
    val expiresAt = timestampWithTimeZone("expires_at")
    val completedAt = timestampWithTimeZone("completed_at").nullable()
    val consumedAt = timestampWithTimeZone("consumed_at").nullable()

    override val primaryKey = PrimaryKey(id)
}

object AiRewardGrantsTable : Table(name = "ai_reward_grants") {
    val challengeId = uuid("challenge_id")
    val installationHash = binary("installation_hash")
    val usageDate = date("usage_date")
    val amount = integer("amount")
    val createdAt = timestampWithTimeZone("created_at")

    override val primaryKey = PrimaryKey(challengeId)
}
