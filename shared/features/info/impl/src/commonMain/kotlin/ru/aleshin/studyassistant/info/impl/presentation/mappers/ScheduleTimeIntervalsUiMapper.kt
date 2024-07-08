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

package ru.aleshin.studyassistant.info.impl.presentation.mappers

import ru.aleshin.studyassistant.core.domain.entities.organizations.NumberedDuration
import ru.aleshin.studyassistant.core.domain.entities.organizations.ScheduleTimeIntervals
import ru.aleshin.studyassistant.info.impl.presentation.models.orgnizations.NumberedDurationUi
import ru.aleshin.studyassistant.info.impl.presentation.models.orgnizations.ScheduleTimeIntervalsUi

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
internal fun ScheduleTimeIntervals.mapToUi() = ScheduleTimeIntervalsUi(
    firstClassTime = firstClassTime,
    baseClassDuration = baseClassDuration,
    baseBreakDuration = baseBreakDuration,
    specificClassDuration = specificClassDuration.map { it.mapToUi() },
    specificBreakDuration = specificBreakDuration.map { it.mapToUi() },
)

internal fun NumberedDuration.mapToUi() = NumberedDurationUi(
    number = number,
    duration = duration,
)

internal fun ScheduleTimeIntervalsUi.mapToDomain() = ScheduleTimeIntervals(
    firstClassTime = firstClassTime,
    baseClassDuration = baseClassDuration,
    baseBreakDuration = baseBreakDuration,
    specificClassDuration = specificClassDuration.map { it.mapToDomain() },
    specificBreakDuration = specificBreakDuration.map { it.mapToDomain() }
)

internal fun NumberedDurationUi.mapToDomain() = NumberedDuration(
    number = number,
    duration = duration,
)
