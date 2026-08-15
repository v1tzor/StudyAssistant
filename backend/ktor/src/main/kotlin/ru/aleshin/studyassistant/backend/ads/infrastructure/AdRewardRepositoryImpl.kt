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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.core.sum
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import ru.aleshin.studyassistant.backend.ads.domain.model.AdRewardChallenge
import ru.aleshin.studyassistant.backend.ads.domain.model.AdRewardCompletion
import ru.aleshin.studyassistant.backend.ads.domain.model.AdRewardPurpose
import ru.aleshin.studyassistant.backend.ads.domain.repository.AdRewardRepository
import ru.aleshin.studyassistant.backend.ai.AiConfig
import ru.aleshin.studyassistant.backend.ai.domain.model.AiQuota
import ru.aleshin.studyassistant.backend.ai.infrastructure.AiUsageTable
import java.nio.ByteBuffer
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.UUID
import kotlin.uuid.Uuid

/**
 * @author Stanislav Aleshin on 13.08.2026.
 */
class AdRewardRepositoryImpl(
    private val database: Database,
    private val config: AiConfig,
) : AdRewardRepository {

    override suspend fun createChallenge(
        installationHash: ByteArray,
        purpose: AdRewardPurpose,
        subjectHash: ByteArray?,
        now: Instant,
    ): AdRewardChallenge? = dbQuery {
        lockInstallation(installationHash = installationHash)

        val existingChallenge = AdRewardChallengesTable
            .select(
                AdRewardChallengesTable.id,
                AdRewardChallengesTable.expiresAt,
            )
            .where {
                (AdRewardChallengesTable.installationHash eq installationHash) and
                    (AdRewardChallengesTable.purpose eq purpose.value) and
                    (AdRewardChallengesTable.subjectHash eq subjectHash) and
                    AdRewardChallengesTable.completedAt.isNull() and
                    (AdRewardChallengesTable.expiresAt greaterEq now.atOffset(ZoneOffset.UTC))
            }
            .limit(1)
            .singleOrNull()

        if (existingChallenge != null) {
            return@dbQuery AdRewardChallenge(
                id = UUID.fromString(existingChallenge[AdRewardChallengesTable.id].toString()),
                purpose = purpose,
                expiresAt = existingChallenge[AdRewardChallengesTable.expiresAt].toInstant(),
            )
        }

        if (purpose == AdRewardPurpose.AI_QUOTA_RESET) {
            val quota = currentAiQuota(
                installationHash = installationHash,
                now = now,
            )
            if (quota.remaining > 0 || quota.rewardedResetsRemaining <= 0) {
                return@dbQuery null
            }
        }

        val challenge = AdRewardChallenge(
            id = UUID.randomUUID(),
            purpose = purpose,
            expiresAt = now.plus(config.rewardChallengeLifetime),
        )

        AdRewardChallengesTable.insert {
            it[id] = Uuid.parse(challenge.id.toString())
            it[AdRewardChallengesTable.installationHash] = installationHash
            it[AdRewardChallengesTable.purpose] = purpose.value
            it[AdRewardChallengesTable.subjectHash] = subjectHash
            it[createdAt] = now.atOffset(ZoneOffset.UTC)
            it[expiresAt] = challenge.expiresAt.atOffset(ZoneOffset.UTC)
            it[completedAt] = null
            it[consumedAt] = null
        }

        challenge
    }

    override suspend fun completeChallenge(
        installationHash: ByteArray,
        challengeId: UUID,
        now: Instant,
    ): AdRewardCompletion? = dbQuery {
        lockInstallation(installationHash = installationHash)
        val storedChallengeId = Uuid.parse(challengeId.toString())

        val challenge = AdRewardChallengesTable
            .select(
                AdRewardChallengesTable.purpose,
                AdRewardChallengesTable.expiresAt,
                AdRewardChallengesTable.completedAt,
            )
            .where {
                (AdRewardChallengesTable.id eq storedChallengeId) and
                    (AdRewardChallengesTable.installationHash eq installationHash)
            }
            .limit(1)
            .singleOrNull()
            ?: return@dbQuery null

        val purpose = AdRewardPurpose.entries.firstOrNull {
            it.value == challenge[AdRewardChallengesTable.purpose]
        } ?: return@dbQuery null

        if (challenge[AdRewardChallengesTable.completedAt] != null) {
            return@dbQuery completion(
                installationHash = installationHash,
                purpose = purpose,
                now = now,
            )
        }
        if (challenge[AdRewardChallengesTable.expiresAt].toInstant().isBefore(now)) {
            return@dbQuery null
        }

        if (purpose == AdRewardPurpose.AI_QUOTA_RESET) {
            val quota = currentAiQuota(
                installationHash = installationHash,
                now = now,
            )
            if (quota.remaining > 0 || quota.rewardedResetsRemaining <= 0) {
                return@dbQuery null
            }

            val usageDate = now.atZone(ZoneOffset.UTC).toLocalDate()
            AiRewardGrantsTable.insert {
                it[AiRewardGrantsTable.challengeId] = storedChallengeId
                it[AiRewardGrantsTable.installationHash] = installationHash
                it[AiRewardGrantsTable.usageDate] = usageDate
                it[amount] = config.rewardedMessageAmount
                it[createdAt] = now.atOffset(ZoneOffset.UTC)
            }
        }

        AdRewardChallengesTable.update(
            where = { AdRewardChallengesTable.id eq storedChallengeId },
        ) {
            it[completedAt] = now.atOffset(ZoneOffset.UTC)
        }

        completion(
            installationHash = installationHash,
            purpose = purpose,
            now = now,
        )
    }

    private fun completion(
        installationHash: ByteArray,
        purpose: AdRewardPurpose,
        now: Instant,
    ): AdRewardCompletion {
        val quota = currentAiQuota(
            installationHash = installationHash,
            now = now,
        ).takeIf { purpose == AdRewardPurpose.AI_QUOTA_RESET }
        val resetAt = now
            .atZone(ZoneOffset.UTC)
            .toLocalDate()
            .plusDays(1)
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .takeIf { purpose == AdRewardPurpose.AI_QUOTA_RESET }

        return AdRewardCompletion(
            purpose = purpose,
            quota = quota,
            quotaResetAt = resetAt,
        )
    }

    private fun currentAiQuota(
        installationHash: ByteArray,
        now: Instant,
    ): AiQuota {
        val usageDate = now.atZone(ZoneOffset.UTC).toLocalDate()
        val used = AiUsageTable
            .select(AiUsageTable.used)
            .where {
                (AiUsageTable.installationHash eq installationHash) and
                    (AiUsageTable.usageDate eq usageDate)
            }
            .limit(1)
            .singleOrNull()
            ?.get(AiUsageTable.used)
            ?: 0
        val amountSum = AiRewardGrantsTable.amount.sum()
        val grantedAmount = AiRewardGrantsTable
            .select(amountSum)
            .where {
                (AiRewardGrantsTable.installationHash eq installationHash) and
                    (AiRewardGrantsTable.usageDate eq usageDate)
            }
            .singleOrNull()
            ?.get(amountSum)
            ?: 0
        val grantCount = if (config.rewardedMessageAmount == 0) {
            0
        } else {
            grantedAmount / config.rewardedMessageAmount
        }

        return AiQuota(
            used = used,
            limit = config.dailyMessageLimit + grantedAmount,
            rewardedResetsRemaining = (config.maxRewardedResetsPerDay - grantCount).coerceAtLeast(0),
        )
    }

    private fun lockInstallation(installationHash: ByteArray) {
        val key = ByteBuffer.wrap(installationHash, 0, Long.SIZE_BYTES).long
        TransactionManager.current().exec("SELECT pg_advisory_xact_lock($key)")
    }

    private suspend fun <T> dbQuery(block: () -> T): T {
        return withContext(Dispatchers.IO) {
            transaction(db = database) {
                block()
            }
        }
    }
}
