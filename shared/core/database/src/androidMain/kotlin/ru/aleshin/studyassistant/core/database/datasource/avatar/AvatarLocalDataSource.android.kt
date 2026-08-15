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

package ru.aleshin.studyassistant.core.database.datasource.avatar

import android.content.Context
import android.net.Uri
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.domain.entities.files.InputFile
import java.io.File

class AndroidAvatarLocalDataSource(
    private val context: Context,
) : AvatarLocalDataSource {

    override suspend fun saveAvatar(type: AvatarType, file: InputFile): String {
        val directory = File(context.filesDir, type.directory()).apply { mkdirs() }
        val extension = file.mimeType.safeExtension()
        val targetFile = File(directory, "${randomUUID()}.$extension")
        targetFile.writeBytes(file.fileBytes)
        return Uri.fromFile(targetFile).toString()
    }

    override suspend fun deleteAvatar(avatar: String) {
        val avatarFile = Uri.parse(avatar).path?.let(::File) ?: return
        val avatarsDirectory = File(context.filesDir, AVATARS_DIRECTORY).canonicalFile
        val targetFile = avatarFile.canonicalFile
        if (targetFile.path.startsWith(avatarsDirectory.path + File.separator)) targetFile.delete()
    }

    private fun AvatarType.directory() = "$AVATARS_DIRECTORY/${name.lowercase()}"

    private fun String.safeExtension(): String = substringAfter('/', DEFAULT_EXTENSION)
        .substringBefore('+')
        .filter(Char::isLetterOrDigit)
        .take(MAX_EXTENSION_LENGTH)
        .ifBlank { DEFAULT_EXTENSION }

    private companion object {
        const val AVATARS_DIRECTORY = "avatars"
        const val DEFAULT_EXTENSION = "jpg"
        const val MAX_EXTENSION_LENGTH = 10
    }
}
