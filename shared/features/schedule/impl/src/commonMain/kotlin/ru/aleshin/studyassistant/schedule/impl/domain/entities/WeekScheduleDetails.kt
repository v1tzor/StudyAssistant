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

package ru.aleshin.studyassistant.schedule.impl.domain.entities

import entities.common.NumberOfRepeatWeek
import entities.schedules.ScheduleDetails
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant

/**
 * @author Stanislav Aleshin on 30.05.2024.
 */
internal data class WeekScheduleDetails(
    val from: Instant,
    val to: Instant,
    val numberOfWeek: NumberOfRepeatWeek,
    val weekDaySchedules: Map<DayOfWeek, ScheduleDetails?>,
)