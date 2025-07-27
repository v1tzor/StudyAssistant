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

package ru.aleshin.studyassistant.core.database.utils

import kotlinx.serialization.Serializable

/**
 * Base class for all local entities stored in the local database (e.g. SQLDelight).
 *
 * All entities that participate in synchronization or offline storage must inherit from this class.
 * It provides a consistent structure for identifying and comparing entities during synchronization.
 *
 * @property uid A unique identifier for the entity. Usually corresponds to the remote document ID.
 * @property updatedAt The timestamp representing the last time the entity was modified.
 * Used to resolve conflicts and determine which data version is more recent.
 *
 * @author Stanislav Aleshin on 21.07.2025.
 */
@Serializable
abstract class BaseLocalEntity {
    abstract val uid: String
    abstract val updatedAt: Long
}