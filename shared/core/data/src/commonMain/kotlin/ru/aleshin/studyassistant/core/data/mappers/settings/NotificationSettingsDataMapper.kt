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

package ru.aleshin.studyassistant.core.data.mappers.settings

import ru.aleshin.studyassistant.core.domain.entities.settings.NotificationSettings
import ru.aleshin.studyassistant.sqldelight.settings.NotificationSettingsEntity

/**
 * @author Stanislav Aleshin on 01.05.2024.
 */
fun NotificationSettingsEntity.mapToDomain() = NotificationSettings(
    beginningOfClasses = beginning_of_classes,
    exceptionsForBeginningOfClasses = exceptions_for_beginning_of_classes,
    endOfClasses = end_of_classes == 1L,
    exceptionsForEndOfClasses = exceptions_for_end_of_classes,
    unfinishedHomeworks = unfinished_homeworks,
    highWorkload = high_workload?.toInt(),
)

fun NotificationSettings.mapToLocalData() = NotificationSettingsEntity(
    id = 1L,
    beginning_of_classes = beginningOfClasses,
    exceptions_for_beginning_of_classes = exceptionsForBeginningOfClasses,
    end_of_classes = if (endOfClasses) 1L else 0L,
    exceptions_for_end_of_classes = exceptionsForEndOfClasses,
    unfinished_homeworks = unfinishedHomeworks,
    high_workload = highWorkload?.toLong(),
)