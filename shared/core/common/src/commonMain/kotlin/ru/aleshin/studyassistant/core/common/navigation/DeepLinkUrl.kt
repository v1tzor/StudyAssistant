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

package ru.aleshin.studyassistant.core.common.navigation

import kotlinx.serialization.Serializable

/**
 * @author Stanislav Aleshin on 23.08.2025.
 */
@Serializable
data class DeepLinkUrl(
    val pathSegments: List<String>,
    val params: Map<String, String>,
) {
    companion object {
        fun fromString(url: String): DeepLinkUrl {
            var path: String = url.substringAfter(delimiter = "://").substringAfter(delimiter = "/")
            var params: Map<String, String> = emptyMap()

            if ('?' in path) {
                params = path.substringAfter(delimiter = "?")
                    .split("&")
                    .map { it.split("=") }
                    .associate { (key, value) -> key to value }

                path = path.substringBefore(delimiter = "?")
            }

            return DeepLinkUrl(pathSegments = path.split("/"), params = params)
        }
    }
}

fun DeepLinkUrl.consumePathSegment(): Pair<String?, DeepLinkUrl> {
    return pathSegments.firstOrNull() to copy(pathSegments = pathSegments.drop(1))
}

inline fun <reified T : Any> pathSegmentOf(): String {
    return T::class.simpleName?.lowerCamelCase() ?: ""
}

fun String.lowerCamelCase(): String = buildString {
    for (char in this@lowerCamelCase) {
        if (char.isUpperCase() && isNotEmpty()) {
            append(char)
        } else {
            append(char.lowercaseChar())
        }
    }
}