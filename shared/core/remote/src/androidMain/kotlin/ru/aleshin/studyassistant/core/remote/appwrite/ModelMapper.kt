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

import ru.aleshin.studyassistant.core.remote.appwrite.auth.Preferences
import ru.aleshin.studyassistant.core.remote.appwrite.auth.Target
import ru.aleshin.studyassistant.core.remote.appwrite.auth.User
import ru.aleshin.studyassistant.core.remote.appwrite.databases.Document
import ru.aleshin.studyassistant.core.remote.appwrite.databases.DocumentList
import ru.aleshin.studyassistant.core.remote.appwrite.databases.RealtimeResponse
import ru.aleshin.studyassistant.core.remote.appwrite.databases.RealtimeResponseEvent
import ru.aleshin.studyassistant.core.remote.appwrite.databases.RealtimeSubscription
import ru.aleshin.studyassistant.core.remote.appwrite.storage.File
import ru.aleshin.studyassistant.core.remote.appwrite.storage.FileList
import ru.aleshin.studyassistant.core.remote.appwrite.storage.UploadProgress

/**
 * @author Stanislav Aleshin on 26.06.2025.
 */
internal fun <T> io.appwrite.models.Document<T>.convertToCommon(): Document<T> {
    return Document(
        id = id,
        collectionId = collectionId,
        databaseId = databaseId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        permissions = permissions,
        data = data,
    )
}

internal fun <T> io.appwrite.models.DocumentList<T>.convertToCommon(): DocumentList<T> {
    return DocumentList(
        total = total,
        documents = documents.map { it.convertToCommon() },
    )
}

internal fun <T> io.appwrite.models.User<T>.convertToCommon(): User<T> {
    return User(
        id = id,
        createdAt = createdAt,
        updatedAt = updatedAt,
        name = name,
        password = password,
        hash = hash,
        hashOptions = hashOptions,
        registration = registration,
        status = status,
        labels = labels,
        passwordUpdate = passwordUpdate,
        email = email,
        phone = phone,
        emailVerification = emailVerification,
        phoneVerification = phoneVerification,
        mfa = mfa,
        prefs = Preferences(prefs.data),
        targets = targets.map { it.convertToCommon() },
        accessedAt = accessedAt,
    )
}

internal fun io.appwrite.models.Target.convertToCommon(): Target {
    return Target(
        id = id,
        createdAt = createdAt,
        updatedAt = updatedAt,
        name = name,
        userId = userId,
        providerId = providerId,
        providerType = providerType,
        identifier = identifier,
        expired = expired,
    )
}

internal fun io.appwrite.models.RealtimeSubscription.convertToCommon(): RealtimeSubscription {
    return RealtimeSubscription(close = { close() })
}

internal fun <T> io.appwrite.models.RealtimeResponseEvent<T>.convertToCommon(): RealtimeResponseEvent<T> {
    return RealtimeResponseEvent(
        events = events.toList(),
        channels = channels.toList(),
        timestamp = timestamp,
        payload = payload
    )
}

internal fun io.appwrite.models.RealtimeResponse.convertToCommon(): RealtimeResponse {
    return RealtimeResponse(
        type = type,
        data = data,
    )
}

internal fun io.appwrite.models.File.convertToCommon(): File {
    return File(
        id = id,
        bucketId = bucketId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        permissions = permissions,
        name = name,
        signature = signature,
        mimeType = mimeType,
        sizeOriginal = sizeOriginal,
        chunksTotal = chunksTotal,
        chunksUploaded = chunksUploaded
    )
}

internal fun io.appwrite.models.FileList.convertToCommon(): FileList {
    return FileList(
        total = total,
        files = files.map { it.convertToCommon() },
    )
}

internal fun io.appwrite.models.UploadProgress.convertToCommon(): UploadProgress {
    return UploadProgress(
        id = id,
        progress = progress,
        sizeUploaded = sizeUploaded,
        chunksTotal = chunksTotal,
        chunksUploaded = chunksUploaded,
    )
}