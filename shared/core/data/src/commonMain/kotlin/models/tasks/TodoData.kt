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

package models.tasks

import entities.tasks.TaskPriority
import functional.UID
import kotlinx.serialization.Serializable

/**
 * @author Stanislav Aleshin on 01.07.2024.
 */
@Serializable
data class TodoData(
    val uid: UID,
    val deadline: Long? = null,
    val name: String = "",
    val priority: String = TaskPriority.STANDARD.name,
    val notification: Boolean = true,
    val done: Boolean = false,
    val completeDate: Long? = null,
)