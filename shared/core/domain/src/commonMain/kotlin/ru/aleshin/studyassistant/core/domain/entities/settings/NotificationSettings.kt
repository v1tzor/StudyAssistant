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

package ru.aleshin.studyassistant.core.domain.entities.settings

import ru.aleshin.studyassistant.core.common.functional.UID

/**
 * @author Stanislav Aleshin on 10.07.2024.
 */
data class NotificationSettings(
    val beginningOfClasses: Long? = BEFORE_BEGINNING_CLASSES_NOTIFY_TIME,
    val exceptionsForBeginningOfClasses: List<UID> = emptyList(),
    val endOfClasses: Boolean = true,
    val exceptionsForEndOfClasses: List<UID> = emptyList(),
    val unfinishedHomeworks: Long? = UNFINISHED_HOMEWORKS_NOTIFY_TIME,
    val highWorkload: Int? = WORKLOAD_HIGH_VALUE,
) {
    companion object {
        const val BEFORE_BEGINNING_CLASSES_NOTIFY_TIME = 600000L
        const val UNFINISHED_HOMEWORKS_NOTIFY_TIME = 72000000L
        const val WORKLOAD_HIGH_VALUE = 7
    }
}