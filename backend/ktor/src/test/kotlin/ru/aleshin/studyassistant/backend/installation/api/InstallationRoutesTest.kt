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

package ru.aleshin.studyassistant.backend.installation.api

import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import ru.aleshin.studyassistant.backend.installation.api.mappers.InstallationRegistrationResponseMapper
import ru.aleshin.studyassistant.backend.installation.domain.repository.InstallationRegistrationRepository
import ru.aleshin.studyassistant.backend.installation.domain.result.InstallationRegistrationStorageResult
import ru.aleshin.studyassistant.backend.installation.services.InstallationRegistrationService
import ru.aleshin.studyassistant.backend.plugins.configureSerialization
import ru.aleshin.studyassistant.backend.plugins.configureStatusPages
import ru.aleshin.studyassistant.backend.security.InstallationCredentialService
import ru.aleshin.studyassistant.backend.security.NetworkHasher
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
class InstallationRoutesTest {

    @Test
    fun registrationShouldReturnServerSignedCredential() = testApplication {
        val credentialService = InstallationCredentialService(secret = SECRET)

        application {
            configureSerialization()
            configureStatusPages()
            routing {
                installationRoutes(
                    service = service(
                        repository = FakeRepository(
                            result = InstallationRegistrationStorageResult.Reserved,
                        ),
                        credentialService = credentialService,
                    ),
                    responseMapper = InstallationRegistrationResponseMapper(),
                )
            }
        }

        val response = client.post("/api/v1/installations/register")
        val credential = CREDENTIAL_REGEX.find(response.bodyAsText())?.value

        assertEquals(HttpStatusCode.Created, response.status)
        assertTrue(credential != null && credentialService.isValid(credential))
    }

    @Test
    fun exhaustedRegistrationBudgetShouldReturnRetryableRateLimit() = testApplication {
        application {
            configureSerialization()
            configureStatusPages()
            routing {
                installationRoutes(
                    service = service(
                        repository = FakeRepository(
                            result = InstallationRegistrationStorageResult.RateLimited(
                                retryAt = Instant.parse("2026-08-13T00:00:00Z"),
                            ),
                        ),
                        credentialService = InstallationCredentialService(secret = SECRET),
                    ),
                    responseMapper = InstallationRegistrationResponseMapper(),
                )
            }
        }

        val response = client.post("/api/v1/installations/register")

        assertEquals(HttpStatusCode.TooManyRequests, response.status)
        assertTrue(response.bodyAsText().contains("\"errorCode\":\"rate_limit\""))
    }

    @Test
    fun registrationShouldRejectRequestBody() = testApplication {
        application {
            configureSerialization()
            configureStatusPages()
            routing {
                installationRoutes(
                    service = service(
                        repository = FakeRepository(
                            result = InstallationRegistrationStorageResult.Reserved,
                        ),
                        credentialService = InstallationCredentialService(secret = SECRET),
                    ),
                    responseMapper = InstallationRegistrationResponseMapper(),
                )
            }
        }

        val response = client.post("/api/v1/installations/register") {
            setBody("not-empty")
        }

        assertEquals(HttpStatusCode.PayloadTooLarge, response.status)
    }

    private fun service(
        repository: InstallationRegistrationRepository,
        credentialService: InstallationCredentialService,
    ): InstallationRegistrationService {
        return InstallationRegistrationService(
            repository = repository,
            credentialService = credentialService,
            networkHasher = NetworkHasher(secret = SECRET),
            clock = Clock.fixed(
                Instant.parse("2026-08-12T10:00:00Z"),
                ZoneOffset.UTC,
            ),
        )
    }

    private class FakeRepository(
        private val result: InstallationRegistrationStorageResult,
    ) : InstallationRegistrationRepository {

        override suspend fun reserve(
            networkHash: ByteArray,
            now: Instant,
        ): InstallationRegistrationStorageResult = result
    }

    private companion object {

        val SECRET = ByteArray(32) { 4 }
        val CREDENTIAL_REGEX = Regex("v1\\.[A-Za-z0-9_-]{43}\\.[A-Za-z0-9_-]{43}")
    }
}
