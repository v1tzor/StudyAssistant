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

import ru.aleshin.studyassistant.core.remote.appwrite.auth.AppwriteAuth
import ru.aleshin.studyassistant.core.remote.appwrite.databases.AppwriteDatabase
import ru.aleshin.studyassistant.core.remote.appwrite.databases.AppwriteRealtime
import ru.aleshin.studyassistant.core.remote.appwrite.storage.AppwriteStorage
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

/**
 * @author Stanislav Aleshin on 29.06.2025.
 */
abstract class Appwrite {
    abstract val auth: AppwriteAuth
    abstract val databases: AppwriteDatabase
    abstract val realtime: AppwriteRealtime
    abstract val storage: AppwriteStorage
}

@OptIn(ExperimentalObjCName::class)
@ObjCName("RemoteAppwriteException", exact = true)
data class AppwriteException(
    override val message: String? = null,
    val code: Int? = null,
    val type: String? = null,
    val response: String? = null
) : Exception()