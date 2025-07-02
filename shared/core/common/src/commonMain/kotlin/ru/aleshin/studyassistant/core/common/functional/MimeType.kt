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

package ru.aleshin.studyassistant.core.common.functional

/**
 * @author Stanislav Aleshin on 01.07.2025.
 */
fun getMimeTypeFromFileName(fileName: String): String {
    val extension = fileName.substringAfterLast('.', "").lowercase()
    return extensionToMimeType[extension] ?: "application/octet-stream"
}

private val extensionToMimeType = mapOf(
    "txt" to "text/plain",
    "html" to "text/html",
    "htm" to "text/html",
    "css" to "text/css",
    "csv" to "text/csv",
    "xml" to "application/xml",
    "json" to "application/json",
    "js" to "application/javascript",
    "pdf" to "application/pdf",
    "zip" to "application/zip",
    "tar" to "application/x-tar",
    "gz" to "application/gzip",
    "rar" to "application/vnd.rar",
    "7z" to "application/x-7z-compressed",
    "doc" to "application/msword",
    "docx" to "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    "xls" to "application/vnd.ms-excel",
    "xlsx" to "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    "ppt" to "application/vnd.ms-powerpoint",
    "pptx" to "application/vnd.openxmlformats-officedocument.presentationml.presentation",
    "mp3" to "audio/mpeg",
    "wav" to "audio/wav",
    "ogg" to "audio/ogg",
    "m4a" to "audio/mp4",
    "flac" to "audio/flac",
    "mp4" to "video/mp4",
    "mov" to "video/quicktime",
    "avi" to "video/x-msvideo",
    "mkv" to "video/x-matroska",
    "webm" to "video/webm",
    "jpg" to "image/jpeg",
    "jpeg" to "image/jpeg",
    "png" to "image/png",
    "gif" to "image/gif",
    "bmp" to "image/bmp",
    "webp" to "image/webp",
    "svg" to "image/svg+xml",
    "ico" to "image/vnd.microsoft.icon",
    "heic" to "image/heic",
    "heif" to "image/heif",
    "apk" to "application/vnd.android.package-archive",
    "aab" to "application/vnd.android.package-archive",
    "exe" to "application/vnd.microsoft.portable-executable",
    "dmg" to "application/x-apple-diskimage",
    "iso" to "application/x-iso9660-image"
)