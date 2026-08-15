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

@file:OptIn(ExperimentalForeignApi::class)

package ru.aleshin.studyassistant.core.database.datasource.avatar

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.writeToURL
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.domain.entities.files.InputFile

/**
 * @author Stanislav Aleshin on 08.08.2026.
 */
class IosAvatarLocalDataSource : AvatarLocalDataSource {

    private val fileManager = NSFileManager.defaultManager

    override suspend fun saveAvatar(type: AvatarType, file: InputFile): String {
        val applicationDirectory = applicationDirectory()
        val directory = checkNotNull(
            applicationDirectory.URLByAppendingPathComponent(
                "avatars/${type.name.lowercase()}",
                isDirectory = true
            )
        )
        fileManager.createDirectoryAtURL(
            url = directory,
            withIntermediateDirectories = true,
            attributes = null,
            error = null,
        )

        val extension = file.mimeType.safeExtension()
        val targetUrl = checkNotNull(
            directory.URLByAppendingPathComponent("${randomUUID()}.$extension", isDirectory = false)
        )
        val data = file.fileBytes.usePinned { bytes ->
            NSData.create(bytes = bytes.addressOf(0), length = file.fileBytes.size.toULong())
        }
        check(data.writeToURL(targetUrl, atomically = true))
        return checkNotNull(targetUrl.absoluteString)
    }

    override suspend fun deleteAvatar(avatar: String) {
        val url = NSURL.URLWithString(avatar) ?: return
        val avatarsPath = applicationDirectory()
            .URLByAppendingPathComponent(AVATARS_DIRECTORY, isDirectory = true)
            ?.path ?: return
        val targetPath = url.path ?: return
        if (url.isFileURL() && targetPath.startsWith("$avatarsPath/")) {
            fileManager.removeItemAtURL(url, error = null)
        }
    }

    private fun applicationDirectory() = checkNotNull(
        fileManager.URLsForDirectory(NSApplicationSupportDirectory, NSUserDomainMask)
            .firstOrNull() as? NSURL
    )

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
