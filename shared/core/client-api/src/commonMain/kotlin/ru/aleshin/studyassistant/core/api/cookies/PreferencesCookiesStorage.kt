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

import com.russhwolf.settings.Settings
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.client.plugins.cookies.fillDefaults
import io.ktor.client.plugins.cookies.matches
import io.ktor.http.Cookie
import io.ktor.http.Url
import io.ktor.util.date.getTimeMillis
import kotlinx.atomicfu.AtomicLong
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.aleshin.studyassistant.core.common.extensions.fromJson
import ru.aleshin.studyassistant.core.common.extensions.toJson
import kotlin.math.min

/**
 * @author Stanislav Aleshin on 09.07.2025.
 */
interface PreferencesCookiesStorage : CookiesStorage {

    fun cleanStorage()

    class Base(private val settings: Settings) : PreferencesCookiesStorage {

        private val oldestCookie: AtomicLong = atomic(0L)
        private val mutex = Mutex()

        init {
            val container = readAllCookies()
            val now = getTimeMillis()
            if (now >= oldestCookie.value) cleanup(now, container)
        }

        override suspend fun addCookie(requestUrl: Url, cookie: Cookie) {
            with(cookie) {
                if (name.isBlank()) return
            }
            mutex.withLock {
                val container = readAllCookies().toMutableList()
                container.removeAll { (existingCookie, _) ->
                    existingCookie.name == cookie.name && existingCookie.toHttpCookie().matches(requestUrl)
                }
                val createdAt = getTimeMillis()
                container.add(
                    CookieCacheEntity(
                        cookie = CookieEntity.from(cookie = cookie.fillDefaults(requestUrl)),
                        createdAt = createdAt,
                    ),
                )

                cookie.maxAgeOrExpires(createdAt)?.let {
                    if (oldestCookie.value > it) {
                        oldestCookie.value = it
                    }
                }
                writeAllCookies(container)
            }
        }

        override fun close() {}

        override suspend fun get(requestUrl: Url): List<Cookie> = mutex.withLock {
            val container = readAllCookies()
            val now = getTimeMillis()
            if (now >= oldestCookie.value) cleanup(now, container)
            val cookies = container.map { entity -> entity.cookie.toHttpCookie() }.filter { it.matches(requestUrl) }
            return@withLock cookies
        }

        override fun cleanStorage() {
            settings.remove("cookies")
        }

        private fun readAllCookies(): List<CookieCacheEntity> {
            return settings.getStringOrNull("cookies")?.fromJson<List<CookieCacheEntity>>() ?: emptyList()
        }

        private fun writeAllCookies(cookies: List<CookieCacheEntity>) {
            settings.putString("cookies", cookies.toJson())
        }

        private fun cleanup(
            timestamp: Long,
            cachedCookies: List<CookieCacheEntity>,
        ) {
            val container = cachedCookies.toMutableList()
            container.removeAll { (cookie, createdAt) ->
                val expires = cookie.toHttpCookie().maxAgeOrExpires(createdAt) ?: return@removeAll false
                expires < timestamp
            }

            val newOldest = container.fold(Long.MAX_VALUE) { acc, (cookie, createdAt) ->
                cookie.toHttpCookie().maxAgeOrExpires(createdAt)?.let { min(acc, it) } ?: acc
            }

            oldestCookie.value = newOldest
        }

        internal fun Cookie.maxAgeOrExpires(createdAt: Long): Long? {
            return maxAge?.let { createdAt + it * 1000L } ?: expires?.timestamp
        }
    }
}