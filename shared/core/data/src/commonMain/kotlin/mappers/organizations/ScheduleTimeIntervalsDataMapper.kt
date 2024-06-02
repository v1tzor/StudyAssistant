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

package mappers.organizations

import entities.organizations.NumberedDuration
import entities.organizations.ScheduleTimeIntervals
import extensions.mapEpochTimeToInstant
import models.organizations.NumberedDurationData
import models.organizations.ScheduleTimeIntervalsData

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
fun ScheduleTimeIntervals.mapToDate() = ScheduleTimeIntervalsData(
    firstClassTime = firstClassTime?.toEpochMilliseconds(),
    baseClassDuration = baseClassDuration,
    baseBreakDuration = baseBreakDuration,
    specificClassDuration = specificClassDuration.map { it.mapToDate() },
    specificBreakDuration = specificBreakDuration.map { it.mapToDate() }
)

fun NumberedDuration.mapToDate() = NumberedDurationData(
    number = number,
    duration = duration,
)

fun ScheduleTimeIntervalsData.mapToDomain() = ScheduleTimeIntervals(
    firstClassTime = firstClassTime?.mapEpochTimeToInstant(),
    baseClassDuration = baseClassDuration,
    baseBreakDuration = baseBreakDuration,
    specificClassDuration = specificClassDuration.map { it.mapToDomain() },
    specificBreakDuration = specificBreakDuration.map { it.mapToDomain() },
)

fun NumberedDurationData.mapToDomain() = NumberedDuration(
    number = number,
    duration = duration,
)
