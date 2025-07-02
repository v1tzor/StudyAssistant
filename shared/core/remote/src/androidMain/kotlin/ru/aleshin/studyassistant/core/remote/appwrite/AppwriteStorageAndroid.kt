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

package ru.aleshin.studyassistant.core.remote.appwrite

import io.appwrite.models.InputFile
import io.appwrite.services.Storage
import ru.aleshin.studyassistant.core.remote.appwrite.storage.AppwriteStorage
import ru.aleshin.studyassistant.core.remote.appwrite.storage.File
import ru.aleshin.studyassistant.core.remote.appwrite.storage.FileList
import ru.aleshin.studyassistant.core.remote.appwrite.storage.ImageFormat
import ru.aleshin.studyassistant.core.remote.appwrite.storage.ImageGravity
import ru.aleshin.studyassistant.core.remote.appwrite.storage.UploadProgress

/**
 * @author Stanislav Aleshin on 30.06.2025.
 */
class AppwriteStorageAndroid(
    private val storage: Storage,
) : AppwriteStorage {

    override suspend fun getFile(
        bucketId: String,
        fileId: String
    ): File {
        val file = storage.getFile(
            bucketId = bucketId,
            fileId = fileId,
        )

        return file.convertToCommon()
    }

    override suspend fun createFile(
        bucketId: String,
        fileId: String,
        fileBytes: ByteArray,
        filename: String,
        mimeType: String,
        permissions: List<String>?,
        onProgress: ((UploadProgress) -> Unit)?
    ): File {
        val file = storage.createFile(
            bucketId = bucketId,
            fileId = fileId,
            file = InputFile.fromBytes(fileBytes, filename, mimeType),
            permissions = permissions,
            onProgress = { onProgress?.invoke(it.convertToCommon()) },
        )

        return file.convertToCommon()
    }

    override suspend fun listFiles(
        bucketId: String,
        queries: List<String>?,
        search: String?
    ): FileList {
        val fileList = storage.listFiles(
            bucketId = bucketId,
            queries = queries,
            search = search,
        )

        return fileList.convertToCommon()
    }

    override suspend fun updateFile(
        bucketId: String,
        fileId: String,
        name: String?,
        permissions: List<String>?
    ): File {
        val file = storage.updateFile(
            bucketId = bucketId,
            fileId = fileId,
            name = name,
            permissions = permissions,
        )

        return file.convertToCommon()
    }

    override suspend fun deleteFile(bucketId: String, fileId: String): Any {
        return storage.deleteFile(bucketId, fileId)
    }

    override suspend fun getFileDownload(
        bucketId: String,
        fileId: String,
        token: String?
    ): ByteArray {
        return storage.getFileDownload(bucketId, fileId, token)
    }

    override suspend fun getFilePreview(
        bucketId: String,
        fileId: String,
        width: Long?,
        height: Long?,
        gravity: ImageGravity?,
        quality: Long?,
        borderWidth: Long?,
        borderColor: String?,
        borderRadius: Long?,
        opacity: Double?,
        rotation: Long?,
        background: String?,
        output: ImageFormat?,
        token: String?
    ): ByteArray {
        val byteArray = storage.getFilePreview(
            bucketId = bucketId,
            fileId = fileId,
            width = width,
            height = height,
            gravity = gravity?.name?.let {
                io.appwrite.enums.ImageGravity.valueOf(it)
            },
            quality = quality,
            borderWidth = borderWidth,
            borderColor = borderColor,
            opacity = opacity,
            rotation = rotation,
            background = background,
            output = output?.name?.let {
                io.appwrite.enums.ImageFormat.valueOf(it)
            },
        )

        return byteArray
    }

    override suspend fun getFileView(
        bucketId: String,
        fileId: String,
        token: String?
    ): ByteArray {
        val byteArray = storage.getFileView(bucketId, fileId, token)

        return byteArray
    }
}