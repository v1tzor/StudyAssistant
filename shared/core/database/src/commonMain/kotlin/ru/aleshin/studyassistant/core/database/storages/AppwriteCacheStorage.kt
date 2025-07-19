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

package ru.aleshin.studyassistant.core.database.storages

/**
 * @author Stanislav Aleshin on 11.07.2025.
 */
//class AppwriteCacheStorage(
//    private val database: AppwriteCacheQueries,
//    private val coroutineManager: CoroutineManager,
//    private val dateManager: DateManager,
//) : CacheStorage {
//
//    init {
//        cleanExpired()
//    }
//
//    override suspend fun store(url: Url, data: CachedResponseData) {
//        coroutineManager.changeFlow(BACKGROUND) {
//            database.insertOrReplace(data.toCacheEntity())
//        }
//    }
//
//    override suspend fun find(url: Url, varyKeys: Map<String, String>): CachedResponseData? {
//        return coroutineManager.changeFlow(BACKGROUND) {
//            val cacheEntity = database.selectByUrl(url.toString()).executeAsList().map {
//                it.toCachedResponseData()
//            }
//            cacheEntity.find { varyKeys.all { (key, value) -> it.varyKeys[key] == value } }
//        }
//    }
//
//    override suspend fun findAll(url: Url): Set<CachedResponseData> {
//        return coroutineManager.changeFlow(BACKGROUND) {
//            val cacheEntity = database.selectByUrl(url.toString()).executeAsList().map {
//                it.toCachedResponseData()
//            }
//            cacheEntity.toSet()
//        }
//    }
//
//    override suspend fun remove(
//        url: Url,
//        varyKeys: Map<String, String>
//    ) {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun removeAll(url: Url) {
//        TODO("Not yet implemented")
//    }
//
//    fun cleanExpired() {
//        val currentTime = dateManager.fetchCurrentInstant().toEpochMilliseconds()
//        database.deleteExpired(currentTime)
//    }
//
//    fun clearAll() {
//        database.deleteAll()
//    }
//}