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

package ru.aleshin.studyassistant.backend.security

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * @author Stanislav Aleshin on 11.08.2026.
 */
class SecurityPrimitivesTest {

    private val installationSecret = ByteArray(32) { 1 }

    private val sharingSecret = ByteArray(32) { 2 }

    private val encryptionKey = ByteArray(32) { 3 }

    @Test
    fun installationHashShouldBeDeterministic() {
        val hasher = InstallationHasher(
            secret = installationSecret,
        )

        val first = hasher.hash(
            installationToken = "test-installation-token",
        )

        val second = hasher.hash(
            installationToken = "test-installation-token",
        )

        assertEquals(
            expected = 32,
            actual = first.size,
        )

        assertContentEquals(
            expected = first,
            actual = second,
        )
    }

    @Test
    fun differentInstallationTokensShouldHaveDifferentHashes() {
        val hasher = InstallationHasher(
            secret = installationSecret,
        )

        val first = hasher.hash(
            installationToken = "installation-1",
        )

        val second = hasher.hash(
            installationToken = "installation-2",
        )

        assertFalse(
            first.contentEquals(second),
        )
    }

    @Test
    fun installationCredentialShouldVerifyOnlyWithIssuingSecret() {
        val service = InstallationCredentialService(
            secret = installationSecret,
        )
        val credential = service.issue()
        val otherService = InstallationCredentialService(
            secret = ByteArray(32) { 9 },
        )

        assertEquals(90, credential.length)
        assertTrue(service.isValid(credential))
        assertFalse(otherService.isValid(credential))
    }

    @Test
    fun tamperedInstallationCredentialShouldBeRejected() {
        val service = InstallationCredentialService(
            secret = installationSecret,
        )
        val credential = service.issue()
        val signatureStartIndex = credential.lastIndexOf('.') + 1
        val replacement = if (credential[signatureStartIndex] == 'A') 'B' else 'A'
        val tampered = credential.replaceRange(
            startIndex = signatureStartIndex,
            endIndex = signatureStartIndex + 1,
            replacement = replacement.toString(),
        )

        assertFalse(service.isValid(tampered))
        assertFalse(service.isValid("v1.invalid.invalid"))
    }

    @Test
    fun nonCanonicalInstallationCredentialSignatureShouldBeRejected() {
        val service = InstallationCredentialService(
            secret = installationSecret,
        )
        val credential = service.issue()
        val canonicalTailIndex = BASE64_URL_ALPHABET.indexOf(credential.last())
        assertTrue(canonicalTailIndex >= 0)
        val nonCanonical = credential.dropLast(1) + BASE64_URL_ALPHABET[canonicalTailIndex + 1]

        assertFalse(service.isValid(nonCanonical))
    }

    @Test
    fun registrationNetworkHashShouldCollapseIpv6PrivacyAddressesToPrefix() {
        val hasher = NetworkHasher(secret = installationSecret)
        val first = hasher.hash("2001:db8:abcd:12::1")
        val second = hasher.hash("2001:db8:abcd:12:ffff:ffff:ffff:ffff")
        val otherNetwork = hasher.hash("2001:db8:abcd:13::1")

        assertContentEquals(first, second)
        assertFalse(first.contentEquals(otherNetwork))
    }

    @Test
    fun shareCodeShouldNormalizeInput() {
        val first = ShareCode.parse(
            raw = "abcd-efgh-jkmn",
        )

        val second = ShareCode.parse(
            raw = "ABCD EFGH JKMN",
        )

        val third = ShareCode.parse(
            raw = "ABCDEFGHJKMN",
        )

        assertEquals(
            expected = first,
            actual = second,
        )

        assertEquals(
            expected = second,
            actual = third,
        )

        assertEquals(
            expected = "ABCD-EFGH-JKMN",
            actual = first.formatted(),
        )
    }

    @Test
    fun generatedShareCodeShouldHaveCorrectFormat() {
        val generator = ShareCodeGenerator()

        repeat(100) {
            val code = generator.generate()

            assertEquals(
                expected = ShareCode.LENGTH,
                actual = code.value.length,
            )

            assertTrue(
                code.value.all { character ->
                    character in ShareCode.ALPHABET
                },
            )

            assertEquals(
                expected = 14,
                actual = code.formatted().length,
            )
        }
    }

    @Test
    fun claimTokenShouldVerifyOnlyCorrectToken() {
        val service = ClaimTokenService(
            secret = sharingSecret,
        )

        val token = service.generate()
        val hash = service.hash(token)

        assertTrue(service.verify(token = token, expectedHash = hash))
        assertFalse(service.verify(token = service.generate(), expectedHash = hash))
    }

    @Test
    fun payloadShouldEncryptAndDecrypt() {
        val cipher = PayloadCipher(
            key = encryptionKey,
        )

        val plaintext = """
            {"hello":"world"}
        """.trimIndent().encodeToByteArray()

        val encrypted = cipher.encrypt(
            plaintext = plaintext,
            purpose = PayloadPurpose.HOMEWORK_SHARE,
        )

        val decrypted = cipher.decrypt(
            ciphertext = encrypted.ciphertext,
            nonce = encrypted.nonce,
            purpose = PayloadPurpose.HOMEWORK_SHARE,
        )

        assertEquals(
            expected = 12,
            actual = encrypted.nonce.size,
        )

        assertContentEquals(
            expected = plaintext,
            actual = decrypted,
        )
    }

    @Test
    fun samePayloadShouldProduceDifferentCiphertext() {
        val cipher = PayloadCipher(
            key = encryptionKey,
        )

        val plaintext = "payload".encodeToByteArray()

        val first = cipher.encrypt(
            plaintext = plaintext,
            purpose = PayloadPurpose.SCHEDULE_SHARE,
        )

        val second = cipher.encrypt(
            plaintext = plaintext,
            purpose = PayloadPurpose.SCHEDULE_SHARE,
        )

        assertFalse(first.nonce.contentEquals(second.nonce))
        assertFalse(first.ciphertext.contentEquals(second.ciphertext))
    }

    @Test
    fun tamperedPayloadShouldNotDecrypt() {
        val cipher = PayloadCipher(
            key = encryptionKey,
        )

        val encrypted = cipher.encrypt(
            plaintext = "important".encodeToByteArray(),
            purpose = PayloadPurpose.SCHEDULE_SHARE,
        )

        val tampered = encrypted.ciphertext.copyOf()

        tampered[0] = (tampered[0].toInt() xor 1).toByte()

        assertFails {
            cipher.decrypt(
                ciphertext = tampered,
                nonce = encrypted.nonce,
                purpose = PayloadPurpose.SCHEDULE_SHARE,
            )
        }
    }

    @Test
    fun payloadShouldBeBoundToPurpose() {
        val cipher = PayloadCipher(
            key = encryptionKey,
        )

        val encrypted = cipher.encrypt(
            plaintext = "payload".encodeToByteArray(),
            purpose = PayloadPurpose.HOMEWORK_SHARE,
        )

        assertFails {
            cipher.decrypt(
                ciphertext = encrypted.ciphertext,
                nonce = encrypted.nonce,
                purpose = PayloadPurpose.SCHEDULE_SHARE,
            )
        }
    }

    private companion object {

        const val BASE64_URL_ALPHABET =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_"
    }
}
