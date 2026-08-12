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

package ru.aleshin.studyassistant.backend.sharing.domain.model

import java.time.Instant
import java.util.UUID

/**
 * @author Stanislav Aleshin on 11.08.2026.
 */
data class StoredScheduleShare(
    val id: UUID,
    val codeHash: ByteArray,
    val creatorHash: ByteArray,
    val itemCount: Int,
    val payload: ByteArray,
    val payloadNonce: ByteArray,
    val createdAt: Instant,
    val expiresAt: Instant,
    val claimHash: ByteArray?,
    val claimedUntil: Instant?,
    val consumedAt: Instant?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StoredScheduleShare

        if (itemCount != other.itemCount) return false
        if (id != other.id) return false
        if (!codeHash.contentEquals(other.codeHash)) return false
        if (!creatorHash.contentEquals(other.creatorHash)) return false
        if (!payload.contentEquals(other.payload)) return false
        if (!payloadNonce.contentEquals(other.payloadNonce)) return false
        if (createdAt != other.createdAt) return false
        if (expiresAt != other.expiresAt) return false
        if (!claimHash.contentEquals(other.claimHash)) return false
        if (claimedUntil != other.claimedUntil) return false
        if (consumedAt != other.consumedAt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = itemCount
        result = 31 * result + id.hashCode()
        result = 31 * result + codeHash.contentHashCode()
        result = 31 * result + creatorHash.contentHashCode()
        result = 31 * result + payload.contentHashCode()
        result = 31 * result + payloadNonce.contentHashCode()
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + expiresAt.hashCode()
        result = 31 * result + (claimHash?.contentHashCode() ?: 0)
        result = 31 * result + (claimedUntil?.hashCode() ?: 0)
        result = 31 * result + (consumedAt?.hashCode() ?: 0)
        return result
    }
}