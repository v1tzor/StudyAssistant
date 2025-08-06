/*
 * Copyright 2024 Stanislav Aleshin
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

package ru.aleshin.studyassistant.data

import android.util.Base64
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object SignatureGenerator {

    @Throws(Exception::class)
    fun generateSignature(keyId: String, privateKeyContent: String): String {
        val keyFactory = KeyFactory.getInstance("RSA")
        val decodedKey = Base64.decode(privateKeyContent, Base64.DEFAULT)
        val keySpec = PKCS8EncodedKeySpec(decodedKey)
        val privateKey: PrivateKey = keyFactory.generatePrivate(keySpec)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        val timestamp = dateFormat.format(Date())
        val messageToSign = keyId + timestamp

        val signature = Signature.getInstance("SHA512withRSA")
        signature.initSign(privateKey)
        signature.update(messageToSign.toByteArray())

        val signedBytes = signature.sign()
        val signatureValue = Base64.encodeToString(signedBytes, Base64.NO_WRAP)

        return """
            {
              "keyId":"$keyId",
              "timestamp":"$timestamp",
              "signature":"$signatureValue"
            }
        """.trimIndent()
    }
}