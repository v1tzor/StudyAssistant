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

package ru.aleshin.studyassistant.backend.sharing.infrastructure

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.java.javaUUID
import org.jetbrains.exposed.v1.javatime.timestampWithTimeZone

/**
 * @author Stanislav Aleshin on 11.08.2026.
 */
object ScheduleSharesTable : Table(name = "schedule_shares") {
    val id = javaUUID("id")
    val codeHash = binary("code_hash")
    val creatorHash = binary("creator_hash")
    val itemCount = integer("item_count")
    val payload = binary("payload")
    val payloadSize = long("payload_size")
    val payloadNonce = binary("payload_nonce")
    val createdAt = timestampWithTimeZone("created_at")
    val expiresAt = timestampWithTimeZone("expires_at")
    val claimHash = binary("claim_hash").nullable()
    val claimedUntil = timestampWithTimeZone("claimed_until").nullable()
    val consumedAt = timestampWithTimeZone("consumed_at").nullable()

    override val primaryKey = PrimaryKey(id)
}
