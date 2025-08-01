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

package ru.aleshin.studyassistant.core.api.models

import kotlinx.serialization.json.JsonElement

/**
 * @author Stanislav Aleshin on 09.07.2025.
 */
sealed class ClientParam {
    data class StringParam(val key: String, val value: String?) : ClientParam()

    data class MapParam(val key: String, val value: Map<String, Any>) : ClientParam()

    data class ListParam(val key: String, val value: List<String>) : ClientParam()

    data class JsonListParam(val key: String, val value: List<JsonElement>) : ClientParam()

    data class FileParam(val fileName: String, val data: ByteArray) : ClientParam() {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as FileParam

            if (fileName != other.fileName) return false
            if (!data.contentEquals(other.data)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = fileName.hashCode()
            result = 31 * result + data.contentHashCode()
            return result
        }
    }
}