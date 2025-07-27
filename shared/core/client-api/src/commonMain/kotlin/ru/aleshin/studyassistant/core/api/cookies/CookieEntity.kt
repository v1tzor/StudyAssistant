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

package ru.aleshin.studyassistant.core.api.cookies

import io.ktor.http.Cookie
import io.ktor.http.CookieEncoding
import io.ktor.util.date.GMTDate
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmName

/**
 * @author Stanislav Aleshin on 09.07.2025.
 */
@Serializable
data class CookieEntity(
    val name: String,
    val value: String,
    val encoding: CookieEncoding = CookieEncoding.URI_ENCODING,
    @get:JvmName("getMaxAgeInt")
    val maxAge: Int? = null,
    val expires: Long? = null,
    val domain: String? = null,
    val path: String? = null,
    val secure: Boolean = false,
    val httpOnly: Boolean = false,
    val extensions: Map<String, String?> = emptyMap(),
) {
    fun toHttpCookie(): Cookie {
        return Cookie(
            name = name,
            value = value,
            encoding = encoding,
            maxAge = maxAge ?: 0,
            expires = expires?.let { GMTDate(it) },
            domain = domain,
            path = path,
            secure = secure,
            httpOnly = httpOnly,
            extensions = extensions,
        )
    }

    companion object Companion {
        fun from(cookie: Cookie): CookieEntity {
            return CookieEntity(
                name = cookie.name,
                value = cookie.value,
                encoding = cookie.encoding,
                maxAge = cookie.maxAge,
                expires = cookie.expires?.timestamp,
                domain = cookie.domain,
                path = cookie.path,
                secure = cookie.secure,
                httpOnly = cookie.httpOnly,
                extensions = cookie.extensions,
            )
        }
    }
}