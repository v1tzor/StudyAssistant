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

package ru.aleshin.studyassistant.backend.sharing.services

import kotlinx.serialization.json.jsonObject
import ru.aleshin.studyassistant.backend.common.api.InvalidRequestException
import ru.aleshin.studyassistant.backend.common.api.InvalidShareException
import ru.aleshin.studyassistant.backend.common.api.RateLimitException
import ru.aleshin.studyassistant.backend.common.api.ServerUnavailableException
import ru.aleshin.studyassistant.backend.plugins.BackendJson
import ru.aleshin.studyassistant.backend.security.InstallationHasher
import ru.aleshin.studyassistant.backend.security.PayloadCipher
import ru.aleshin.studyassistant.backend.security.PayloadPurpose
import ru.aleshin.studyassistant.backend.security.ShareCode
import ru.aleshin.studyassistant.backend.security.ShareCodeGenerator
import ru.aleshin.studyassistant.backend.security.ShareCodeHasher
import ru.aleshin.studyassistant.backend.sharing.SharingConfig
import ru.aleshin.studyassistant.backend.sharing.api.dto.homework.CreateHomeworkShareRequest
import ru.aleshin.studyassistant.backend.sharing.api.dto.homework.CreateHomeworkShareResponse
import ru.aleshin.studyassistant.backend.sharing.api.dto.homework.FetchHomeworkShareRequest
import ru.aleshin.studyassistant.backend.sharing.api.dto.homework.FetchHomeworkShareResponse
import ru.aleshin.studyassistant.backend.sharing.api.dto.homework.HomeworkShareLinkDto
import ru.aleshin.studyassistant.backend.sharing.api.validation.SharePayloadValidator
import ru.aleshin.studyassistant.backend.sharing.domain.model.StoredHomeworkShare
import ru.aleshin.studyassistant.backend.sharing.domain.repository.HomeworkSharingRepository
import ru.aleshin.studyassistant.backend.sharing.domain.result.CodeLookupStorageResult
import ru.aleshin.studyassistant.backend.sharing.domain.result.CreateHomeworkShareStorageResult
import java.time.Clock
import java.util.UUID

/**
 * @author Stanislav Aleshin on 11.08.2026.
 */
class HomeworkSharingService(
    private val repository: HomeworkSharingRepository,
    private val installationHasher: InstallationHasher,
    private val shareCodeGenerator: ShareCodeGenerator,
    private val shareCodeHasher: ShareCodeHasher,
    private val payloadCipher: PayloadCipher,
    private val payloadValidator: SharePayloadValidator,
    private val config: SharingConfig,
    private val clock: Clock,
) {

    suspend fun create(
        installationToken: String,
        request: CreateHomeworkShareRequest,
    ): CreateHomeworkShareResponse {
        val payload = payloadValidator.validateHomework(share = request.share)

        val creatorHash = installationHasher.hash(
            installationToken = installationToken,
        )

        val encrypted = payloadCipher.encrypt(
            plaintext = payload.bytes,
            purpose = PayloadPurpose.HOMEWORK_SHARE,
        )

        val createdAt = clock.instant()
        val expiresAt = createdAt.plus(config.homeworkLifetime)

        repeat(CODE_GENERATION_ATTEMPTS) {
            val code = shareCodeGenerator.generate()

            val share = StoredHomeworkShare(
                id = UUID.randomUUID(),
                codeHash = shareCodeHasher.hash(code = code),
                creatorHash = creatorHash,
                itemCount = payload.itemCount,
                payload = encrypted.ciphertext,
                payloadNonce = encrypted.nonce,
                createdAt = createdAt,
                expiresAt = expiresAt,
            )

            when (val result = repository.tryCreate(share = share)) {
                CreateHomeworkShareStorageResult.Created -> {
                    return CreateHomeworkShareResponse(
                        link = HomeworkShareLinkDto(
                            code = code.formatted(),
                            createdAt = createdAt.toEpochMilli(),
                            expiresAt = expiresAt.toEpochMilli(),
                        ),
                    )
                }
                CreateHomeworkShareStorageResult.CodeConflict -> {
                    // Generate another code.
                }
                is CreateHomeworkShareStorageResult.Limited -> {
                    throw RateLimitException(retryAt = result.retryAt?.toEpochMilli())
                }
            }
        }

        throw ServerUnavailableException()
    }

    suspend fun fetch(
        installationToken: String,
        request: FetchHomeworkShareRequest,
    ): FetchHomeworkShareResponse {
        val installationHash = installationHasher.hash(
            installationToken = installationToken,
        )

        when (
            val result = repository.recordCodeLookup(
                installationHash = installationHash,
                now = clock.instant(),
            )
        ) {
            is CodeLookupStorageResult.Allowed -> Unit
            is CodeLookupStorageResult.Limited -> {
                throw RateLimitException(retryAt = result.retryAt?.toEpochMilli())
            }
        }

        val code = try {
            ShareCode.parse(raw = request.code)
        } catch (_: IllegalArgumentException) {
            throw InvalidRequestException()
        }

        val storedShare = repository.findAvailable(
            codeHash = shareCodeHasher.hash(code = code),
            now = clock.instant(),
        ) ?: throw InvalidShareException()

        val share = try {
            val plaintext = payloadCipher.decrypt(
                ciphertext = storedShare.payload,
                nonce = storedShare.payloadNonce,
                purpose = PayloadPurpose.HOMEWORK_SHARE,
            )
            BackendJson.parseToJsonElement(plaintext.decodeToString()).jsonObject
        } catch (_: Exception) {
            throw ServerUnavailableException()
        }

        return FetchHomeworkShareResponse(
            share = share,
        )
    }

    private companion object {

        const val CODE_GENERATION_ATTEMPTS = 5
    }
}
