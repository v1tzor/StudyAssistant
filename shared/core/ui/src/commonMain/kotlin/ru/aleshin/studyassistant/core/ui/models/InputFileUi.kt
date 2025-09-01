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

package ru.aleshin.studyassistant.core.ui.models

import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.domain.entities.files.InputFile

/**
 * @author Stanislav Aleshin on 02.07.2025.
 */
@Serializable
data class InputFileUi(
    val uri: String?,
    val filename: String,
    val mimeType: String,
    val fileBytes: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as InputFile

        if (uri != other.uri) return false
        if (filename != other.filename) return false
        if (mimeType != other.mimeType) return false
        if (!fileBytes.contentEquals(other.fileBytes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = uri.hashCode()
        result = 31 * result + filename.hashCode()
        result = 31 * result + mimeType.hashCode()
        result = 31 * result + fileBytes.contentHashCode()
        return result
    }
}