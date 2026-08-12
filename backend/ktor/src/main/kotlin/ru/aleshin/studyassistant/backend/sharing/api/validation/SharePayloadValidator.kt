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

package ru.aleshin.studyassistant.backend.sharing.api.validation

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.longOrNull
import ru.aleshin.studyassistant.backend.common.api.InvalidRequestException
import ru.aleshin.studyassistant.backend.sharing.SharingConfig

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
class SharePayloadValidator(
    private val config: SharingConfig,
    private val json: Json,
) {

    fun validateHomework(share: JsonObject): ValidatedSharePayload {
        if (share.keys != HOMEWORK_FIELDS) throw InvalidRequestException()
        validateSenderName(share = share)
        if ((share["date"] as? JsonPrimitive)?.longOrNull == null) {
            throw InvalidRequestException()
        }
        return validate(
            share = share,
            itemField = "homeworks",
        )
    }

    fun validateSchedule(share: JsonObject): ValidatedSharePayload {
        if (share.keys != SCHEDULE_FIELDS) throw InvalidRequestException()
        validateSenderName(share = share)
        val organizations = share["organizations"] as? JsonArray
            ?: throw InvalidRequestException()
        if (organizations.size > MAX_ORGANIZATIONS || organizations.any { it !is JsonObject }) {
            throw InvalidRequestException()
        }
        return validate(
            share = share,
            itemField = "schedules",
        )
    }

    private fun validate(
        share: JsonObject,
        itemField: String,
    ): ValidatedSharePayload {
        val items = share[itemField] as? JsonArray ?: throw InvalidRequestException()
        if (
            items.isEmpty() ||
            items.size > config.maxItemsPerShare ||
            items.any { item -> item !is JsonObject }
        ) {
            throw InvalidRequestException()
        }

        var nodes = 0
        fun validateElement(element: JsonElement, depth: Int) {
            if (depth > MAX_JSON_DEPTH || ++nodes > MAX_JSON_NODES) {
                throw InvalidRequestException()
            }
            when (element) {
                is JsonObject -> element.forEach { (key, value) ->
                    if (key.isBlank() || key.length > MAX_FIELD_NAME_CHARACTERS) {
                        throw InvalidRequestException()
                    }
                    validateElement(value, depth + 1)
                }
                is JsonArray -> element.forEach { value -> validateElement(value, depth + 1) }
                is JsonPrimitive -> if (
                    element.isString &&
                    (element.contentOrNull?.length ?: 0) > MAX_STRING_CHARACTERS
                ) {
                    throw InvalidRequestException()
                }
            }
        }
        validateElement(share, depth = 0)

        val bytes = json.encodeToString(
            serializer = JsonElement.serializer(),
            value = share,
        ).encodeToByteArray()
        if (bytes.size > config.maxPayloadBytes) throw InvalidRequestException()

        return ValidatedSharePayload(
            bytes = bytes,
            itemCount = items.size,
        )
    }

    private fun validateSenderName(share: JsonObject) {
        val senderName = (share["senderName"] as? JsonPrimitive)
            ?.takeIf(JsonPrimitive::isString)
            ?.contentOrNull
            ?: throw InvalidRequestException()
        if (senderName.isBlank() || senderName.length > MAX_SENDER_NAME_CHARACTERS) {
            throw InvalidRequestException()
        }
    }

    private companion object {

        const val MAX_SENDER_NAME_CHARACTERS = 256
        const val MAX_STRING_CHARACTERS = 32_768
        const val MAX_FIELD_NAME_CHARACTERS = 128
        const val MAX_JSON_DEPTH = 16
        const val MAX_JSON_NODES = 20_000
        const val MAX_ORGANIZATIONS = 100

        val HOMEWORK_FIELDS = setOf("senderName", "date", "homeworks")
        val SCHEDULE_FIELDS = setOf("senderName", "schedules", "organizations")
    }
}
