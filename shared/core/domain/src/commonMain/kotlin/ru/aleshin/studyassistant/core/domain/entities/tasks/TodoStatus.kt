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

package ru.aleshin.studyassistant.core.domain.entities.tasks

import kotlinx.datetime.Instant

/**
 * @author Stanislav Aleshin on 01.07.2024.
 */
enum class TodoStatus {
    COMPLETE, IN_PROGRESS, NOT_COMPLETE;

    companion object {
        fun calculate(
            isDone: Boolean,
            deadline: Instant?,
            currentTime: Instant
        ): TodoStatus {
            return if (isDone) {
                COMPLETE
            } else {
                if (deadline != null) {
                    if (deadline >= currentTime) IN_PROGRESS else NOT_COMPLETE
                } else {
                    IN_PROGRESS
                }
            }
        }
    }
}