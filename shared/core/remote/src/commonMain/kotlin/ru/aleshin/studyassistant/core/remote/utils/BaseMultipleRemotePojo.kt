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

package ru.aleshin.studyassistant.core.remote.utils

import kotlinx.serialization.Serializable

/**
 * Base class for all remote data models used with Appwrite.
 *
 * This class defines common fields that must be present in all documents
 * retrieved from or sent to the Appwrite database.
 *
 * Subclasses of [BaseRemotePojo] represent serialized data (DTOs) that map
 * directly to Appwrite documents.
 *
 * All inheriting models must declare:
 * - a unique `id` matching the Appwrite document ID (`$id`),
 * - a UNIX timestamp `updatedAt` indicating the last update time.
 *
 * The `updatedAt` field is used during synchronization to resolve data conflicts
 * between the local and remote sources by comparing which version is newer.
 *
 * @author Stanislav Aleshin on 21.07.2025.
 */
@Serializable
abstract class BaseMultipleRemotePojo : BaseRemotePojo() {

    /**
     * The ID of the user who owns the document
     */
    abstract val userId: String
}